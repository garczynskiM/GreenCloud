package AgentStuff;

import ScenarioStructs.ContainerAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.graphstream.graph.Graph;

import java.lang.ref.Reference;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;

public class ContainerAgent extends Agent {

    String ForecastAgentName;
    String RegionalAgentName;
    Duration ConnectionTime;
    double BandwidthInMB;
    double RAMInGB;
    double MaxEnergyUsage;
    double MaxEnergyProduction;
    double CurrentEnergyProduction;
    int CPUCores;
    Graph Display;

    @Override
    protected void setup() {
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        ContainerAgentData initData = (ContainerAgentData)args[0];
        ForecastAgentName = initData.ForecastAgentName;
        RegionalAgentName = initData.RegionalAgentName;
        ConnectionTime = initData.ConnectionTime;
        BandwidthInMB = initData.BandwidthInMB;
        RAMInGB = initData.RAMInGB;
        MaxEnergyUsage = initData.MaxEnergyUsage;
        MaxEnergyProduction = initData.MaxEnergyProduction;
        CPUCores = initData.CPUCores;
        Display = (Graph)args[1];
        Display.addNode("ContainerAgent " + getName());
    }
}
