import java.sql.SQLException;

public class Main {

    public static void main(String[] args){

        Database data = new Database();
        data.createConnection();
        if(!data.hasTable()) {
            data.buildTable();
        }

        PlaylistDatabase playData = new PlaylistDatabase();
        playData.createConnection();
        if(!playData.hasTable()){
            playData.buildTable();
        }

        RecentSongDatabase recentData = new RecentSongDatabase();
        recentData.createConnection();
        if(!recentData.hasTable()){
            recentData.buildTable();
        }

        Library lib = new Library(data);
        MusicPlayerGUI music = new MusicPlayerGUI(lib,playData, recentData);
        music.go();
    }
}
