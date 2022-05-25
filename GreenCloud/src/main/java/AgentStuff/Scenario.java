package AgentStuff;

import ScenarioStructs.CloudAgentData;
import ScenarioStructs.ContainerAgentData;
import ScenarioStructs.RegionalAgentData;
import ScenarioStructs.TaskToDistribute;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Scenario
{
    public CloudAgentData SystemInfo;
    public List<TaskToDistribute> TasksToDistribute;

    public Scenario(CloudAgentData systemInfo, List<TaskToDistribute> tasksToDistribute)
    {
        SystemInfo = systemInfo;
        TasksToDistribute = tasksToDistribute;
    }
    static public Scenario createScenario1()
    {
        RegionalAgentData europeRegData = new RegionalAgentData(new ArrayList<>(), "EuropeManager");
        for(int i = 0; i < 2; i++)
        {
            europeRegData.AgentsToCreate.add(new ContainerAgentData("Europe" + Integer.toString(i + 1),
                    "null", "EuropeManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32));
        }
        RegionalAgentData americaRegData = new RegionalAgentData(new ArrayList<>(), "AmericaManager");
        for(int i = 0; i < 2; i++)
        {
            americaRegData.AgentsToCreate.add(new ContainerAgentData("America" + Integer.toString(i + 1),
                    "null","AmericaManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32));
        }
        List<RegionalAgentData> cloudInfo = new ArrayList<>();
        cloudInfo.add(europeRegData);
        cloudInfo.add(americaRegData);
        CloudAgentData systemInfo = new CloudAgentData(cloudInfo);
        List<TaskToDistribute> tasksToDistribute = new ArrayList<>();
        return new Scenario(systemInfo, tasksToDistribute);
    }
}
