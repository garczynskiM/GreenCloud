package AgentStuff;

import ScenarioStructs.TaskToDistribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.sql.Time;

public class ScenarioAgent extends Agent
{
    Graph graph;
    Time timeElapsed;
    Scenario scenarioToRealise;
    int secondsElapsed = 0;
    String cloudAgentNickname = "CloudAgent";

    @Override
    protected void setup()
    {
        System.out.println("Scenario agent created");
        scenarioToRealise = Scenario.createScenario1();
        timeElapsed = new Time(0);
        /*Object[] args = getArguments();
        graph = (Graph)args[0];*/
        System.setProperty("org.graphstream.ui", "swing");
        graph = new SingleGraph("System");
        //graph.setAttribute("ui.stylesheet", "url('CSS/nodeStuff')");
        graph.display();
        ContainerController cc = getContainerController();
        Object[] cloudArgs = new Object[2];
        cloudArgs[0] = scenarioToRealise.SystemInfo;
        cloudArgs[1] = graph;
        AgentContainer c = getContainerController();
        try {
            AgentController a = c.createNewAgent(cloudAgentNickname,"AgentStuff.CloudAgent", cloudArgs);
            a.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        /*AgentController ac = null;
        try
        {
            System.out.println("Creating cloud agent");
            ac = cc.createNewAgent(cloudAgentNickname, "AgentStuff.CloudAgent", cloudArgs);
            ac.start();
        }
        catch (StaleProxyException e)
        {
            e.printStackTrace();
        }*/

        Behaviour wakerSystemStart = createWakerSystemStart();
        addBehaviour(wakerSystemStart);
    }
    private Behaviour createWakerSystemStart()
    {
        return new WakerBehaviour(this, 5000)
        {
            @Override
            protected void onWake()
            {
                Behaviour cyclicMessageSender = createTickerMessageSender();

                // Send info to system to start counting time
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                msg.addReceiver(new AID(cloudAgentNickname, AID.ISLOCALNAME));
                msg.setLanguage("English");
                msg.setOntology("System startup");
                msg.setContent("System starts, start counting time.");
                send(msg);
                myAgent.addBehaviour(cyclicMessageSender);
            }
        };
    }
    private Behaviour createTickerMessageSender()
    {
        return new TickerBehaviour(this, 1000)
        {
            @Override
            protected void onTick() {
                secondsElapsed++;
                timeElapsed.setTime(secondsElapsed * 1000L);
                System.out.println(getLocalName() + " - " + timeElapsed);
                // Check if we can distribute another task

                /*for(int i = 0; i < scenarioToRealise.TasksToDistribute.size(); i++)
                {
                    TaskToDistribute temp = scenarioToRealise.TasksToDistribute.get(i);
                    if(temp.StartTime <= secondsElapsed)
                    {
                        Task task = temp.Task;
                        // send the task to the cloud
                    }
                }*/
            }
        };
    };
}
