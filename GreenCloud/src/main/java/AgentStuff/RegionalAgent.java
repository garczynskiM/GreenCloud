package AgentStuff;

import AgentStuff.Task;
import ScenarioStructs.ContainerAgentData;
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
import jade.wrapper.StaleProxyException;
import org.graphstream.graph.Graph;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class RegionalAgent extends Agent{
    List<String> ContainerAgentNames;
    String CloudAgentName = "CloudAgent";
    Time timeElapsed = new Time(0);
    int secondsElapsed = 0;
    List<Task> TasksToDistribute;
    Graph Display;

    @Override
    protected void setup() {
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        RegionalAgentData initData = (RegionalAgentData)args[0];
        ContainerAgentNames = new ArrayList<>();
        Display = (Graph)args[1];
        Display.addNode(getLocalName());
        Display.addEdge(CloudAgentName + " " + getLocalName(), CloudAgentName, getLocalName());
        for (ContainerAgentData data: initData.AgentsToCreate) {
            ContainerController cc = getContainerController();
            Object[] containerArgs = new Object[2];
            containerArgs[0] = data;
            containerArgs[1] = Display;
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
            catch (Exception e)
            {
                e.printStackTrace();
            }
            ContainerAgentNames.add(data.ContainerAgentName);
        }
        addBehaviour(createCyclicSystemStartupManager());
    }
    private Behaviour createCyclicSystemStartupManager()
    {
        return new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt =
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage rcv = receive(mt);
                if(rcv != null) {
                    String message = rcv.getContent();
                    String ontology = rcv.getOntology();
                    switch (ontology)
                    {
                        case "System startup":
                            // Propagate system startup
                            for(int i = 0; i < ContainerAgentNames.size(); i++)
                            {
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                                msg.addReceiver(new AID(ContainerAgentNames.get(i), AID.ISLOCALNAME));
                                msg.setLanguage("English");
                                msg.setOntology("System startup");
                                msg.setContent("System starts, start counting time.");
                                send(msg);
                            }
                            addBehaviour(createTickerTimeMeasurement());
                    }
                }
                block();
            }
        };
    }
    private Behaviour createTickerTimeMeasurement()
    {
        return new TickerBehaviour(this, 1000)
        {
            @Override
            protected void onTick() {
                secondsElapsed++;
                timeElapsed.setTime(secondsElapsed * 1000L);
                System.out.println(getLocalName() + " - " + timeElapsed);
            }
        };
    }
}
