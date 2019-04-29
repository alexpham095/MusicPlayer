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
    /***
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
     ***/

    protected static void buildTable()
    {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + tableName + " (Playlist CHAR(150), " + "SongPaths CLOB(1999999999)" + ")");
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    //return name of playlists
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

    //create a playlist
    protected static boolean insertPlaylist(String name, String songPaths){
        try {
            String queryCheck = "SELECT count(*) from PLAYLISTS WHERE Playlist = ?";
            PreparedStatement statement = conn.prepareStatement(queryCheck);
            statement.setString(1, name);
            final ResultSet resultSet = statement.executeQuery();
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

            //insert the playlist
            if (count == 0) {
                statement = conn.prepareStatement("INSERT INTO " + tableName + " values (?,?)");
                statement.setString(1, name);
                statement.setString(2, songPaths);
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

    //add a song to the playlist
    protected static String addSong(String name, String songPathToAdd){
        String songPath = "";
        try {

            String query = "SELECT SongPaths from PLAYLISTS WHERE Playlist = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString(1));
                if(!rs.getString(1).trim().equals("empty")) {
                    songPath = rs.getString(1).trim();
                    songPath = songPath + ", " + songPathToAdd;
                }
                else{
                    songPath = songPathToAdd;
                }

                statement = conn.prepareStatement("UPDATE PLAYLISTS set SongPaths = ? WHERE Playlist = ?");
                statement.setString(1, songPath);
                statement.setString(2, name);
                statement.executeUpdate();
            }

        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
        return songPath;
    }

    protected void deleteSong(String name, String songPath){
        String index = "";
        String indexVal ="";
        try {

            String query = "SELECT SongPaths from PLAYLISTS WHERE Playlist = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                System.out.println(rs.getString(1));
                if(!rs.getString(1).trim().equals("empty")) {
                    index = rs.getString(1).trim();
                    String[] indexes = index.split("\\s*,\\s");

                    for(int i = 0; i < indexes.length; i++){
                        System.out.println(indexes[i]);
                        if(!indexes[i].equals(songPath)){
                            if(!indexVal.equals("")) {
                                indexVal = indexVal + ", " + indexes[i];
                            } else {
                                indexVal = indexes[i];
                            }
                        }

                    }
                }

                if (indexVal==""){
                    indexVal = "empty";
                }

                statement = conn.prepareStatement("UPDATE PLAYLISTS set SongPaths = ? WHERE Playlist = ?");
                statement.setString(1, indexVal);
                statement.setString(2, name);
                statement.executeUpdate();
            }

        } catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }

    protected static void deletePlaylist(String name)
    {

        try
        {
            PreparedStatement st = conn.prepareStatement("DELETE FROM " + tableName + " WHERE Playlist = ?");
            st.setString(1,name);
            st.executeUpdate();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
    }

    //return songPaths of songs in the playlist
    protected static String searchDB(String name){
        String songPaths = "";
        try {
            String queryCheck = "SELECT SongPaths from PLAYLISTS WHERE Playlist = ?";
            PreparedStatement statement = conn.prepareStatement(queryCheck);
            statement.setString(1, name);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                songPaths = rs.getString(1);
            }
            return songPaths;
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return songPaths;
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
