package ScenarioStructs;

import java.util.List;

public class CloudAgentData {

    public List<RegionalAgentData> AgentsToCreate;
    public CloudAgentData(List<RegionalAgentData> agentsToCreate)
    {
        AgentsToCreate = agentsToCreate;
    }
}
