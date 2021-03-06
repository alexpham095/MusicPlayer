import javax.swing.table.DefaultTableModel;

public class Library {
    Database data;

    public Library(Database database){
        data = database;
    }

    protected DefaultTableModel buildSongsTable(int order){
        return data.buildSongsTable(order);
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

    protected DefaultTableModel sortData(int order) {
        return data.sortData(order);
    }
}
