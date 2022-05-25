package AgentStuff;

import ScenarioStructs.CloudAgentData;
import ScenarioStructs.RegionalAgentData;
import ScenarioStructs.TaskToDistribute;
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
import org.graphstream.graph.Node;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CloudAgent extends Agent {

    List<String> RegionalAgentNames;
    Time timeElapsed;
    int secondsElapsed = 0;
    List<Task> Tasks;
    Graph Display;

    private void initialNodeStyle()
    {
        Node node = Display.getNode(getLocalName());
        node.setAttribute("ui.style", "fill-color: rgb(0,255,255);size: 30px;" +
                "text-alignment: under;");
        node.setAttribute("ui.label", getLocalName());
    }

    @Override
    protected void setup() {
        System.out.println("Cloud agent created");
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        timeElapsed = new Time(0);
        CloudAgentData initData = (CloudAgentData)args[0];
        RegionalAgentNames = new ArrayList<>();
        Display = (Graph)args[1];
        Display.addNode(getLocalName());
        initialNodeStyle();

        for (RegionalAgentData data: initData.AgentsToCreate) {
            ContainerController cc = getContainerController();
            Object[] containerArgs = new Object[4];
            containerArgs[0] = data;
            containerArgs[1] = Display;
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
            catch (Exception e)
            {
                e.printStackTrace();
            }
            RegionalAgentNames.add(data.RegionalAgentName);
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
                            for(int i = 0; i < RegionalAgentNames.size(); i++)
                            {
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                                msg.addReceiver(new AID(RegionalAgentNames.get(i), AID.ISLOCALNAME));
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

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
