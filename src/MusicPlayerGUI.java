import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.JPopupMenu;

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
    JPopupMenu popupMenu;
    JMenuItem newFile, deleteFile, open, newPlaylist, close;
    JMenuItem newFilepop, deleteFilepop;
    ButtonListener bl;
    ActionListener al;
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    int CurrentSelectedRow;
    Mp3File songNotInLibrary;
    Database data;

    //Connecting the DragAndDrop class to the main JFrame
    DragAndDrop dndObj = new DragAndDrop();
    DropTarget targetOfDND = new DropTarget(main,dndObj);

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


        //listener for opening a song to play without adding it to the library
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setDialogTitle("Play a song");
                dialog.setApproveButtonText("Play");
                if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    try {
                        songNotInLibrary = new Mp3File(dialog.getSelectedFile().getAbsolutePath());
                        player.open(new File(dialog.getSelectedFile().getAbsolutePath()));
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
                }
            }

        });


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
                data.deleteSong(songPath);
                table.setModel(data.buildSongsTable()); //refresh table
            }

        });

        //listener for closing the player
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                main.dispose();
            }

        });

        //add to the menu
        menu.add(open);
        //menu.add(newPlaylist);
        menu.add(newFile);
        menu.add(deleteFile);
        menu.add(close);
        menu.addSeparator();

        // constructs the popup menu
        popupMenu = new JPopupMenu();
        newFilepop = new JMenuItem("Add New Song");
        deleteFilepop = new JMenuItem("Remove Song");


        //listener for adding a file in popup
        newFilepop.addActionListener(new ActionListener() {
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

        //listener for deleting a file in popup
        deleteFilepop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.deleteSong(songPath);
                table.setModel(data.buildSongsTable()); //refresh table
                /***
                dialog.setDialogTitle("Delete Song");
                dialog.setApproveButtonText("Delete");
                if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    data.deleteSong(dialog.getSelectedFile().getAbsolutePath());
                    table.setModel(data.buildSongsTable()); //refresh table
                }
                 ***/
            }

        });

        popupMenu.add(newFilepop);
        popupMenu.add(deleteFilepop);

        MouseListener mouseListenerpop = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                CurrentSelectedRow = table.getSelectedRow();
                songPath = (table.getValueAt(CurrentSelectedRow,0)).toString();
            }
        };

        ////////////end popup//////////////////////

        //table creation and mouseListener.

        table = new JTable(data.buildSongsTable());
        table.setDefaultEditor(Object.class, null);
        MouseListener mouseListener = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                CurrentSelectedRow = table.getSelectedRow();
                songPath = (table.getValueAt(CurrentSelectedRow,0)).toString();
            }
        };
        table.addMouseListener(mouseListener);
        // sets the popup menu for the table
        table.setComponentPopupMenu(popupMenu);
        table.addMouseListener(mouseListenerpop);
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
        main.setSize(1200,700);
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
                   // if(player.getStatus() == 1){
                      //  player.resume();
                    //} else {
                        Mp3File mp3file = new Mp3File(songPath);
                        player.open(new File(songPath));
                        player.play();
                        System.out.println(player.getStatus());
                   // }
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
                    String firstSong = (table.getValueAt(0,0).toString());
                    if(firstSong == songPath){
                        songPath = (table.getValueAt(table.getModel().getRowCount()-1,0).toString());
                        table.setRowSelectionInterval(table.getModel().getRowCount()-1, table.getModel().getRowCount()-1);
                        player.open(new File(songPath));
                        player.play();
                    } else {
                        songPath = (table.getValueAt(--CurrentSelectedRow, 0)).toString();
                        table.setRowSelectionInterval(CurrentSelectedRow, CurrentSelectedRow);
                        player.open(new File(songPath));
                        player.play();
                    }
                    //table.setModel(data.buildSongsTable()); //refresh table
                    //System.out.println(player.getStatus());
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(">|".equals(e.getActionCommand())) {
                try {
                    String lastSong = (table.getValueAt(table.getModel().getRowCount()-1,0).toString());
                    if(lastSong == songPath){
                        songPath = (table.getValueAt(0,0).toString());
                        table.setRowSelectionInterval(0, 0);
                        player.open(new File(songPath));
                        player.play();
                    } else {
                        songPath = (table.getValueAt(++CurrentSelectedRow, 0)).toString();
                        table.setRowSelectionInterval(CurrentSelectedRow, CurrentSelectedRow);
                        player.open(new File(songPath));
                        player.play();
                    }
                    //table.setModel(data.buildSongsTable()); //refresh table
                    //System.out.println(songPath);
                    //System.out.println(player.getStatus());
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    //Creating a class for Drag and Drop

    public class DragAndDrop implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {

        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {

        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {

        }

        @Override
        public void dragExit(DropTargetEvent dte) {

        }

        //This method will get the dropped files
        @Override
        public void drop(DropTargetDropEvent fileDropped)
        {
            fileDropped.acceptDrop(DnDConstants.ACTION_COPY);

            Transferable transfer = fileDropped.getTransferable();

            DataFlavor[] dataFlavor = transfer.getTransferDataFlavors();

            for(DataFlavor flavor:dataFlavor)
            {
                try
                {
                    if(flavor.isFlavorJavaFileListType())
                    {
                        List<File> files = (List<File>) transfer.getTransferData(flavor);

                        for(File file : files)
                        {
                            if(data.insertSong(file.getAbsolutePath()) == false){
                                JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            table.setModel(data.buildSongsTable()); //refresh table
                        }
                    }
                }catch(Exception problem)
                {
                    JOptionPane.showMessageDialog(null, problem);
                }

            }
        }
    }


}
