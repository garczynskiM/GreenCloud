package AgentStuff;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

public class Task implements Serializable {
    public String id;
    public Duration timeRequired;
    public int cpuCoresRequired;
    public int ramRequired; // GB
    public LocalDateTime deadline;

    public Task(String id, Duration timeRequired, int cpuCoresRequired, int ramRequired, LocalDateTime deadline) {
        this.id = id;
        this.timeRequired = timeRequired;
        this.cpuCoresRequired = cpuCoresRequired;
        this.ramRequired = ramRequired;
        this.deadline = deadline;
    }

    public static String taskToString(Task task) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(task);
        final byte[] byteArray = bos.toByteArray();
        var result =  Base64.getEncoder().encodeToString(byteArray);
        os.close();

        return result;
    }

    public static Task stringToTask (String string) throws IOException, ClassNotFoundException {
        //System.out.println(string);
        //try
        //{
            final byte[] bytes = Base64.getDecoder().decode(string);
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream oInputStream = new ObjectInputStream(bis);
            Task task = (Task) oInputStream.readObject();
            oInputStream.close();

            return task;
        //}
        //catch(IllegalArgumentException e)
        //{
        //    System.out.println(string);
        //    e.printStackTrace();
        //}
        //return null;
    }
}
