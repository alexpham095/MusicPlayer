import javax.swing.table.DefaultTableModel;

public class Library {
    Database data;

    public Library(Database database){
        data = database;
    }

    protected DefaultTableModel buildTable(){
        return data.buildSongsTable();
    }

    protected void shutdown(){
        data.shutdown();
    }

    protected boolean insertSong(String songPath){
        return data.insertSong(songPath);
    }

    protected void deleteSong(String songPath){
        data.deleteSong(songPath);
    }
}
