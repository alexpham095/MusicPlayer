import com.mpatric.mp3agic.*;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;

public class Database {
    private static String dbURL = "jdbc:derby:SongsDB;create=true";
    private static String tableName = "Songs";
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

    //Basically creating the table for the first time.
    protected static void buildTable()
    {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + tableName + " (File CHAR(150), " + "Title CHAR(150), " + "Artist CHAR(150), " + "Album CHAR(150), "
                    + "\"YEAR\" CHAR(150), " + "Comment CHAR(150), " + "Genre CHAR(150) " + ")");
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    protected static boolean insertSong(String songPath)
    {
        try
        {
            //mp3 file information is being assigned depending on which type of tag it has.
            Mp3File mp3file = new Mp3File(songPath);
            String title = " ";
            String artist = " ";
            String album = " ";
            String comment = " ";
            String genre = " ";
            String year = " ";
            if(mp3file.hasId3v1Tag()){
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                title = id3v1Tag.getTitle();
                artist = id3v1Tag.getArtist();
                album = id3v1Tag.getAlbum();
                comment = id3v1Tag.getComment();
                genre = id3v1Tag.getGenreDescription();
                year = id3v1Tag.getYear();
            }
            if(mp3file.hasId3v2Tag()){
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                title = id3v2Tag.getTitle();
                artist = id3v2Tag.getArtist();
                album = id3v2Tag.getAlbum();
                comment = id3v2Tag.getComment();
                genre = id3v2Tag.getGenreDescription();
                year = id3v2Tag.getYear();
            }

            //check if the song exists already
            String queryCheck = "SELECT count(*) from SONGS WHERE file = ?";
            PreparedStatement statement = conn.prepareStatement(queryCheck);
            statement.setString(1, songPath);
            final ResultSet resultSet = statement.executeQuery();
            int count = 0;
            if(resultSet.next()) {
                count = resultSet.getInt(1);
            }

            //insert the song
            if(count == 0) {
                statement = conn.prepareStatement("INSERT INTO " + tableName + " values (?,?,?,?,?,?,?)");
                statement.setString(1, songPath);
                statement.setString(2, title);
                statement.setString(3, artist);
                statement.setString(4, album);
                statement.setString(5, year);
                statement.setString(6, comment);
                statement.setString(7, genre);
                statement.executeUpdate();
                return true;
            } else {
                return false;
            }
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }
        return false;
    }

    //This is a method used for the GUI. This is relaying the information back for the table to use.
    protected static DefaultTableModel buildSongsTable()
    {
        Vector<Vector<Object>> output = new Vector<Vector<Object>>();
        Vector<String> columnNames = new Vector<String>();
        try
        {
            stmt = conn.createStatement();
            ResultSet results = stmt.executeQuery("select * from " + tableName);
            ResultSetMetaData rsmd = results.getMetaData();
            int numberCols = rsmd.getColumnCount();
            for (int i=1; i<=numberCols; i++)
            {
                //print Column Names
                columnNames.add(rsmd.getColumnLabel(i));
            }

            while(results.next())
            {
                Vector<Object> vector = new Vector<Object>();
                for (int columnIndex = 1; columnIndex <= numberCols; columnIndex++) {
                    vector.add(results.getObject(columnIndex));
                }
                output.add(vector);
            }
            results.close();
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
        return new DefaultTableModel(output, columnNames);
    }

    //delete a song by removing it from the database
    protected static void deleteSong(String songPath)
    {
        try
        {
            PreparedStatement st = conn.prepareStatement("DELETE FROM " + tableName + " WHERE file = ?");
            st.setString(1,songPath);
            st.executeUpdate();
        }
        catch (SQLException sqlExcept) {
            sqlExcept.printStackTrace();
        }
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
            ResultSet tables = meta.getTables(conn.getCatalog(), "APP", "SONGS", new String[] {"TABLE"});
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
