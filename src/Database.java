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

    protected static void buildTable()
    {
        try
        {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE " + tableName + " (File CHAR(150), " + "Title CHAR(150), " + "Artist CHAR(100) " + ")");
            stmt.close();
        }
        catch (SQLException sqlExcept)
        {
            sqlExcept.printStackTrace();
        }
    }

    protected static void insertSong(String songPath)
    {
        try
        {
            //File file = new File("C:\\Users\\aPham\\Google Drive\\CSULB\\CECES 543\\MusicPlayer\\javaMP\\KDA_POPSTARS_INSTRUMENTAL_320kbps.mp3");
            //songPath = file.getAbsolutePath();


            Mp3File mp3file = new Mp3File(songPath);
            String title = " ";
            String artist = " ";
            if(mp3file.hasId3v1Tag()){
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                title = id3v1Tag.getTitle();
                artist = id3v1Tag.getArtist();
            }
            if(mp3file.hasId3v2Tag()){
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                title = id3v2Tag.getTitle();
                artist = id3v2Tag.getArtist();
            }

            PreparedStatement statement = conn.prepareStatement("INSERT INTO " + tableName + " values (?,?,?)");
            statement.setString(1,songPath);
            statement.setString(2,title);
            statement.setString(3,artist);
            statement.executeUpdate();

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
    }

    protected static DefaultTableModel buildSongs()
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

            System.out.println("\n-------------------------------------------------");

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
