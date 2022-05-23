import ScenarioStructs.ContainerAgentData;
import ScenarioStructs.RegionalAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.graphstream.graph.Graph;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;

public class RegionalAgent extends Agent{
    List<String> ContainerAgentNames;
    List<Task> TasksToDistribute;
    Graph Display;

    @Override
    protected void setup() {
        Object[] args = getArguments(); // arguments that are passed on agent creation, similar to UNIX
        RegionalAgentData initData = (RegionalAgentData)args[0];
        ContainerAgentNames = new ArrayList<>();
        Display = (Graph)args[1];
        Display.addNode("RegionalAgent " + getName());
        for (ContainerAgentData data: initData.AgentsToCreate) {
            ContainerController cc = getContainerController();
            Object[] containerArgs = new Object[2];
            containerArgs[0] = data;
            containerArgs[1] = Display;
            AgentController ac = null;
            try {
                ac = cc.createNewAgent(data.ContainerAgentName, "ContainerAgent", containerArgs);
                ac.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
            ContainerAgentNames.add(data.ContainerAgentName);
        }
    }
}
