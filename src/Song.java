public class Song {
    protected String path, name;
    protected int row, col;

    protected String getPath(){
        return path;
    }

    protected int getRow(){
        return row;
    }

    protected int getCol() {
        return col;
    }

    protected void setPath(String songPath){
        path = songPath;
    }

    protected String getName() {
        return name;
    }
    protected void setContents(String songPath, String songName, int row, int col){
        path = songPath;
        name = songName;
        this.row = row;
        this.col = col;
    }

}
