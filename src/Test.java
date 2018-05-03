import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

public class Test {


    public void dbTest(){
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection("Zengs-Macbook.byod.gmu.edu");
//        dbOp.put("hello", "world");
//        dbOp.put("hope", "work");
//        System.out.println(dbOp.get("hello"));
//        System.out.println(dbOp.get("hope"));
//        dbOp.put("hope", "big");
//        System.out.println(dbOp.get("hope"));
//        dbOp.del("hello");
//        dbOp.del("hope");
//        System.out.println(dbOp.get("h"));
//        System.out.println(dbOp.get("hope"));
        dbOp.selectAll();
//        dbOp.closeConnection();
    }

    public void testInput(){
        Scanner scan = new Scanner(System.in);
        String s = scan.nextLine();
        System.out.println(s.length());
        LinkedList<String>  l = new LinkedList<>();
        String[] ss =s.split(" ");
        System.out.println(ss.length);
        for(int i=0; i<ss.length; i++){
            System.out.println(i + ": " + ss[i]);
        }
    }

    public static void main(String[] args){
        Test test = new Test();
        test.dbTest();
    }
}