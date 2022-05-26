package AgentStuff;

import ScenarioStructs.ContainerAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.lang.ref.Reference;
import java.sql.Time;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;

public class ContainerAgent extends Agent {
    Time timeElapsed  = new Time(0);
    int secondsElapsed = 0;
    String ForecastAgentName;
    String RegionalAgentLocalName;
    String RegionalAgentName;
    Duration ConnectionTime;
    double BandwidthInMB;
    double RAMInGB;
    double MaxEnergyUsage;
    double MaxEnergyProduction;
    double CurrentEnergyProduction;
    int CPUCores;
    Graph Display;
    WeatherForecast weatherForecast;

    private void initialNodeStyle()
    {
        Node node = Display.getNode(getLocalName());
        node.setAttribute("ui.style", "fill-color: rgb(128,128,128);size: 15px;" +
                "text-alignment: under;");
        node.setAttribute("ui.label", getLocalName());
    }

    @Override
    protected void setup() {
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        ContainerAgentData initData = (ContainerAgentData)args[0];
        ForecastAgentName = initData.ForecastAgentName;
        RegionalAgentName = (String)args[2];
        RegionalAgentLocalName = (String)args[3];
        ConnectionTime = initData.ConnectionTime;
        BandwidthInMB = initData.BandwidthInMB;
        RAMInGB = initData.RAMInGB;
        MaxEnergyUsage = initData.MaxEnergyUsage;
        MaxEnergyProduction = initData.MaxEnergyProduction;
        CPUCores = initData.CPUCores;
        Display = (Graph)args[1];
        Display.addNode(getLocalName());
        initialNodeStyle();
        Display.addEdge(RegionalAgentLocalName + " " + getLocalName(), RegionalAgentLocalName, getLocalName());
        addBehaviour(createCyclicSystemStartupManager());

        weatherForecast = new WeatherForecast(5);
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

    //main heuristic method -> returns how possible it is to finish given task
    private double check_weather_heuristic(Task task) {
        int task_hours = (int) task.TimeRequired.toHours();
        if(weatherForecast.forecast_list.size() < task_hours)
            weatherForecast.expand_forecast(task_hours - weatherForecast.forecast_list.size());
        //in our heuristic the more the better -> result. Regional agent should choose container with best result
        double result = 0.0;
        for(int i=0; i<=task_hours;i++){
            result += weather_resource_to_energy_ratio(task.RAMRequired,
                        weatherForecast.weather_status.get(weatherForecast.forecast_list.get(i)),
                    "RAM");
            result += weather_resource_to_energy_ratio(task.CPUCoresRequired,
                    weatherForecast.weather_status.get(weatherForecast.forecast_list.get(i)),
                    "CPU");
        }
        return result;
    }

    private double weather_resource_to_energy_ratio(double resource_required,
                                                    double energy_ratio, String resource_type) {
        double energy_production = energy_ratio * MaxEnergyProduction; //weather energy production
        //ratio >= 1 -> enough energy to power all resources; else not all resources are available
        double energy_usage_ratio = energy_production /MaxEnergyUsage;
        if(energy_usage_ratio>1.0) energy_usage_ratio = 1.0;
        if(resource_type == "RAM") {
            //ratio of available resources to resources required
            //in our heuristic the more the better (cannot be more than 1).
            //might happen than task is too big for container -> too much resources.
            // Then the result will raise only slightly
            double result = energy_usage_ratio * RAMInGB / resource_required;
            return result <= 1.0 ? result : 1.0;
        }
        else if(resource_type == "CPU") {
            double result = energy_usage_ratio * CPUCores / resource_required;
            return result <= 1.0 ? result : 1.0;
        }
        return -1;
    }
}
