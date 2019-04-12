import com.mpatric.mp3agic.*;

import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;

public class PlaylistDatabase {
    private static String dbURL = "jdbc:derby:playlistDB;create=true";
    private static String tableName = "Playlists";
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

    protected static void buildTable()
    {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + tableName + " (Playlist CHAR(150), " + "SongIndexes CHAR(150)" + ")");
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    protected static Vector<String> buildPlaylistTree()
    {
        Vector<String> output = new Vector<String>();

        try
        {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from " + tableName);



            while(results.next())
            {
                output.add(results.getString(1));
            }
            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return output;
    }

    protected static int getSize(){
        try {
            String queryCheck = "SELECT count(*) from PLAYLISTS ";
            ResultSet resultSet = stmt.executeQuery(queryCheck);
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return 0;
    }

    protected static boolean insertPlaylist(String name, String indexes){
        try {
            String queryCheck = "SELECT count(*) from PLAYLISTS WHERE Playlist = ?";
            PreparedStatement statement = conn.prepareStatement(queryCheck);
            statement.setString(1, name);
            final ResultSet resultSet = statement.executeQuery();
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

            //insert the song
            if (count == 0) {
                statement = conn.prepareStatement("INSERT INTO " + tableName + " values (?,?)");
                statement.setString(1, name);
                statement.setString(2, indexes);
                statement.executeUpdate();
                return true;
            } else {
                return false;
            }
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return false;
    }
    //create method to change value for the playlist. Adding the index of the songs.

    protected static String addSong(String name, String songNum){
        String index = "";
        try {

            String query = "SELECT SongIndexes from PLAYLISTS WHERE Playlist = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString(1));
                if(!rs.getString(1).trim().equals("empty")) {
                    index = rs.getString(1).trim();
                    System.out.println("in database" + index);
                    index = index + ", " + songNum;
                }
                else{
                    index = songNum;
                }

                statement = conn.prepareStatement("UPDATE PLAYLISTS set SongIndexes = ? WHERE Playlist = ?");
                statement.setString(1, index);
                statement.setString(2, name);
                statement.executeUpdate();
            }

        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return index;
    }

    //return index of the playlist
    protected static String searchDB(String name){
        String index = "";
        try {
            String queryCheck = "SELECT SongIndexes from PLAYLISTS WHERE Playlist = ?";
            PreparedStatement statement = conn.prepareStatement(queryCheck);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                index = rs.getString(1);
            }
            return index;
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return index;
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
            ResultSet tables = meta.getTables(conn.getCatalog(), "APP", "PLAYLISTS", new String[] {"TABLE"});
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
