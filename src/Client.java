import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {


    public static void main(String[] args){
        String instruction = "- Commands you could issue:\n-> put key value\n-> del key\n-> get key";

        if(args.length != 2){
            System.out.println("Usage: java -Djava.sevurity.policy=client.policy Client hostName hostPort");
            System.exit(0);
        }

        try {
            Registry registry = LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1]));
            TPCNode server = (TPCNode) registry.lookup("2PCServer");
            Scanner scan = new Scanner(System.in);
            System.out.println("----------\nClient started!");
            System.out.println(instruction);
            String s;
            while (true){
                s = scan.nextLine();
                String[] input = s.split(" ");
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
                }else if(cmd.contentEquals("exit"))System.exit(0);
                else if(cmd.contentEquals("getAll")){
                    try {
                        String ret = server.getAll();
                        BufferedWriter bw = new BufferedWriter(new FileWriter(InetAddress.getLocalHost().getHostName() +"_dbSnapshot.txt",true));
                        bw.write(ret);
                        bw.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }
                else System.out.println(instruction);
            }
        }catch (RemoteException| NotBoundException e){
            e.printStackTrace();
        }

    }
}

