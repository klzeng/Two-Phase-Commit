import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;

public class Test {


    public void dbTest(String dbname){
        DatabaseOp dbOp = new DatabaseOp();
        dbOp.getConnection(dbname);
        dbOp.selectAll();
        dbOp.closeConnection();
    }

    public void testInput(){
        Scanner scan = new Scanner(System.in);
        String s = scan.nextLine();
        String[] ss =s.split(" ");
        System.out.println(ss.length);
        for(int i=0; i<ss.length; i++){
            System.out.println(i + ": " + ss[i]);
        }
    }

    public static void main(String[] args){
        Test test = new Test();
        test.testInput();
    }
}