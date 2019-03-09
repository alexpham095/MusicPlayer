import java.sql.SQLException;

public class Main {

    public static void main(String[] args){

        Database data = new Database();
        data.createConnection();
        if(!data.hasTable()) {
            data.buildTable();
        }

        MusicPlayerGUI music = new MusicPlayerGUI(data);
        music.go();
    }
}
