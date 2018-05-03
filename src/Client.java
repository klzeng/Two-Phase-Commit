import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {
    static String instruction = "- Commands you could issue:\n-> put key value\n-> del key\n-> get key";

    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("Usage: java -Djava.sevurity.policy=client.policy Client hostName hostPort");
            System.exit(0);
        }

        try {
            Registry registry = LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1]));
            TPCNode server = (TPCNode) registry.lookup("2PCServer");
            Scanner scan = new Scanner(System.in);
            System.out.println("----------\nClient started!");
            System.out.println(Client.instruction);
            while (true){
                String[] input = scan.nextLine().split(" ");
                String cmd = input[0];
                String value = null;
                if(cmd.contentEquals("put")){
                    if(input.length != 3){
                        System.out.println("Usage: put key value");
                        continue;
                    }
                    String key = input[1];
                    value = input[2];
                    server.put(key, value);
                }else if(cmd.contentEquals("del")){
                    String key = input[1];
                    server.del(key);
                }
                else if(cmd.contentEquals("get")){
                    String key = input[1];
                    System.out.println(server.get(key));
                }
                else if(cmd.contentEquals("sleep")) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else System.out.println(Client.instruction);
            }
        }catch (RemoteException| NotBoundException e){
            e.printStackTrace();
        }

    }
}

