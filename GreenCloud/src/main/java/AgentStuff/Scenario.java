package AgentStuff;

import ScenarioStructs.CloudAgentData;
import ScenarioStructs.TaskToDistribute;

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
        CloudAgentData systemInfo = null;
        List<TaskToDistribute> tasksToDistribute = new ArrayList<>();
        return new Scenario(systemInfo, tasksToDistribute);
    }
}
