package AgentStuff;

import java.time.LocalDateTime;

public class OngoingTask {
    public Task task;
    public LocalDateTime startTime;
    public boolean completed;

    public OngoingTask(Task task, LocalDateTime startTime) {
        this.task = task;
        this.startTime = startTime;
        this.completed = false;
    }
}
