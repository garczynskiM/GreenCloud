import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.util.Display;

import java.lang.ref.Reference;

public class Main {
    public static void main(String[] args)
    {
        System.setProperty("org.graphstream.ui", "swing");
        Graph graph = new SingleGraph("System");

        /*graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addEdge("AB", "A", "B");
        graph.addEdge("BC", "B", "C");
        graph.addEdge("CA", "C", "A");*/

        graph.display();

        graph.display();

        Object[] arguments = new Object[1];
        arguments[0] = graph;
        ContainerController myContainer;

        Runtime myRuntime = Runtime.instance();

        // prepare the settings for the platform that we're going to start
        Profile myProfile = new ProfileImpl();

        // create the main container
        myContainer = myRuntime.createMainContainer(myProfile);
        try {
            AgentController rma = myContainer.createNewAgent(
                    "rma", "jade.tools.rma.rma", null);
            myContainer.createNewAgent("Cloud", "CloudAgent", arguments);
            rma.start();
        } catch(StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
