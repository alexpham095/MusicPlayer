import java.sql.SQLException;

public class Main {

    public static void main(String[] args){

        Database data = new Database();
        data.createConnection();
        if(!data.hasTable()) {
            data.buildTable();
        }

        Library lib = new Library(data);
        MusicPlayerGUI music = new MusicPlayerGUI(lib);
        music.go();
    }
}
