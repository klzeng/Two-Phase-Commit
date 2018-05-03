import java.sql.*;

public class DatabaseOp {
    String dbName = "jdbc:sqlite:";
    Connection con = null;

    public void getConnection(String daName) {
        try{
            Class.forName("org.sqlite.JDBC");
            this.dbName = this.dbName + daName + ".db";
            this.con = DriverManager.getConnection(this.dbName);
            this.con.setAutoCommit(false);
            System.out.println("Opened database "+ this.dbName +  " successfully.");

            Statement  stmt = con.createStatement();
            String sql = "create table if not exists keyValue (keyString char(18) primary key not null, valueString char(50))";
            stmt.executeUpdate(sql);
            stmt.close();
            con.commit();
        }catch (Exception e){
            System.out.println("Error connecting to database.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public int put(String key, String value){
        try{
            Statement  stmt = this.con.createStatement();
            String exist = this.get(key);
            String sql = null;
            if(exist == null){
                sql = "insert into keyvalue (keyString, valueString) values (\"" + key + "\", \""  + value + "\");";
            }else {
                sql = "update keyValue set valueString = \"" + value + "\" " + "where keyString = \"" + key + "\"";
            }
            stmt.executeUpdate(sql);
            stmt.close();
            con.commit();
            System.out.println("DB OPRATION: put " + key + " " + value +" successfully!" );
            return 0;
        }catch (Exception e) {
            System.out.println("Error inserting \"" + key + "\",\"" + value);
            e.printStackTrace();
            return 1;
        }
    }

    public int del(String key){
        try {
            Statement stmt = this.con.createStatement();
            String exist = this.get(key);
            String sql = null;
            if(exist != null){
                sql = "delete from keyValue where keyString = \"" + key + "\"";
                stmt.executeUpdate(sql);
                stmt.close();
                this.con.commit();
            }
            System.out.println("DB OPRATION: del " + key +" successfully!" );
            return 0;
        }catch (Exception e){
            System.out.println("Error deleting " + key);
            e.printStackTrace();
            return 1;
        }
    }

    public String get(String key){
        String ret = null;
        try{
            Statement stmt = this.con.createStatement();
            String sql = "select * from keyValue where keyString = " + "\"" + key +"\"";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) ret = rs.getString("valueString");
            rs.close();
            stmt.close();
        }catch (Exception e){
            System.out.println("Error getting " + key);
            e.printStackTrace();
        }

        return ret;
    }

    public void selectAll(){
        try{
            Statement stmt = this.con.createStatement();
            String sql = "select * from keyValue;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                System.out.println(rs.getString("keyString") + " : " + rs.getString("valueString"));
            }
            rs.close();
            stmt.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try{
            this.con.close();
        }catch (Exception e){
            System.out.println("Error closing database.");
            e.printStackTrace();
        }
    }

}
