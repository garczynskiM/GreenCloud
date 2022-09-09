package AgentStuff;

public class RegionalTaskWithStatus {
    public Task task;
    public TaskStatus status;
    public String conversationId;

    public RegionalTaskWithStatus() {
    }

    public RegionalTaskWithStatus(Task task, TaskStatus status, String conversationId) {
        this.task = task;
        this.status = status;
        this.conversationId = conversationId;
    }
}
