package ScenarioStructs;

import java.util.List;

public class RegionalAgentData {
    public List<ContainerAgentData> AgentsToCreate;
    public String RegionalAgentName;
    public RegionalAgentData(List<ContainerAgentData> agentsToCreate, String name)
    {
        AgentsToCreate = agentsToCreate;
        RegionalAgentName = name;
    }
}
