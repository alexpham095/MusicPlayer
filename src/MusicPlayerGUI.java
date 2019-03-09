import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
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
    JMenuItem newFile, deleteFile, open, newPlaylist, close;
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

        newFile = new JMenuItem("Add Song");
        deleteFile = new JMenuItem("Delete Song");
        newPlaylist = new JMenuItem("New Playlist");
        open = new JMenuItem("Open");
        close = new JMenuItem("Close");

        //listener for adding a file
        newFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    if(data.insertSong(dialog.getSelectedFile().getAbsolutePath()) == false){
                        JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    table.setModel(data.buildSongsTable()); //refresh table
                }
            }

        });

        //listener for deleting a file
        deleteFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setDialogTitle("Delete Song");
                dialog.setApproveButtonText("Delete");
                if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    data.deleteSong(dialog.getSelectedFile().getAbsolutePath());
                    table.setModel(data.buildSongsTable()); //refresh table
                }
            }

        });

        //add to the menu
        menu.add(open);
        menu.add(newPlaylist);
        menu.add(newFile);
        menu.add(deleteFile);
        menu.add(close);
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

        //stop button
        stop = new JButton("[]");
        stop.addActionListener(bl);

        //pause button
        pause = new JButton("=");
        pause.addActionListener(bl);

        //skip button
        skip = new JButton(">|");
        skip.addActionListener(bl);

        //back button
        previous = new JButton("|<");
        previous.addActionListener(bl);

        //add button panel and menubar to the main panel.
        buttonPanel.add(previous);
        buttonPanel.add(pause);
        buttonPanel.add(stop);
        buttonPanel.add(play);
        buttonPanel.add(skip);
        main.setSize(1200,900);
        main.setJMenuBar(menuBar);
        main.add(scrollPane, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);
        main.setLocationRelativeTo(null);
    }

    public void go(){
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }

    //Button listener instructions
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
