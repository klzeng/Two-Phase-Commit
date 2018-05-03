import java.rmi.RemoteException;
import java.util.Scanner;

public class Coordinator extends TPCServer{

    public Coordinator(int portNum){
        super(portNum);

        System.out.println("Please enter the replica host(boot them up firstly):");
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
        System.out.println("--------\ntrying to connecting to participants.\n");
        for(String each: input.split(" ")){
            each = each.replace(" ", "");
            if(each.length() != 0){
                this.participants.add(each);
            }
        }

        // handshaking with the participants, sending them all the participants info
        for(int i=0; i < this.participants.size(); i++){
            String ptp = this.participants.get(i);
            System.out.println("contacting participant: " + ptp);

            StringBuilder hosts = new StringBuilder(this.hostName + ":" + this.RMIPort);
            for(int j=0; j< this.participants.size(); j++){
                if(j == i) continue;
                hosts.append(" " + this.participants.get(j));
            }

            TPCNode server = this.getRMIObect(ptp);
            try{
                server.handShaking(hosts.toString());
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        // need to set up its coordinator to itself
        this.coordinator = this.hostName + ":" + this.RMIPort;

        System.out.println("--------\nCoordinator ready!");
    }

    public static void main(String[] args){

        if(args.length != 1){
            System.out.println("Usage: java -Djava.security.policy=server.policy Coordinator portNumber");
        }

        int portNumber;

        try{
            portNumber = Integer.parseInt(args[0]);
            Coordinator coordnt = new Coordinator(portNumber);
        }catch (NumberFormatException e){
            System.out.println("Argument must be an integer.");
            System.exit(0);
        }
    }
}
