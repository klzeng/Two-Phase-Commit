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
        String s = " ";

    }

    public static void main(String[] args){
        Test test = new Test();
        test.testInput();
    }
}