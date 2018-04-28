import java.sql.*;

public class DatabaseOp {
    String dbName = "jdbc:sqlite:cs675.db";
    Connection con = null;

    public void getConnection() {
        try{
            Class.forName("org.sqlite.JDBC");
            this.con = DriverManager.getConnection(this.dbName);
            System.out.println("Opened database successfully.");

            Statement  stmt = con.createStatement();
            String sql = "create table is not exists keyValue(id int primary not null, key char(18), value char(50))";
            stmt.executeUpdate(sql);
            stmt.close();
            con.commit();
        }catch (Exception e){
            System.out.println("Error connecting to database.");
            e.printStackTrace();
        }
    }

    public void put(int id, String key, String value){
        try{
            Statement  stmt = con.createStatement();
            String sql = "insert into keyvalue (key, value) values (" + key + ", "  + value + ");";
            stmt.executeUpdate(sql);
            stmt.close();
            con.commit();
        }catch (Exception e) {
            System.out.println("Error inserting " + key + "," + value);
            e.printStackTrace();
        }
    }

    public String get(int id, String key){
        String ret = null;
        

        return ret;
    }

}
