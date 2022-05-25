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
}
