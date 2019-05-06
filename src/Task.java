import javax.swing.*;

public class Task extends SwingWorker<Void, Void> {
    private JProgressBar progressBar;
    private MusicPlayerGUI gui;
    private int curVal;

    public Task(JProgressBar aProgressBar, MusicPlayerGUI gui) {
        this.progressBar = aProgressBar;
        this.gui = gui;
        progressBar.setVisible(true);
        progressBar.setValue(0);
    }

    @Override
    public Void doInBackground() {
        //long running task
        while(curVal < 100){
            getTime();
            progressBar.setValue(curVal);
        }
        return null;
    }

    public void getTime(){
        double currentValue = ((double)gui.songTime/gui.songLength)*100;
        curVal = (int) currentValue;
    }

    @Override
    public void done() {
        progressBar.setValue(100);
        progressBar.setStringPainted(false);
    }
}