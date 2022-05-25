package AgentStuff;

import jade.core.AID;

import java.io.*;
import java.util.Base64;

public class ContainerProposal implements Serializable {
    public AID containerAID;
    public double goodness;

    public ContainerProposal(AID containerAID, double goodness) {
        this.containerAID = containerAID;
        this.goodness = goodness;
    }

    public static String proposalToString(ContainerProposal proposal) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(proposal);
        final byte[] byteArray = bos.toByteArray();
        var result =  Base64.getEncoder().encodeToString(byteArray);
        os.close();

        return result;
    }

    public static ContainerProposal stringToProposal (String string) throws IOException, ClassNotFoundException {
        final byte[] bytes = Base64.getDecoder().decode(string);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream oInputStream = new ObjectInputStream(bis);
        ContainerProposal proposal = (ContainerProposal) oInputStream.readObject();
        oInputStream.close();

        return proposal;
    }
}
