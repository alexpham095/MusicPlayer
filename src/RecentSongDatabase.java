import com.mpatric.mp3agic.*;

import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;

public class RecentSongDatabase {
    private static String dbURL = "jdbc:derby:recentSongDB;create=true";
    private static String tableName = "RecentSongs";
    // jdbc Connection
    private static Connection conn = null;
    private static Statement stmt = null;

    protected static void createConnection()
    {
        try
        {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            //Get a connection
            conn = DriverManager.getConnection(dbURL);

        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
    }

    protected static void buildTable() {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + tableName + " (SongName CHAR(150), " + "SongPath CLOB(300)"  + ")");
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    protected static Vector<String> buildNamesList(){
        Vector<String> namesList = new Vector<String>();
        try {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from " + tableName);
            while(results.next())
            {
                namesList.add(results.getString(1));
            }
            results.close();
            stmt.close();
        } catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return namesList;
    }

    protected static int getSize(){
        try {
            stmt = conn.createStatement();
            String queryCheck = "SELECT count(*) from " + tableName;
            ResultSet resultSet = stmt.executeQuery(queryCheck);
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
            stmt.close();
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return 0;
    }

    //add a song to the recent List
    protected static String addSong(String name, String songPathToAdd){
        String songName = name;
        try {
            int size = 0;
            stmt = conn.createStatement();
            String queryCheck = "SELECT count(*) from " + tableName;
            ResultSet resultSet = stmt.executeQuery(queryCheck);
            while (resultSet.next()) {
                size = resultSet.getInt(1);
            }

            if(size == 10){
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                ResultSet results = stmt.executeQuery("select * from " + tableName);
                results.next();
                System.out.println(results.getString(1));
                results.deleteRow();
                //PreparedStatement st = conn.prepareStatement("DELETE FROM " + tableName + " WHERE COUNT = 1");
                //st.executeUpdate();
            }

            PreparedStatement statement = conn.prepareStatement("INSERT INTO " + tableName + " values (?,?)");
            statement.setString(1, name);
            statement.setString(2, songPathToAdd);
            statement.executeUpdate();
            stmt.close();

        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return songName;
    }

    protected static void shutdown()
    {
        try
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (conn != null)
            {
                DriverManager.getConnection(dbURL + ";shutdown=true");
                conn.close();
            }
        }
        catch (SQLException sqlExcept)
        {

        }

    }

    protected static boolean hasTable(){
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(conn.getCatalog(), "APP", "RECENTSONGS", new String[] {"TABLE"});
            stmt = conn.createStatement();
            if (!tables.next()) {
                return false;
            } else {
                return true;
            }
        }
        catch( SQLException sqlExcept){
            sqlExcept.printStackTrace();
            return false;
        }
    }
}
