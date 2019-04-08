public class Song {
    protected String path;
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
    protected void setContents(String songPath, int row, int col){
        path = songPath;
        this.row = row;
        this.col = col;
    }

}
