import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.TableColumn;

import com.mpatric.mp3agic.*;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

public class MusicPlayerGUI extends JFrame{

    BasicPlayer player;

    String songPath;
    JFrame  main = new JFrame("Music Player");
    JTable table;
    JScrollPane scrollPane;
    JButton play, stop, pause, skip, previous;
    JPanel buttonPanel, musicPanel;
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem add, open, newPlaylist, newFile;
    ButtonListener bl;
    ActionListener al;
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    int CurrentSelectedRow;
    Database data;

    public MusicPlayerGUI(Database database) {
        data = database;
        player = new BasicPlayer();
        buttonPanel = new JPanel();
        musicPanel = new JPanel();
        main.setLayout(new BorderLayout());
        main.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                data.shutdown();
            }
        });
        bl=new ButtonListener();
        menuBar = new JMenuBar();

        menu = new JMenu("File");
        menuBar.add(menu);

        add = new JMenuItem("Add Song");
        newPlaylist = new JMenuItem("New Playlist");
        newFile = new JMenuItem("Add File To Library");

        MouseListener mouseListener1 = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    data.insertSong(dialog.getSelectedFile().getAbsolutePath());
                    //update table
                }
            }
        };

        add.addMouseListener(mouseListener1);
        menu.add(add);
        menu.add(newPlaylist);
        menu.add(newFile);
        menu.addSeparator();

        //table creation and mouseListener.
        table = new JTable(data.buildSongsTable());
        MouseListener mouseListener = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                CurrentSelectedRow = table.getSelectedRow();
                songPath = (table.getValueAt(CurrentSelectedRow,0)).toString();
            }
        };
        table.addMouseListener(mouseListener);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(250);
        column = table.getColumnModel().getColumn(1);
        column.setPreferredWidth(20);
        scrollPane = new JScrollPane(table);

        //play button
        play = new JButton(">");
        play.addActionListener(bl);

        stop = new JButton("[]");
        stop.addActionListener(bl);

        pause = new JButton("=");
        pause.addActionListener(bl);

        skip = new JButton(">|");
        skip.addActionListener(bl);

        previous = new JButton("|<");
        previous.addActionListener(bl);

        buttonPanel.add(previous);
        buttonPanel.add(pause);
        buttonPanel.add(stop);
        buttonPanel.add(play);
        buttonPanel.add(skip);
        main.setSize(700,200);
        main.setJMenuBar(menuBar);
        main.add(scrollPane, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void go(){
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }


    class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(">".equals(e.getActionCommand())) {
                try {
                    Mp3File mp3file = new Mp3File(songPath);
                    player.open(new File(songPath));
                    player.play();
                    System.out.println(player.getStatus());
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedTagException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InvalidDataException e1) {
                    e1.printStackTrace();
                }
            } else if("=".equals(e.getActionCommand())) {
                try {
                    if(player.getStatus() == 1){
                        player.resume();
                    } else if(player.getStatus() == 0){
                        player.pause();
                    }
                    System.out.println(player.getStatus());

                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if("[]".equals(e.getActionCommand())) {
                try {
                    player.stop();
                    System.out.println(player.getStatus());
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if("|<".equals(e.getActionCommand())) {
                try {
                    songPath = (table.getValueAt(--CurrentSelectedRow,0)).toString();
                    player.open(new File(songPath));
                    player.play();
                    System.out.println(player.getStatus());
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(">|".equals(e.getActionCommand())) {
                try {
                    songPath = (table.getValueAt(++CurrentSelectedRow,0)).toString();
                    player.open(new File(songPath));
                    player.play();
                    System.out.println(songPath);
                    System.out.println(player.getStatus());
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }


}
