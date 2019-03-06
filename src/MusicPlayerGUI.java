import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
    ButtonListener bl;
    int CurrentSelectedRow;
    Database data;

    public MusicPlayerGUI(Database database) {
        data = database;
        player = new BasicPlayer();
        buttonPanel = new JPanel();
        musicPanel = new JPanel();
        main.setLayout(new BorderLayout());
        bl=new ButtonListener();

        //table creation and mouseListener.
        table = new JTable(data.buildSongs());
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
