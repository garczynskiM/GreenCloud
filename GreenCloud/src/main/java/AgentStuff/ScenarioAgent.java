package AgentStuff;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.graphstream.graph.Graph;

public class ScenarioAgent extends Agent {
    Graph graph;

    @Override
    protected void setup() {
        Scenario scenarioToRealise = Scenario.createScenario1();
        Object[] args = getArguments();
        graph = (Graph)args[0];
        ContainerController cc = getContainerController();
        Object[] cloudArgs = new Object[2];
        cloudArgs[0] = scenarioToRealise.SystemInfo;
        cloudArgs[1] = graph;
        AgentController ac = null;
        try {
            ac = cc.createNewAgent("CloudAgent", "CloudAgent", cloudArgs);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
