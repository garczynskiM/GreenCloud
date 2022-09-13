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
    List<TaskWithStatus> tasks;
    Graph display;
    int numberOfTasksTotal;
    int numberOfTasksCompleted = 0;
    int numberOfTasksGreen = 0;
    int numberOfTasksNonGreen = 0;
    String scenarioAgentName;
    Random random;

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
        numberOfTasksTotal = (int)args[2];
        scenarioAgentName = (String)args[3];
        tasks = new ArrayList<>();
        random = new Random(100);
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
        //addBehaviour(createTaskCheckerTicker());
        //addBehaviour(createTaskGeneratorTicker());
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
                while(rcv != null) {
                    String message = rcv.getContent();
                    String ontology = rcv.getOntology();
                    Task completedTask = null;
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
                            break;
                        case "New task":
                            try {
                                TaskWithStatus newTask = new TaskWithStatus();
                                newTask.task = Task.stringToTask(message);
                                newTask.status = TaskStatus.NotSent;
                                tasks.add(newTask);
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "Task completed green":
                            try {
                                completedTask = Task.stringToTask(message);
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            if(completedTask == null) return;
                            for(int i = 0; i < tasks.size();)
                            {
                                TaskWithStatus task = tasks.get(i);
                                if(Objects.equals(completedTask.id, task.task.id))
                                {
                                    task.status = TaskStatus.CompletedWithGreen;
                                    //System.out.format("Cloud received completed task by green container: [task id=%s]!\n",
                                    //       task.task.id);
                                    tasks.remove(task);
                                    break;
                                }
                                else i++;
                            }
                            numberOfTasksCompleted++;
                            numberOfTasksGreen++;
                            checkIfTasksCompleted();
                            break;
                        case "Task completed nonGreen":
                            try {
                                completedTask = Task.stringToTask(message);
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            if(completedTask == null) return;
                            for(int i = 0; i < tasks.size();)
                            {
                                TaskWithStatus task = tasks.get(i);
                                if(Objects.equals(completedTask.id, task.task.id))
                                {
                                    task.status = TaskStatus.CompletedWithoutGreen;
                                    //System.out.format("Cloud received completed task by regional agent: [task id=%s]!\n",
                                    //        task.task.id);
                                    tasks.remove(task);
                                    break;
                                }
                                else i++;
                            }
                            numberOfTasksCompleted++;
                            numberOfTasksNonGreen++;
                            checkIfTasksCompleted();
                            break;
                        case "System shutdown":
                            for (String regionalAgentName : regionalAgentNames) {
                                ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
                                msg.addReceiver(new AID(regionalAgentName, AID.ISLOCALNAME));
                                msg.setLanguage("English");
                                msg.setOntology("System shutdown");
                                msg.setContent("Job done, shut down.");
                                send(msg);
                            }
                            System.out.format("All tasks complete. There were %s tasks, out of which %s were completed " +
                                    "green and %s were completed non-green.\n", numberOfTasksTotal, numberOfTasksGreen, numberOfTasksNonGreen);
                            takeDown();
                            break;
                    }
                    rcv = receive(mt);
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

//    private Behaviour createTaskGeneratorTicker() {
//        return new TickerBehaviour(this, 3000) {
//            @Override
//            protected void onTick() {
//                tasks.add(new TaskWithStatus(generateTask(), TaskStatus.NotSent));
//            }
//        };
//    }

    private Behaviour createTaskSenderTicker() {
        return new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                if (tasks.isEmpty()) {
                    return;
                }
                for (TaskWithStatus task : tasks) {
                    if (task == null) return;
                    else if (task.status != TaskStatus.NotSent) continue;
                    System.out.println(task.task.timeRequired);
                    var agentIndex = random.nextInt(regionalAgentNames.size());
                    var regionalAgent = regionalAgentNames.get(agentIndex);
                    var regionalAgentAID = new AID(regionalAgent, AID.ISLOCALNAME);
                    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                    message.addReceiver(regionalAgentAID);
                    message.setOntology("New task");
                    try {
                        message.setContent(Task.taskToString(task.task));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    myAgent.send(message);
                System.out.format("[%s] Sent task [id=%s] to [%s]!\nTask will take %s hours.\n",
                        myAgent.getName(), task.task.id, regionalAgentAID.getName(), task.task.timeRequired.toSeconds());
                    task.status = TaskStatus.Sent;
                }
            }
        };
    }

    /*private Behaviour createTaskCheckerTicker() {
        return new TickerBehaviour(this, 30000) {
            @Override
            protected void onTick() {
                System.out.format("System has completed %s out of %s tasks so far.\n", numberOfTasksCompleted, numberOfTasksTotal);
            }
        };
    }*/

    private void checkIfTasksCompleted()
    {
        //System.out.format("Is %s equal to %s?\n", numberOfTasksCompleted, numberOfTasksTotal);
        if(numberOfTasksTotal == numberOfTasksCompleted)
        {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM); // PROPAGATE?
            msg.addReceiver(new AID(scenarioAgentName, AID.ISLOCALNAME));
            msg.setLanguage("English");
            msg.setOntology("Scenario complete");
            msg.setContent("All tasks completed.");
            send(msg);
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}
