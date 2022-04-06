import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.time.Duration;
import java.util.Calendar;
import java.util.List;

public class ContainerAgent extends Agent {

    AID ForecastAgentAID;
    AID RegionalAgentAID;
    Duration ConnectionTime;
    double BandwidthInMB;
    double RAMInGB;
    double MaxEnergyUsage;
    double MaxEnergyProduction;
    double CurrentEnergyProduction;
    int CPUCores;

    @Override
    protected void setup() {
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        // Should we create WeatherForecastAgent here, or create on program startup and pass AID here?
    }
}
