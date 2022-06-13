package AgentStuff;

import ScenarioStructs.ContainerAgentData;
import ScenarioStructs.RegionalAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.io.IOException;
import java.sql.Time;
import java.util.*;

public class RegionalAgent extends Agent {
    List<String> containerAgentNames;
    String cloudAgentLocalName;
    String cloudAgentName;
    Time timeElapsed = new Time(0);
    int secondsElapsed = 0;
    List<Task> tasksToDistribute;
    Graph display;
    Map<String, Task> tasksSent;

    private void initialNodeStyle() {
        Node node = display.getNode(getLocalName());
        node.setAttribute("ui.style", "fill-color: rgb(255,0,0);size: 20px;" +
                "text-alignment: under;");
        node.setAttribute("ui.label", getLocalName());
    }

    @Override
    protected void setup() {
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        RegionalAgentData initData = (RegionalAgentData)args[0];
        containerAgentNames = new ArrayList<>();
        tasksSent = new HashMap<>();
        display = (Graph)args[1];
        cloudAgentName = (String)args[2];
        cloudAgentLocalName = (String)args[3];
        display.addNode(getLocalName());
        initialNodeStyle();
        display.addEdge(cloudAgentLocalName + " " + getLocalName(), cloudAgentLocalName, getLocalName());
        for (ContainerAgentData data: initData.AgentsToCreate) {
            ContainerController cc = getContainerController();
            Object[] containerArgs = new Object[4];
            containerArgs[0] = data;
            containerArgs[1] = display;
            containerArgs[2] = getName();
            containerArgs[3] = getLocalName();
            /*AgentController ac = null;
            try {
                ac = cc.createNewAgent(data.ContainerAgentName, "AgentStuff.ContainerAgent", containerArgs);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }*/
            AgentContainer c = getContainerController();
            try {
                AgentController a = c.createNewAgent(data.ContainerAgentName, "AgentStuff.ContainerAgent",
                        containerArgs);
                a.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            containerAgentNames.add(data.ContainerAgentName);
        }
        addBehaviour(createCyclicSystemStartupManager());
        addBehaviour(createReceiverFromCloudCyclic());
        addBehaviour(createReceiverFromContainerFailureCyclic());
    }
    private Behaviour createCyclicSystemStartupManager() {
        return new SimpleBehaviour() {
            private boolean finished = false;
            @Override
            public void action() {
                MessageTemplate mt =
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage rcv = receive(mt);
                if(rcv != null) {
                    String message = rcv.getContent();
                    String ontology = rcv.getOntology();
                    switch (ontology) {
                        case "System startup":
                            // Propagate system startup
                            for (String containerAgentName : containerAgentNames) {
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                                msg.addReceiver(new AID(containerAgentName, AID.ISLOCALNAME));
                                msg.setLanguage("English");
                                msg.setOntology("System startup");
                                msg.setContent("System starts, start counting time.");
                                send(msg);
                            }
                            addBehaviour(createTickerTimeMeasurement());
                            finished = true;
                    }
                }
                block();
            }

            @Override
            public boolean done() {
                return finished;
            }
        };
    }
    private Behaviour createTickerTimeMeasurement() {
        return new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                secondsElapsed++;
                timeElapsed.setTime(secondsElapsed * 1000L);
                //System.out.println(getLocalName() + " - " + timeElapsed);
            }
        };
    }

    private Behaviour createReceiverFromCloudCyclic() {
        return new CyclicBehaviour() {
            @Override
            public void action() {
                var cloudAID = new AID(cloudAgentName, AID.ISLOCALNAME);
                var mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                var message = myAgent.receive(mt);
                if (message != null) {
                    System.out.format("[%s] Got message from cloud\n", myAgent.getName());
                    var content = message.getContent();
                    var conversationId = UUID.randomUUID().toString();
                    try {
                        tasksSent.put(conversationId, Task.stringToTask(content));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    var cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.setConversationId(conversationId);
                    for (var containerAgent : containerAgentNames) {
                        cfp.addReceiver(new AID(containerAgent, AID.ISLOCALNAME));
                    }
                    cfp.setContent(content);
                    myAgent.send(cfp);
                    System.out.format("[%s] sent CallForProposal\n", myAgent.getName());
                    mt = MessageTemplate.MatchConversationId(conversationId);
                    myAgent.addBehaviour(createNegotiatorSimple(mt, conversationId));
                } else {
                    block();
                }
            }
        };
    }

    private Behaviour createReceiverFromContainerFailureCyclic()
    {
        return new CyclicBehaviour() {
            @Override
            public void action() {
                var mt = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
                var message = myAgent.receive(mt);
                if (message != null) {
                    System.out.format("[%s] Got failure message from container [%s]\n", myAgent.getName(),
                            message.getSender());
                    var content = message.getContent();
                    var conversationId = UUID.randomUUID().toString();
                    try {
                        tasksSent.put(conversationId, Task.stringToTask(content));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    var cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.setConversationId(conversationId);
                    for (var containerAgent : containerAgentNames) {
                        if(containerAgent != message.getSender().toString())
                            cfp.addReceiver(new AID(containerAgent, AID.ISLOCALNAME));
                    }
                    cfp.setContent(content);
                    myAgent.send(cfp);
                    System.out.format("[%s] sent CallForProposal\n", myAgent.getName());
                    mt = MessageTemplate.MatchConversationId(conversationId);
                    myAgent.addBehaviour(createNegotiatorSimple(mt, conversationId));
                } else {
                    block();
                }
            }
        };
    }

    private Behaviour createNegotiatorSimple(MessageTemplate mt, String conversationId) {
        return new SimpleBehaviour() {
            private int repliesCount = 0;
            private final List<ContainerProposal> proposals = new ArrayList<>();
            private boolean finished = false;

            @Override
            public void action() {
                var reply = myAgent.receive(mt);
                if (reply != null) {
                    repliesCount++;
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        // got proposal
                        try {
                            var proposal = ContainerProposal.stringToProposal(reply.getContent());
                            proposals.add(proposal);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    if (repliesCount == containerAgentNames.size()) {
                        // got replies from every container
                        if (!proposals.isEmpty()) {
                            myAgent.addBehaviour(createProposalChooserOneShot(proposals, conversationId));
                            finished = true;
                        }
                    }
                }
                else {
                    block();
                }
            }

            @Override
            public boolean done() {
                return finished;
            }
        };
    }

    private Behaviour createProposalChooserOneShot(List<ContainerProposal> proposals, String conversationId) {
        return new OneShotBehaviour() {
            @Override
            public void action() {
                ContainerProposal bestProposal = proposals.get(0);
                for (var i = 1; i < proposals.size(); i++) {
                    var current = proposals.get(i);
                    if (current.goodness > bestProposal.goodness) {
                        bestProposal = current;
                    }
                }
                var bestContainer = bestProposal.containerAID;
                var message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                message.setConversationId(conversationId);
                message.addReceiver(bestContainer);
                myAgent.send(message);

                var rejectMessage = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                rejectMessage.setConversationId(conversationId);
                for (var proposal : proposals) {
                    if (proposal.containerAID != bestContainer) {
                        rejectMessage.addReceiver(proposal.containerAID);
                    }
                }
                myAgent.send(rejectMessage);
            }
        };
    }
}
