package AgentStuff;

import ScenarioStructs.ContainerAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.io.IOException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ContainerAgent extends Agent {
    Time timeElapsed  = new Time(0);
    int secondsElapsed = 0;
    WeatherForecast weatherForecast;
    String forecastAgentName;
    String regionalAgentLocalName;
    String regionalAgentName;
    Duration connectionTime;
    double bandwidthInMB;
    double ramInGB;
    double currentlyUsedRam;
    double maxEnergyUsage;
    double maxEnergyProduction;
    double currentEnergyProduction;
    int cpuCores;
    int currentlyUsedCPU;
    Graph display;
    Map<String, Task> tasksToAcceptByRegional;
    List<OngoingTask> ongoingTasks;

    private void initialNodeStyle()
    {
        Node node = display.getNode(getLocalName());
        node.setAttribute("ui.style", "fill-color: rgb(128,128,128);size: 15px;" +
                "text-alignment: under;");
        node.setAttribute("ui.label", getLocalName());
    }

    @Override
    protected void setup() {
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        ContainerAgentData initData = (ContainerAgentData)args[0];
        tasksToAcceptByRegional = new HashMap<>();
        ongoingTasks = new ArrayList<>();
        forecastAgentName = initData.ForecastAgentName;
        regionalAgentName = (String)args[2];
        regionalAgentLocalName = (String)args[3];
        connectionTime = initData.ConnectionTime;
        bandwidthInMB = initData.BandwidthInMB;
        ramInGB = initData.RAMInGB;
        currentlyUsedRam = 0;
        maxEnergyUsage = initData.MaxEnergyUsage;
        maxEnergyProduction = initData.MaxEnergyProduction;
        cpuCores = initData.CPUCores;
        currentlyUsedCPU = 0;
        display = (Graph)args[1];
        display.addNode(getLocalName());
        initialNodeStyle();
        display.addEdge(regionalAgentLocalName + " " + getLocalName(), regionalAgentLocalName, getLocalName());
        addBehaviour(createCyclicSystemStartupManager());
        weatherForecast = new WeatherForecast(5);
        addBehaviour(createTaskReceiverCyclic());
        addBehaviour(createTaskCompleteCheckerTicker());
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
                            addBehaviour(createTickerTimeMeasurement());
                            break;
                    }
                }
                block();
            }
        };
    }
    private Behaviour createTickerTimeMeasurement()
    {
        return new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                secondsElapsed++;
                timeElapsed.setTime(secondsElapsed * 1000L);
                //System.out.println(getLocalName() + " - " + timeElapsed);
                weatherForecast.hour_passed_weather_update();
                if(weatherForecast.forecast_list.size() < 5)
                    weatherForecast.expand_forecast(5 - weatherForecast.forecast_list.size());
                currentEnergyProduction = Math.min(weatherForecast.current_weather_factor * maxEnergyProduction,
                        maxEnergyUsage);
            }
        };
    }

    private Behaviour createTaskReceiverCyclic() {
        return new CyclicBehaviour() {
            @Override
            public void action() {
                var mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                var msg = myAgent.receive(mt);
                if (msg != null) {
                    try {
                        var task = Task.stringToTask(msg.getContent());
                        var goodness = check_weather_heuristic(task);
                        var reply = msg.createReply();
                        reply.setConversationId(msg.getConversationId());
                        if (goodness <= 0.0) {
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent("Cannot do the task!");
                            System.out.format("[%s] Cannot do the task!\n", myAgent.getName());
                        }
                        else {
                            tasksToAcceptByRegional.put(msg.getConversationId(), task);
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent(ContainerProposal.proposalToString(new ContainerProposal(myAgent.getAID(),
                                    goodness)));
                            System.out.format("[%s] Sending Proposal [goodness=%f,task_id=%s]\n", myAgent.getName(),
                                    goodness, task.id);
                        }
                        myAgent.send(reply);
                        myAgent.addBehaviour(createDecisionReceiverSimple());
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    block();
                }
            }
        };
    }

    private Behaviour createDecisionReceiverSimple() {
        return new SimpleBehaviour() {
            private boolean finished = false;
            @Override
            public void action() {
                var mt = MessageTemplate.or(MessageTemplate.MatchPerformative(
                        ACLMessage.ACCEPT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL));
                var msg = myAgent.receive(mt);
                if (msg != null) {
                    var conversationId = msg.getConversationId();
                    var task = tasksToAcceptByRegional.remove(conversationId);
                    if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        if (currentlyUsedCPU + task.cpuCoresRequired > cpuCores || currentlyUsedRam + task.ramRequired > ramInGB) {
                            var failureMessage = new ACLMessage(ACLMessage.FAILURE);
                            try {
                                failureMessage.setContent(Task.taskToString(task));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            failureMessage.addReceiver(new AID(regionalAgentLocalName, AID.ISLOCALNAME));
                            myAgent.send(failureMessage);
                            System.out.format("Sending task [id=%s] back to [%s] due to insufficient resources to start execution.\n",
                                    regionalAgentName, task.id);
                        } else {
                            ongoingTasks.add(new OngoingTask(task, LocalDateTime.now()));
                            currentlyUsedCPU += task.cpuCoresRequired;
                            currentlyUsedRam += task.ramRequired;
                            System.out.format("[%s] Starting doing task: [id=%s]!\n", myAgent.getName(), task.id);
                        }
                    }
                    finished = true;
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

    private Behaviour createTaskCompleteCheckerTicker() {
        return new TickerBehaviour(this, 500) {
            @Override
            protected void onTick() {
                if (ongoingTasks.isEmpty()) {
                    return;
                }
                double currentlyAvailableRAM = currentEnergyProduction/maxEnergyUsage * ramInGB;
                int currentlyAvailableCPU = (int) (currentEnergyProduction/maxEnergyUsage * cpuCores);
                ongoingTasks.sort(new SortByRemainingDuration());
                for (OngoingTask ongoingTask : ongoingTasks) {
                    if (Duration.between(ongoingTask.startTime, LocalDateTime.now()).toMillis() >=
                            ongoingTask.task.timeRequired.toMillis()) {
                        ongoingTask.completed = true;
                        var completionMessage = new ACLMessage(ACLMessage.INFORM);
                        //cfp.setConversationId(conversationId);
                        completionMessage.setOntology("Task completed");
                        completionMessage.addReceiver(new AID(regionalAgentLocalName, AID.ISLOCALNAME));
                        try {
                            completionMessage.setContent(Task.taskToString(ongoingTask.task));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        myAgent.send(completionMessage);
                        System.out.format("[%s] Completed task: [id=%s]!\n", myAgent.getName(), ongoingTask.task.id);
                        currentlyUsedRam -= ongoingTask.task.ramRequired;
                        currentlyUsedCPU -= ongoingTask.task.cpuCoresRequired;
                    }
                    else
                    {
                        if(currentlyAvailableCPU < currentlyUsedCPU || currentlyAvailableRAM < currentlyUsedRam)
                        {
                            System.out.format("[%s] - can't complete task [%s] because weather is [%s]\n", myAgent.getName(),
                                    ongoingTask.task.id, weatherForecast.forecast_list.get(0));
                            var failureMessage = new ACLMessage(ACLMessage.FAILURE);
                            //cfp.setConversationId(conversationId);
                            failureMessage.addReceiver(new AID(regionalAgentLocalName, AID.ISLOCALNAME));
                            try {
                                failureMessage.setContent(Task.taskToString(ongoingTask.task));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            myAgent.send(failureMessage);
                            System.out.format("Sending task back to [%s].\n", regionalAgentName);
                            ongoingTask.completed = true;
                            currentlyUsedRam -= ongoingTask.task.ramRequired;
                            currentlyUsedCPU -= ongoingTask.task.cpuCoresRequired;
                        }
                    }
                }
                ongoingTasks.removeIf(task -> task.completed);
            }
        };
    }

    //main heuristic method -> returns how possible it is to finish given task
    private double check_weather_heuristic(Task task) {
        int task_hours = (int) task.timeRequired.toHours();
        if(weatherForecast.forecast_list.size() < task_hours)
            weatherForecast.expand_forecast(task_hours - weatherForecast.forecast_list.size());
        //in our heuristic the more the better -> result. Regional agent should choose container with best result
        double result = 0.0;
        for(int i=0; i<=task_hours;i++){
            result += weather_resource_to_energy_ratio(task.ramRequired,
                        weatherForecast.weather_status.get(weatherForecast.forecast_list.get(i)),
                    "RAM");
            result += weather_resource_to_energy_ratio(task.cpuCoresRequired,
                    weatherForecast.weather_status.get(weatherForecast.forecast_list.get(i)),
                    "CPU");
        }
        return result;
    }

    private double weather_resource_to_energy_ratio(double resource_required,
                                                    double energy_ratio, String resource_type) {
        double energy_production = energy_ratio * maxEnergyProduction; //weather energy production
        //ratio >= 1 -> enough energy to power all resources; else not all resources are available
        double energy_usage_ratio = energy_production / maxEnergyUsage;
        if(energy_usage_ratio>1.0) energy_usage_ratio = 1.0;
        if(Objects.equals(resource_type, "RAM")) {
            //ratio of available resources to resources required
            //in our heuristic the more the better (cannot be more than 1).
            //might happen than task is too big for container -> too much resources.
            // Then the result will raise only slightly
            double result = energy_usage_ratio * ramInGB / resource_required;
            return Math.min(result, 1.0);
        }
        else if(Objects.equals(resource_type, "CPU")) {
            double result = energy_usage_ratio * cpuCores / resource_required;
            return Math.min(result, 1.0);
        }
        return -1;
    }

    static class SortByRemainingDuration implements Comparator<OngoingTask>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(OngoingTask a, OngoingTask b)
        {
            var remainingDuration_a = a.task.timeRequired.toMillis() -
                    Duration.between(a.startTime, LocalDateTime.now()).toMillis();
            var remainingDuration_b = b.task.timeRequired.toMillis() -
                    Duration.between(b.startTime, LocalDateTime.now()).toMillis();
            return (int) (remainingDuration_a - remainingDuration_b);
        }
    }
}
