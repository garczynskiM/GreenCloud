package ScenarioStructs;

import AgentStuff.Task;

public class TaskToDistribute {
    public Task Task;
    public int StartTime;
    public TaskToDistribute(Task task, int startTime)
    {
        Task = task;
        StartTime = startTime;
    }
}
