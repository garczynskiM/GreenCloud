package AgentStuff;

import java.sql.Time;
import java.time.Duration;

public class Task {
    public Duration TimeRequired;
    public int CPUCoresRequired;
    public int RAMRequired;
    public Time Deadline;
    public Task(Duration timeRequired, int cpuCoresRequired, int ramRequired, Time deadline)
    {
        TimeRequired = timeRequired;
        CPUCoresRequired = cpuCoresRequired;
        RAMRequired = ramRequired;
        Deadline = deadline;
    }
}
