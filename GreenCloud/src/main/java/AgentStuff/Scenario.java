package AgentStuff;

import FIPA.DateTime;
import ScenarioStructs.CloudAgentData;
import ScenarioStructs.ContainerAgentData;
import ScenarioStructs.RegionalAgentData;
import ScenarioStructs.TaskToDistribute;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Scenario
{
    public CloudAgentData SystemInfo;
    public List<TaskToDistribute> TasksToDistribute;
    public LocalDateTime ScenarioStart;

    public Scenario(CloudAgentData systemInfo, List<TaskToDistribute> tasksToDistribute, LocalDateTime scenarioStart)
    {
        SystemInfo = systemInfo;
        TasksToDistribute = tasksToDistribute;
        ScenarioStart = scenarioStart;
    }
    static public Scenario randomScenario()
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
        return new Scenario(systemInfo, tasksToDistribute, LocalDateTime.now());
    }
    static public Scenario conflictingTaskScenario() // one task should be done by regionalAgent
    // because the deadlines won't fit
    {
        int numberOfTasks = 4;
        RegionalAgentData europeRegData = new RegionalAgentData(new ArrayList<>(), "EuropeManager");
        europeRegData.AgentsToCreate.add(new ContainerAgentData("Europe1",
                "null", "EuropeManager", Duration.ofMillis(250), 1000,
                64, 100,200, 32));
        List<RegionalAgentData> cloudInfo = new ArrayList<>();
        cloudInfo.add(europeRegData);
        CloudAgentData systemInfo = new CloudAgentData(cloudInfo);
        List<TaskToDistribute> tasksToDistribute = new ArrayList<>();
        LocalDateTime startOfCreation = LocalDateTime.now();
        for(int i = 0; i < numberOfTasks; i++)
        {
            tasksToDistribute.add(new TaskToDistribute(new Task("Task" + Integer.toString(i + 1),
                    Duration.ofSeconds(4),32, 64, startOfCreation.plusSeconds(6*(i + 1))),
                    i*6));
        }
        tasksToDistribute.add(new TaskToDistribute(new Task("TaskToDoByRegional",
                Duration.ofSeconds(4),32, 64, startOfCreation.plusSeconds(6*numberOfTasks)),
                (numberOfTasks - 1)*6));
        tasksToDistribute.add(new TaskToDistribute(new Task("TaskAfterConflict",
                Duration.ofSeconds(4),32, 64, startOfCreation.plusSeconds(6*(numberOfTasks + 1))),
                numberOfTasks*6));

        return new Scenario(systemInfo, tasksToDistribute, startOfCreation);
    }
    static public Scenario doubleTaskScenario() // All tasks should be done by the container, because he can do multiple
            // tasks at once
    {
        int numberOfTasks = 4;
        RegionalAgentData europeRegData = new RegionalAgentData(new ArrayList<>(), "EuropeManager");
        europeRegData.AgentsToCreate.add(new ContainerAgentData("Europe1",
                "null", "EuropeManager", Duration.ofMillis(250), 1000,
                256, 100,200, 128));
        List<RegionalAgentData> cloudInfo = new ArrayList<>();
        cloudInfo.add(europeRegData);
        CloudAgentData systemInfo = new CloudAgentData(cloudInfo);
        List<TaskToDistribute> tasksToDistribute = new ArrayList<>();
        LocalDateTime startOfCreation = LocalDateTime.now();
        for(int i = 0; i < numberOfTasks; i++)
        {
            tasksToDistribute.add(new TaskToDistribute(new Task("Task" + Integer.toString(i + 1),
                    Duration.ofSeconds(4),32, 64, startOfCreation.plusSeconds(6*(i + 1))),
                    i*6));
        }
        tasksToDistribute.add(new TaskToDistribute(new Task("TaskToDoByRegional",
                Duration.ofSeconds(4),32, 64, startOfCreation.plusSeconds(6*numberOfTasks)),
                (numberOfTasks - 1)*6));

        return new Scenario(systemInfo, tasksToDistribute, startOfCreation);
    }
    static public Scenario TenTasksScenario()
    {
        RegionalAgentData europeRegData = new RegionalAgentData(new ArrayList<>(), "EuropeManager");
        for(int i = 0; i < 2; i++)
        {
            europeRegData.AgentsToCreate.add(new ContainerAgentData("Europe" + Integer.toString(i + 1),
                    "null", "EuropeManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32, 100 + i));
        }
        RegionalAgentData americaRegData = new RegionalAgentData(new ArrayList<>(), "AmericaManager");
        for(int i = 0; i < 2; i++)
        {
            americaRegData.AgentsToCreate.add(new ContainerAgentData("America" + Integer.toString(i + 1),
                    "null","AmericaManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32, 102 + i));
        }
        List<RegionalAgentData> cloudInfo = new ArrayList<>();
        cloudInfo.add(europeRegData);
        cloudInfo.add(americaRegData);
        CloudAgentData systemInfo = new CloudAgentData(cloudInfo);
        List<TaskToDistribute> tasksToDistribute = new ArrayList<>();

        LocalDateTime startOfCreation = LocalDateTime.now();
        for(int i = 0; i < 7; i++)
        {
            tasksToDistribute.add(new TaskToDistribute(new Task("Task" + Integer.toString(i + 1),
                    Duration.ofSeconds(4),16, 32, startOfCreation.plusSeconds(300)),
                    0));
        }
        for(int i = 7; i < 10; i++)
        {
            tasksToDistribute.add(new TaskToDistribute(new Task("Task" + Integer.toString(i + 1),
                    Duration.ofSeconds(4),32, 64, startOfCreation.plusSeconds(300)),
                    0));
        }
        return new Scenario(systemInfo, tasksToDistribute, LocalDateTime.now());
    }
    static public Scenario HundredTasksScenario()
    {
        RegionalAgentData europeRegData = new RegionalAgentData(new ArrayList<>(), "EuropeManager");
        for(int i = 0; i < 4; i++)
        {
            europeRegData.AgentsToCreate.add(new ContainerAgentData("Europe" + Integer.toString(i + 1),
                    "null", "EuropeManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32, 100 + i));
        }
        RegionalAgentData americaRegData = new RegionalAgentData(new ArrayList<>(), "AmericaManager");
        for(int i = 0; i < 4; i++)
        {
            americaRegData.AgentsToCreate.add(new ContainerAgentData("America" + Integer.toString(i + 1),
                    "null","AmericaManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32, 100 + i));
        }
        List<RegionalAgentData> cloudInfo = new ArrayList<>();
        cloudInfo.add(europeRegData);
        cloudInfo.add(americaRegData);
        CloudAgentData systemInfo = new CloudAgentData(cloudInfo);
        List<TaskToDistribute> tasksToDistribute = new ArrayList<>();

        LocalDateTime startOfCreation = LocalDateTime.now();
        for(int i = 0; i < 70; i++)
        {
            tasksToDistribute.add(new TaskToDistribute(new Task("Task" + Integer.toString(i + 1),
                    Duration.ofSeconds(4),16, 32, startOfCreation.plusSeconds(300)),
                    0));
        }
        for(int i = 70; i < 100; i++)
        {
            tasksToDistribute.add(new TaskToDistribute(new Task("Task" + Integer.toString(i + 1),
                    Duration.ofSeconds(4),32, 64, startOfCreation.plusSeconds(300)),
                    0));
        }
        return new Scenario(systemInfo, tasksToDistribute, LocalDateTime.now());
    }
    static public Scenario SixtySequentialTasksScenario()
    {
        RegionalAgentData europeRegData = new RegionalAgentData(new ArrayList<>(), "EuropeManager");
        for(int i = 0; i < 3; i++)
        {
            europeRegData.AgentsToCreate.add(new ContainerAgentData("Europe" + Integer.toString(i + 1),
                    "null", "EuropeManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32, 100 + i));
        }
        RegionalAgentData americaRegData = new RegionalAgentData(new ArrayList<>(), "AmericaManager");
        for(int i = 0; i < 3; i++)
        {
            americaRegData.AgentsToCreate.add(new ContainerAgentData("America" + Integer.toString(i + 1),
                    "null","AmericaManager", Duration.ofMillis(250), 1000,
                    64, 100,200, 32, 103 + i));
        }
        List<RegionalAgentData> cloudInfo = new ArrayList<>();
        cloudInfo.add(europeRegData);
        cloudInfo.add(americaRegData);
        CloudAgentData systemInfo = new CloudAgentData(cloudInfo);
        List<TaskToDistribute> tasksToDistribute = new ArrayList<>();

        LocalDateTime startOfCreation = LocalDateTime.now();
        for(int i = 0; i < 60; i++)
        {
            tasksToDistribute.add(new TaskToDistribute(new Task("Task" + Integer.toString(i + 1),
                    Duration.ofSeconds(4),16, 32, startOfCreation.plusSeconds(300)),
                    i));
        }
        return new Scenario(systemInfo, tasksToDistribute, LocalDateTime.now());
    }
    public void updateTaskDeadline()
    {
        LocalDateTime rightNow = LocalDateTime.now();
        for (TaskToDistribute taskToDistribute : this.TasksToDistribute) {
            LocalDateTime tempTaskDeadline = taskToDistribute.Task.deadline;
            LocalDateTime ScenarioStartCopy = this.ScenarioStart;

            long hours = ScenarioStartCopy.until(tempTaskDeadline, ChronoUnit.HOURS);
            ScenarioStartCopy = ScenarioStartCopy.plusHours(hours);
            long minutes = ScenarioStartCopy.until(tempTaskDeadline, ChronoUnit.MINUTES);
            ScenarioStartCopy = ScenarioStartCopy.plusMinutes(minutes);
            long seconds = ScenarioStartCopy.until(tempTaskDeadline, ChronoUnit.SECONDS);

            taskToDistribute.Task.deadline = rightNow;
            taskToDistribute.Task.deadline = taskToDistribute.Task.deadline.plusSeconds(seconds);
            taskToDistribute.Task.deadline = taskToDistribute.Task.deadline.plusMinutes(minutes);
            taskToDistribute.Task.deadline = taskToDistribute.Task.deadline.plusHours(hours);
        }
    }
}
