import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Calendar;
import java.util.List;

public class CloudAgent extends Agent {

    AID[] RegionalAgentAIDs;
    List<Task> Tasks;

    @Override
    protected void setup() {

    }
}
