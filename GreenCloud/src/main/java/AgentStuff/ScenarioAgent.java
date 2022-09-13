package AgentStuff;

import ScenarioStructs.TaskToDistribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.IOException;
import java.sql.Time;
import java.util.Objects;

public class ScenarioAgent extends Agent
{
    Graph graph;
    Time timeElapsed;
    Scenario scenarioToRealise;
    int secondsElapsed = 0;
    String cloudAgentNickname = "CloudAgent";
    protected String styleSheet = "node {\n" +
            "        fill-color: grey;\n" +
            "        size: 10px;\n" +
            "        stroke-mode: plain;\n" +
            "        stroke-color: black;\n" +
            "        stroke-width: 1px;\n" +
            "        text-alignment: under;\n" +
            "    }\n" +
            "    node.cloud {\n" +
            "        size: 30px;\n" +
            "        fill-color: rgb(0,255,255);\n" +
            "    }\n" +
            "    node.regional {\n" +
            "        size: 20px;\n" +
            "        fill-color: rgb(255,0,0);\n" +
            "    }\n" +
            "    node.big {\n" +
            "        size: 15px;\n" +
            "    }";

    @Override
    protected void setup()
    {
        System.out.println("Scenario agent created");
        //scenarioToRealise = Scenario.createScenario1();
        //scenarioToRealise = Scenario.conflictingTaskScenario();
        //scenarioToRealise = Scenario.doubleTaskScenario();
        //scenarioToRealise = Scenario.HundredTasksScenario();
        //scenarioToRealise = Scenario.TenTasksScenario();
        scenarioToRealise = Scenario.SixtySequentialTasksScenario();
        timeElapsed = new Time(0);
        /*Object[] args = getArguments();
        graph = (Graph)args[0];*/
        System.setProperty("org.graphstream.ui", "swing");
        graph = new SingleGraph("System");
        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.display();
        ContainerController cc = getContainerController();
        Object[] cloudArgs = new Object[4];
        cloudArgs[0] = scenarioToRealise.SystemInfo;
        cloudArgs[1] = graph;
        cloudArgs[2] = scenarioToRealise.TasksToDistribute.size();
        cloudArgs[3] = this.getLocalName();
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
                Behaviour cyclicMessageReceiver = createCyclicJobCompletionManager();

                // Send info to system to start counting time
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                msg.addReceiver(new AID(cloudAgentNickname, AID.ISLOCALNAME));
                msg.setLanguage("English");
                msg.setOntology("System startup");
                msg.setContent("System starts, start counting time.");
                send(msg);
                scenarioToRealise.updateTaskDeadline();
                myAgent.addBehaviour(cyclicMessageSender);
                myAgent.addBehaviour(cyclicMessageReceiver);
            }
        };
    }
    private Behaviour createWakerSystemShutdown()
    {
        return new WakerBehaviour(this, 5000)
        {
            @Override
            protected void onWake()
            {
                takeDown();
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
                //System.out.println(getLocalName() + " - " + timeElapsed);
                // Check if we can distribute another task

                for(int i = 0; i < scenarioToRealise.TasksToDistribute.size();)
                {
                    TaskToDistribute temp = scenarioToRealise.TasksToDistribute.get(i);
                    if(temp.StartTime <= secondsElapsed)
                    {
                        Task task = temp.Task;
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                        msg.addReceiver(new AID(cloudAgentNickname, AID.ISLOCALNAME));
                        msg.setLanguage("English");
                        msg.setOntology("New task");
                        try {
                            msg.setContent(Task.taskToString(task));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        send(msg);
                        scenarioToRealise.TasksToDistribute.remove(i);
                    }
                    else
                    {
                        i++;
                    }
                }
            }
        };
    };
    private Behaviour createCyclicJobCompletionManager() {
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
                        case "Scenario complete":
                            Behaviour oneShotSystemShutdown = createWakerSystemShutdown();

                            // Propagate system shutdown
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                            msg.addReceiver(new AID(cloudAgentNickname, AID.ISLOCALNAME));
                            msg.setLanguage("English");
                            msg.setOntology("System shutdown");
                            msg.setContent("Job done, shut down.");
                            send(msg);
                            myAgent.addBehaviour(oneShotSystemShutdown);
                            break;
                    }
                }
                block();
            }
        };
    }
}
