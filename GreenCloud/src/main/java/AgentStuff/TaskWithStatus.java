package AgentStuff;

public class TaskWithStatus {
    public Task task;
    public TaskStatus status;

    public TaskWithStatus() {
    }

    public TaskWithStatus(Task task, TaskStatus status) {
        this.task = task;
        this.status = status;
    }
}
