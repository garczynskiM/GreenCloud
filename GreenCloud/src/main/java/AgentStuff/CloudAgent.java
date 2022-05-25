package AgentStuff;

import ScenarioStructs.CloudAgentData;
import ScenarioStructs.RegionalAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.io.IOException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class CloudAgent extends Agent {
    List<String> regionalAgentNames;
    Time timeElapsed;
    int secondsElapsed = 0;
    Queue<Task> tasks;
    Graph display;

    private void initialNodeStyle() {
        Node node = display.getNode(getLocalName());
        /*node.setAttribute("ui.style", "fill-color: rgb(0,255,255);size: 30px;" +
                "text-alignment: under;");*/
        node.setAttribute("ui.class", "cloud");
        node.setAttribute("ui.label", getLocalName());
    }

    @Override
    protected void setup() {
        System.out.println("Cloud agent created");
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        timeElapsed = new Time(0);
        CloudAgentData initData = (CloudAgentData)args[0];
        regionalAgentNames = new ArrayList<>();
        display = (Graph)args[1];
        display.addNode(getLocalName());
        initialNodeStyle();
        tasks = new LinkedList<>();

        for (RegionalAgentData data: initData.AgentsToCreate) {
            ContainerController cc = getContainerController();
            Object[] containerArgs = new Object[4];
            containerArgs[0] = data;
            containerArgs[1] = display;
            containerArgs[2] = getName();
            containerArgs[3] = getLocalName();
            /*AgentController ac = null;
            try {
                System.out.println("Creating regional agent");
                ac = cc.createNewAgent(data.RegionalAgentName, "AgentStuff.RegionalAgent", containerArgs);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }*/
            AgentContainer c = getContainerController();
            try {
                AgentController a = c.createNewAgent(data.RegionalAgentName, "AgentStuff.RegionalAgent",
                        containerArgs);
                a.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            regionalAgentNames.add(data.RegionalAgentName);
        }
        addBehaviour(createCyclicSystemStartupManager());
        addBehaviour(createTaskGeneratorTicker());
        addBehaviour(createTaskSenderTicker());
    }

    private Task generateTask() {
        var random = new Random();
        var duration = Duration.ofSeconds(random.nextInt(5));
        var deadline = LocalDateTime.now().plusSeconds((random.nextInt(1000)+1)*duration.toSeconds());
        return new Task(UUID.randomUUID().toString(), duration, random.nextInt(9), random.nextInt(9), deadline);
    }

    private Behaviour createCyclicSystemStartupManager() {
        return new CyclicBehaviour() {
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
                            for (String regionalAgentName : regionalAgentNames) {
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                                msg.addReceiver(new AID(regionalAgentName, AID.ISLOCALNAME));
                                msg.setLanguage("English");
                                msg.setOntology("System startup");
                                msg.setContent("System starts, start counting time.");
                                send(msg);
                            }
                            System.out.println("Started waking up");
                            addBehaviour(createTickerTimeMeasurement());
                    }
                }
                block();
            }
        };
    }
    private Behaviour createTickerTimeMeasurement() {
        return new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                secondsElapsed++;
                timeElapsed.setTime(secondsElapsed * 1000L);
                System.out.println(getLocalName() + " - " + timeElapsed);
            }
        };
    }

    private Behaviour createTaskGeneratorTicker() {
        return new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                tasks.offer(generateTask());
            }
        };
    }

    private Behaviour createTaskSenderTicker() {
        return new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                if (tasks.isEmpty()) {
                    return;
                }
                var random = new Random();
                var agentIndex = random.nextInt(regionalAgentNames.size());
                var regionalAgent = regionalAgentNames.get(agentIndex);
                var regionalAgentAID = new AID(regionalAgent, AID.ISLOCALNAME);
                var task = tasks.poll();
                if (task == null) {
                    return;
                }
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.addReceiver(regionalAgentAID);
                try {
                    message.setContent(Task.taskToString(task));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                myAgent.send(message);
                System.out.format("[%s] Sent task to [%s]!\n", myAgent.getName(), regionalAgentAID.getName());
            }
        };
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
