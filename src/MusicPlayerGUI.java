import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import javax.swing.JPopupMenu;
import javax.swing.tree.*;

import com.mpatric.mp3agic.*;
import javazoom.jlgui.basicplayer.*;



public class MusicPlayerGUI {

    BasicPlayer player;

    String highlightedSongPath;
    JFrame  main = new JFrame("Music Player");
    JTable table, plTable, currentTable;
    JTree tree;
    JScrollPane scrollPane, sourceScrollPane;
    JSplitPane splitPane;
    JButton play, stop, pause, skip, previous;
    JPanel buttonPanel, musicPanel, progressPanel;
    JMenuBar menuBar;
    JMenu menu,addToPlaylist, controlMenu, recentMenu;
    JPopupMenu popupLibraryMenu, popupTreeMenu, popupHeader;
    JMenuItem newFile, deleteFile, open, newPlaylist, close;
    JMenuItem newFilepop, deleteFilepop, newWindowPop, deleteplayPop;
    JMenuItem playMenuItem, nextMenuItem, previousMenuItem, currentSongMenuItem,
                incVolMenuItem, decVolMenuItem;
    JCheckBoxMenuItem filePop, titlePop, albumPop, artistPop, yearPop, genrePop, commentPop, shuffleMenuItem, repeatMenuItem;
    JTableHeader header;
    JProgressBar progressBar;
    JTextField passedTimeText, remainTimeText;
    JSlider slider;
    MouseListener mouseListenerpop,mouseListenerTree;
    DefaultTreeModel model;
    BasicPlayerListener bpListener;
    ButtonListener bl;
    ActionListener al;
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    List<MusicPlayerGUI> windowList = new ArrayList<MusicPlayerGUI>();
    ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();
    ArrayList<JMenuItem> songMenuItems = new ArrayList<JMenuItem>();
    int windowIndex = 0;
    DefaultMutableTreeNode playlistNode, selectedNode;
    int currentSelectedRow = -1;
    Mp3File songNotInLibrary;
    boolean isExistingInLibrary = true;
    boolean isPlaylist = false;
    boolean isWindow = false;
    boolean manuallyClicked = false;
    boolean isShuffled = false;
    boolean isRepeating = false;
    Library library;
    PlaylistDatabase playDB;
    RecentSongDatabase recentDB;
    Song currentSong = new Song();
    String playlistName;
    double volume = 0.5;
    int order = 0;
    long songLength = 0;
    long songTime = 0;
    int index = 0;
    Task task;
    String duration;
    String timeLapsed;
    //Connecting the DragAndDrop class to the main JFrame
    DragAndDrop dndObj = new DragAndDrop();
    DropTarget targetOfDND = new DropTarget(main,dndObj);
    MusicPlayerGUI origin;
    int numColumns = 7;

    public MusicPlayerGUI(Library lib, PlaylistDatabase playDataB, RecentSongDatabase recentData) {
        library = lib;
        playDB = playDataB;
        recentDB = recentData;
        progressBar = new JProgressBar(0,100);
        progressBar.setValue(0);
        Dimension dim = new Dimension();
        dim.width = 1330;
        dim.height = 20;
        progressBar.setPreferredSize(dim);
        player = new BasicPlayer();
        createBPList();
        player.addBasicPlayerListener(bpListener);
        buttonPanel = new JPanel();
        musicPanel = new JPanel();
        createMain();
    }

    //constructor for playlists
    public MusicPlayerGUI(Library lib, PlaylistDatabase playDataB, String name, MusicPlayerGUI origin, RecentSongDatabase recentData) {
        library = lib;
        playDB = playDataB;
        recentDB = recentData;
        player = new BasicPlayer();
        progressBar = new JProgressBar(0,100);
        progressBar.setValue(0);
        createBPList();
        player.addBasicPlayerListener(bpListener);
        buttonPanel = new JPanel();
        musicPanel = new JPanel();
        isPlaylist = true;
        isWindow = true;
        this.origin = origin;
        createPlaylistMain(name);
        currentTable = plTable;
    }

    public void createBPList(){
        MusicPlayerGUI mainGUI = this;
        bpListener = new BasicPlayerListener() {
            @Override
            public void opened(Object o, Map properties) {
                //System.out.println("opened : "+properties.toString());
                songLength = (long) properties.get("duration");
                duration = String.format("%02d:%02d:%02d",
                        TimeUnit.MICROSECONDS.toHours(songLength),
                        TimeUnit.MICROSECONDS.toMinutes(songLength) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MICROSECONDS.toHours(songLength)),
                        TimeUnit.MICROSECONDS.toSeconds(songLength) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(songLength)));
                progressBar.setMaximum((int)songLength);
                remainTimeText.setText(duration);
                //progressBar.setValue(55);
                //progressBar.repaint();
            }

            @Override
            public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
                //System.out.println(songLength);
                //System.out.println("progress : "+properties.toString());
                songTime = (long) properties.get("mp3.position.microseconds");
                timeLapsed = String.format("%02d:%02d:%02d",
                        TimeUnit.MICROSECONDS.toHours(songTime),
                        TimeUnit.MICROSECONDS.toMinutes(songTime) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MICROSECONDS.toHours(songTime)),
                        TimeUnit.MICROSECONDS.toSeconds(songTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(songTime)));
                duration = String.format("%02d:%02d:%02d",
                        TimeUnit.MICROSECONDS.toHours(songLength - songTime),
                        TimeUnit.MICROSECONDS.toMinutes(songLength - songTime) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MICROSECONDS.toHours(songLength - songTime)),
                        TimeUnit.MICROSECONDS.toSeconds(songLength - songTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(songLength - songTime)));
                double currentValue = ((double)songTime/songLength)*100;
                int curVal = (int) currentValue;
                //Task task = new Task(progressBar,mainGUI);
                //task.execute();


                passedTimeText.setText(timeLapsed);
                remainTimeText.setText(duration);
                if(songLength == songTime) {
                    System.out.println(songTime);
                }
            }

            @Override
            public void stateUpdated(BasicPlayerEvent event) {
                System.out.println("State Updated: " + event.toString());
                if (event.getCode() == BasicPlayerEvent.STOPPED && !manuallyClicked && !isRepeating) {
                    if(!isShuffled) {
                        if (!isPlaylist) {
                            if (currentSong.getRow() < table.getRowCount() - 1) {
                                currentSelectedRow = currentSong.getRow() + 1;
                            }
                        } else {
                            if (currentSong.getRow() < plTable.getRowCount() - 1) {
                                currentSelectedRow = currentSong.getRow() + 1;
                            }
                        }
                    } else {
                        Random rand = new Random();
                        if (!isPlaylist) {
                            int shuffledIndex = rand.nextInt(table.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(table.getRowCount());
                            }
                            if (currentSong.getRow() < table.getRowCount() - 1) {
                                currentSelectedRow = shuffledIndex;
                            }
                        } else {
                            int shuffledIndex = rand.nextInt(plTable.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(plTable.getRowCount());
                            }
                            if (currentSong.getRow() < plTable.getRowCount() - 1) {
                                currentSelectedRow = shuffledIndex;
                            }
                        }
                    }
                    play.doClick();
                } else if( !manuallyClicked && event.getCode() == BasicPlayerEvent.STOPPED && isRepeating) {
                    currentSelectedRow = currentSong.getRow();
                    play.doClick();
                }
                else if(event.getCode() == BasicPlayerEvent.PLAYING){
                    manuallyClicked = false;
                    recentDB.addSong(currentSong.getName(), currentSong.getPath());
                    populateRecents();
                }
            }


            @Override
            public void setController(BasicController basicController) {

            }

        };
    }


    public void createMain(){
        main.setLayout(new BorderLayout());
        main.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                library.shutdown();
            }
        });
        bl=new ButtonListener();
        menuBar = new JMenuBar();

        createMenu();
        createControlMenu();
        createLibraryPopup();
        createTable();
        //stopFileCol(table);
        displayLibraryTable();
        createButtons();
        //table.setModel(library.sortData());
        createProgressBarPanel();

        main.setSize(1500,700);
        main.setJMenuBar(menuBar);
        main.add(progressPanel, BorderLayout.NORTH);
        main.add(splitPane, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);
        main.setLocationRelativeTo(null);
    }

    public void createPlaylistMain(String name){
        main.setLayout(new BorderLayout());

        playlistName = name;
        main.setTitle(playlistName);

        bl=new ButtonListener();
        menuBar = new JMenuBar();

        createMenu();
        createControlMenu();
        createLibraryPopup();
        createTable();
        //stopFileCol(table);
        populatePlaylist(name);
        displayPlaylistTable();
        createButtons();
        //table.setVisible(false);

        main.setSize(1500,700);
        main.setJMenuBar(menuBar);
        main.add(scrollPane, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);
        main.setLocationRelativeTo(null);
    }

    public void createProgressBarPanel(){
        progressPanel = new JPanel();
        passedTimeText = new JTextField("00:00:00"){
            @Override public void setBorder(Border border) {
                // No!
            }
        };
        remainTimeText = new JTextField("00:00:00"){
            @Override public void setBorder(Border border) {
                // No!
            }
        };
        passedTimeText.setEditable(false);
        remainTimeText.setEditable(false);
        progressPanel.add(passedTimeText);
        progressPanel.add(progressBar);
        progressPanel.add(remainTimeText);
    }

    //table creation and mouseListener.
    public void createTable(){
        table = new JTable();
        table.setModel(library.buildSongsTable());
        table.setDefaultEditor(Object.class, null);
        table.setDragEnabled(true);
        DragAndDrop plDnD = new DragAndDrop();
        DropTarget targetOfPLDND = new DropTarget(main,plDnD);

        // sets the popup menu for the table
        table.setComponentPopupMenu(popupLibraryMenu);
        table.addMouseListener(mouseListenerpop);
        //TableColumn column = table.getColumnModel().getColumn(0);
        //column.setMinWidth(0);
        //column.setMaxWidth(0);
        //column.setPreferredWidth(0);
        //column = table.getColumnModel().getColumn(1);
        //column.setPreferredWidth(200);


            header = table.getTableHeader();
            createHeader();
            header.setComponentPopupMenu(popupHeader);
            table.setTableHeader(header);

            //Get the current toggle state of the columns and draw it
            redrawColumns(table);


        //hideAllCol(table);
        currentTable = table;
        KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK);
        table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(leftKey, "ArrowKeys");
        table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(rightKey, "ArrowKeys");

        createTree();
        redrawColumns(table);
    }

    public void displayLibraryTable(){
        scrollPane = new JScrollPane(table);
        sourceScrollPane = new JScrollPane(tree);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourceScrollPane, scrollPane);
        splitPane.setOneTouchExpandable(true);
        int location = 1500/8; //put size in here
        splitPane.setDividerLocation(location);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPane.setMinimumSize(minimumSize);
        sourceScrollPane.setMinimumSize(minimumSize);
    }

    public void displayPlaylistTable(){
        // plTable = new JTable();
        popupLibraryMenu.remove(addToPlaylist);
        popupLibraryMenu.remove(newFilepop);
        plTable.setComponentPopupMenu(popupLibraryMenu);
        scrollPane = new JScrollPane(plTable);

        KeyStroke leftKey = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke rightKey = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK);
        plTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(leftKey, "ArrowKeys");
        plTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(rightKey, "ArrowKeys");
        header = plTable.getTableHeader();
        createHeader();
        header.setComponentPopupMenu(popupHeader);
        plTable.setTableHeader(header);
        //hideAllCol(table);

        //stopFileCol(plTable);
        redrawColumns(plTable);


        Dimension minimumSize = new Dimension(100, 50);
        scrollPane.setMinimumSize(minimumSize);
    }

    public void stopFileCol(JTable table){
        if(table.getRowCount() != 0) {
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);
        }
    }

    public void hideAllCol(JTable table){
        JCheckBoxMenuItem[] activeCol = {filePop, titlePop, albumPop, artistPop, yearPop, genrePop, commentPop};
        if(table.getRowCount() == 0){
            {
                for (int i = 2; i < table.getColumnCount(); i++) {
                    table.getColumnModel().getColumn(i).setMinWidth(0);
                    table.getColumnModel().getColumn(i).setMaxWidth(0);
                }
            }
        }else {

            for (int i = 0; i < activeCol.length; i++) {
                toggleColumn(table, activeCol[i].getText(), activeCol[i].getState());
                //table.getColumnModel().getColumn(i).setMinWidth(0);
                //table.getColumnModel().getColumn(i).setMaxWidth(0);
            }
            table.getTableHeader().repaint();
        }

    }

    //create the tree for the side panel
    public void createTree(){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode libraryNode = new DefaultMutableTreeNode("Library");
        playlistNode = new DefaultMutableTreeNode("Playlists");
        root.add(libraryNode);
        root.add(playlistNode);
        libraryNode.setAllowsChildren(false);
        //playlistNode.add(new DefaultMutableTreeNode("Popping"));
        Vector<String> names = playDB.buildPlaylistTree();
        for(int i = 0; i < names.size(); i++){
            DefaultMutableTreeNode temp = new DefaultMutableTreeNode(names.get(i));
            playlistNode.add(temp);
        }
        model = new DefaultTreeModel(root);

        tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        expandAllNodes(1,tree.getRowCount());

        createTreePopup();
        tree.setComponentPopupMenu(popupTreeMenu);
        tree.addMouseListener(mouseListenerTree);



        tree.setEditable(false);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.putClientProperty("JTree.lineStyle", "None");
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
    }

    public void addPlaylist(){
        DefaultMutableTreeNode getNode = new DefaultMutableTreeNode("New Playlist");

        String input = JOptionPane.showInputDialog("Playlist Name: ");

        if(playDB.insertPlaylist(input, "empty") == false){
            JOptionPane.showMessageDialog(musicPanel, "Playlist already exists", "Error", JOptionPane.ERROR_MESSAGE);
        } else {

            getNode.setUserObject(input);
            model.insertNodeInto(getNode, playlistNode, playlistNode.getChildCount());
            expandAllNodes(1, tree.getRowCount());
            TreeNode[] nodes = model.getPathToRoot(getNode);
            TreePath treePath = new TreePath(nodes);
            tree.scrollPathToVisible(treePath);
            tree.setSelectionPath(treePath);
            updatePlaylistPopUp();
            plTable = new JTable();
            popupLibraryMenu.remove(addToPlaylist);
            popupLibraryMenu.remove(newFilepop);
            plTable.setComponentPopupMenu(popupLibraryMenu);
            scrollPane.setViewportView(plTable);
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }

    public void expandAllNodes(int startingIndex, int rowCount){
        for(int i=startingIndex;i<rowCount;++i){
            tree.expandRow(i);
        }

        if(tree.getRowCount()!=rowCount){
            expandAllNodes(rowCount, tree.getRowCount());
        }

    }

    //Create buttons and button panel
    public void createButtons(){
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
        slider = new JSlider();
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(10);
        slider.getValue();

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent f) {
             volume=slider.getValue()/100.0;
                try {
                    player.setGain(volume);
                } catch (BasicPlayerException e) {
                    e.printStackTrace();
                }
            }

        });

        buttonPanel.add(slider);
    }

    //create the file menu and options
    public void createMenu(){
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
                        isExistingInLibrary = false;
                        player.play();
                    } catch (BasicPlayerException ex) {
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

        newPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPlaylist();
            }
        });


        //listener for adding a file
        newFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    if(library.insertSong(dialog.getSelectedFile().getAbsolutePath()) == false && !isPlaylist){
                        JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    table.setModel(library.buildSongsTable()); //refresh table
                    redrawColumns(table);
                    //stopFileCol(table);
                    //hideAllCol(table);

                    if(isPlaylist) {
                        String songPath = dialog.getSelectedFile().getAbsolutePath();
                        String selectedPath = "0";

                        //retrieve the song to add
                        for (int i = 0; i < table.getRowCount(); i++) {
                            table.setRowSelectionInterval(i, i);
                            String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                            if(songPath.equals(tableValue)){
                                selectedPath = tableValue;
                            }
                        }

                        //check if the playlist has the song already
                        boolean playlistContains = false;
                        String currentList = playDB.searchDB(playlistName);
                        ArrayList<String> songPathList = new ArrayList<String>();
                        if(!currentList.trim().equals("empty")) {
                            String[] tokens = currentList.trim().split("\\s*,\\s*");
                            for (String s : tokens) {
                                songPathList.add(s);
                            }
                        }

                        for(int i = 0; i < songPathList.size(); i++) {
                            if(songPathList.get(i).equals(selectedPath)){
                                playlistContains = true;
                            }
                        }
                        //add the song to the playlist
                        if(!playlistContains) {
                            playDB.addSong(playlistName, selectedPath);
                            populatePlaylist(playlistName);
                        }
                        scrollPane.setViewportView(plTable);
                        //stopFileCol(plTable);

                    }

                    redrawColumns(table);
                }
            }

        });

        //listener for deleting a file
        deleteFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPlaylist) {

                    for(int j = 0; j < windowList.size(); j++){
                        MusicPlayerGUI temp = windowList.get(j);
                        String name = temp.playlistName;
                        String songPath = highlightedSongPath.trim();
                        String selectedPath = "";
                        //if deleted from library, then must delete from playlist.
                        for (int i = 0; i < table.getRowCount(); i++) {
                            table.setRowSelectionInterval(i, i);

                            String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                            if(songPath.equals(tableValue)){
                                selectedPath = tableValue;
                            }

                        }
                        //delete from the playlist, song does not exist in the library anymore.
                        playDB.deleteSong(name, selectedPath);
                        temp.populatePlaylist(name);
                        temp.scrollPane.setViewportView(temp.plTable);
                        //stopFileCol(plTable);

                    }
                    library.deleteSong(highlightedSongPath);
                    table.setModel(library.buildSongsTable()); //refresh table
                    redrawColumns(table);
                    //stopFileCol(table);
                   // hideAllCol(table);
                }
                else
                {
                    String songPath = highlightedSongPath.trim();
                    String selectedPath = "";
                    for (int i = 0; i < table.getRowCount(); i++) {
                        table.setRowSelectionInterval(i, i);
                        String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                        if(songPath.equals(tableValue)){
                            selectedPath = tableValue;
                        }

                    }
                    playDB.deleteSong(playlistName, selectedPath);
                    populatePlaylist(playlistName);
                    scrollPane.setViewportView(plTable);
                    //stopFileCol(plTable);
                }

                redrawColumns(table);
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
        menu.add(newPlaylist);
        menu.add(newFile);
        menu.add(deleteFile);
        menu.add(close);

    }

    public void createControlMenu(){
        controlMenu = new JMenu("Controls");
        menuBar.add(controlMenu);

        playMenuItem = new JMenuItem("Play");
        nextMenuItem = new JMenuItem("Next");
        previousMenuItem = new JMenuItem("Previous");
        recentMenu = new JMenu("Play Recent");
        currentSongMenuItem = new JMenuItem("Go To Current Song");
        incVolMenuItem = new JMenuItem("Increase Volume");
        decVolMenuItem = new JMenuItem("Decrease Volume");
        shuffleMenuItem = new JCheckBoxMenuItem("Shuffle", false);
        repeatMenuItem = new JCheckBoxMenuItem("Repeat", false);

        playMenuItem.setAccelerator(KeyStroke.getKeyStroke(32, 0));
        nextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK));
        previousMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK));
        currentSongMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        incVolMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK));
        decVolMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));

        playMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                play.doClick();

            }
        });

        nextMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               skip.doClick();
            }
        });

        previousMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previous.doClick();
            }
        });

        recentMenu.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {
                recentMenu.doClick();
            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        populateRecents();

        currentSongMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentTable == table){
                    scrollPane.setViewportView(table);
                } else if(currentTable == plTable){
                    scrollPane.setViewportView(plTable);
                }
                if(player.getStatus() == 0){
                    currentTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                    currentTable.scrollRectToVisible(new Rectangle(currentTable.getCellRect(currentSong.getRow(), 0, true)));
                } else {
                    currentTable.scrollRectToVisible(new Rectangle(currentTable.getCellRect(currentTable.getSelectedRow(), 0, true)));
                }
            }
        });

        incVolMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                volume = (slider.getValue() + 5)/100.0;
                slider.setValue(slider.getValue() + 5);
            }
        });

        decVolMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                volume = (slider.getValue() - 5)/100.0;
                slider.setValue(slider.getValue() - 5);
            }
        });

        shuffleMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                isShuffled = !isShuffled;
            }
        });

        repeatMenuItem.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                isRepeating = !isRepeating;
            }
        });


        controlMenu.add(playMenuItem);
        controlMenu.add(nextMenuItem);
        controlMenu.add(previousMenuItem);
        controlMenu.add(recentMenu);
        controlMenu.add(currentSongMenuItem);
        controlMenu.add(incVolMenuItem);
        controlMenu.add(decVolMenuItem);
        controlMenu.addSeparator();
        controlMenu.add(shuffleMenuItem);
        controlMenu.add(repeatMenuItem);
    }

    public void createHeader(){
        popupHeader = new JPopupMenu();
        filePop = new JCheckBoxMenuItem("FILE");
        titlePop = new JCheckBoxMenuItem("TITLE");
        albumPop = new JCheckBoxMenuItem("ALBUM");
        artistPop = new JCheckBoxMenuItem("ARTIST");
        yearPop = new JCheckBoxMenuItem("YEAR");
        genrePop = new JCheckBoxMenuItem("GENRE");
        commentPop = new JCheckBoxMenuItem("COMMENT");
        BufferedReader br = null;
        char textReader;

        //popupHeader.add(filePop); HIDE SO THAT USER CANNOT CHANGE THE TOGGLE
        //popupHeader.add(titlePop; HIDE SO THAT USER CANNOT CHANGE THE TOGGLE
        popupHeader.add(albumPop);
        popupHeader.add(artistPop);
        popupHeader.add(yearPop);
        popupHeader.add(genrePop);
        popupHeader.add(commentPop);

        //Get the boolean states of each column from a text file called headerToggle.txt
        try {
            br = new BufferedReader(new FileReader("headerToggle.txt"));

            for(int i = 0; i < numColumns; ++i)
            {
                textReader = (char)br.read();

                //The following if...else statements reads 1 or 0 from the text file
                //and sets the appropriate state for the check boxes for each column
                if  (i == 0) {
                    if (textReader == '1') {
                        filePop.setState(true);
                    }
                    else {
                        filePop.setState(false);
                    }
                }

                else if (i == 1) {
                    if (textReader == '1') {
                        titlePop.setState(true);
                    }
                    else {
                        titlePop.setState(false);
                    }
                }

                else if (i == 2) {
                    if (textReader == '1') {
                        albumPop.setState(true);
                    }
                    else {
                        albumPop.setState(false);
                    }
                }

                else if (i == 3) {
                    if (textReader == '1') {
                        artistPop.setState(true);
                    }
                    else {
                        artistPop.setState(false);
                    }
                }

                else if (i == 4) {
                    if (textReader == '1') {
                        yearPop.setState(true);
                    }
                    else {
                        yearPop.setState(false);
                    }
                }

                else if (i == 5) {
                    if (textReader == '1') {
                        genrePop.setState(true);
                    }
                    else {
                        genrePop.setState(false);
                    }
                }

                else if (i == 6) {
                    if (textReader == '1') {
                        commentPop.setState(true);
                    }
                    else {
                        commentPop.setState(false);
                    }
                }
            }
        } catch(IOException e) {

        } finally {
            try {
                br.close();
            } catch (IOException e) {

            }
        }

        filePop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleColumn(table, "FILE", filePop.getState());

                //Update the columns base on the boolean of each column
                writeToHeaderToggle (filePop.getState(), titlePop.getState(), albumPop.getState(), artistPop.getState(), yearPop.getState(), genrePop.getState(), commentPop.getState());

            }
        });

        titlePop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleColumn(table, "TITLE", titlePop.getState());

                //Update the columns base on the boolean of each column
                writeToHeaderToggle (filePop.getState(), titlePop.getState(), albumPop.getState(), artistPop.getState(), yearPop.getState(), genrePop.getState(), commentPop.getState());

            }
        });
        albumPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleColumn(table, "ALBUM", albumPop.getState());

                //Update the columns base on the boolean of each column
                writeToHeaderToggle (filePop.getState(), titlePop.getState(), albumPop.getState(), artistPop.getState(), yearPop.getState(), genrePop.getState(), commentPop.getState());

            }
        });

        artistPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleColumn(table, "ARTIST", artistPop.getState());

                writeToHeaderToggle (filePop.getState(), titlePop.getState(), albumPop.getState(), artistPop.getState(), yearPop.getState(), genrePop.getState(), commentPop.getState());

            }
        });

        yearPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleColumn(table, "YEAR", yearPop.getState());

                writeToHeaderToggle (filePop.getState(), titlePop.getState(), albumPop.getState(), artistPop.getState(), yearPop.getState(), genrePop.getState(), commentPop.getState());

            }
        });

        genrePop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleColumn(table, "GENRE", genrePop.getState());

                writeToHeaderToggle (filePop.getState(), titlePop.getState(), albumPop.getState(), artistPop.getState(), yearPop.getState(), genrePop.getState(), commentPop.getState());

            }
        });

        commentPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleColumn(table, "COMMENT", commentPop.getState());

                writeToHeaderToggle (filePop.getState(), titlePop.getState(), albumPop.getState(), artistPop.getState(), yearPop.getState(), genrePop.getState(), commentPop.getState());

            }
        });

        if(!isPlaylist) {
            //if playlist is in a window
            //if its a main window
            table.getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = table.columnAtPoint(e.getPoint());
                    if (order == 0) {
                        order = 1;
                    } else {
                        order = 0;
                    }
                    if (column == 1) {
                        table.setModel(library.sortData(order));

                        //Get the current toggle state of the columns and draw it
                        redrawColumns(table);
                    }
                }
            });
            //if it is a playlist
        } else {

            plTable.getTableHeader().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int column = plTable.columnAtPoint(e.getPoint());
                    if (order == 0) {
                        order = 1;
                    } else {
                        order = 0;
                    }
                    if (column == 1) {
                        plTable.setAutoCreateRowSorter(true);
                        TableRowSorter<TableModel> sorter = new TableRowSorter<>(plTable.getModel());
                        plTable.setRowSorter(sorter);
                        List<RowSorter.SortKey> sortKeys = new ArrayList<>();

                        int columnIndexToSort = 1;
                        if (order == 0) {
                            sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.DESCENDING));
                        } else {
                            sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));
                        }

                        sorter.setSortKeys(sortKeys);
                        sorter.sort();

                        redrawColumns(plTable);
                    }
                }
            });
        }

    }

    //This function takes the boolean of each column and writes it to headerToggle.txt to save the state of each boolean
    public void writeToHeaderToggle (boolean file, boolean title, boolean album, boolean  artist, boolean year, boolean genre, boolean comment) {
        char boolTranslate;
        boolean boolArray[] = {file, title, album, artist, year, genre, comment};
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter("headerToggle.txt"));

            for (int i = 0; i < numColumns; ++i) {
                if (boolArray[i]) {
                    boolTranslate = '1';
                }
                else {
                    boolTranslate = '0';
                }

                bw.write((int)boolTranslate);
            }

        } catch(IOException e) {

        } finally {
            try {
                bw.close();
            } catch (IOException e) {

            }
        }
    }

    public void toggleColumn(JTable table, String column, boolean check){

        if(table.getRowCount() != 0) {
            int toggle = 0;
            if(check){
                toggle = 220;
            }

            for(int i = 0; i < table.getColumnCount(); i++){
                if(column.equals(table.getColumnName(i))){
                    System.out.println(table.getValueAt(0,4));
                    table.getColumnModel().getColumn(i).setMinWidth(toggle);
                    table.getColumnModel().getColumn(i).setMaxWidth(toggle);
                    table.getColumnModel().getColumn(i).setPreferredWidth(toggle);
                }
            }
            System.out.println();
            table.getTableHeader().repaint();
            table.repaint();
        }
    }

    //create popup menu and options
    public void createLibraryPopup(){
        popupLibraryMenu = new JPopupMenu();
        newFilepop = new JMenuItem("Add New Song");
        deleteFilepop = new JMenuItem("Remove Song");
        addToPlaylist = new JMenu("Add to Playlist");

        //listener for adding a file in popup

        newFilepop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    if(library.insertSong(dialog.getSelectedFile().getAbsolutePath()) == false){
                        JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    table.setModel(library.buildSongsTable()); //refresh table
                    redrawColumns(table);
                    //stopFileCol(table);
                    //hideAllCol(table);
                }
            }

        });

        //listener for deleting a file in popup
        deleteFilepop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPlaylist) {
                    //if there are no playlist windows open, should delete the song from the library and the playlist
                    if(windowList.size() == 0){
                        Vector<String> playlistList = playDB.buildPlaylistTree();
                        for (int j = 0; j < playlistList.size(); j++) {
                            String name = playlistList.get(j);
                            String songPath = highlightedSongPath.trim();
                            //duplicate, can combine with other delete method
                            String selectedPath = "";
                            for (int i = 0; i < table.getRowCount(); i++) {
                                table.setRowSelectionInterval(i, i);

                                String tableValue = table.getModel().getValueAt(i, 0).toString().trim();
                                if (songPath.equals(tableValue)) {
                                    selectedPath = tableValue;
                                }

                            }

                            if(name != null) {
                                playDB.deleteSong(name, selectedPath);
                                populatePlaylist(name);
                            }
                            redrawColumns(table);
                        }
                    }
                    //if there are windows open, update those windows.
                    else {
                        for (int j = 0; j < windowList.size(); j++) {
                            MusicPlayerGUI temp = windowList.get(j);
                            String name = temp.playlistName;
                            String songPath = highlightedSongPath.trim();
                            //duplicate can combine
                            String selectedPath = "";
                            for (int i = 0; i < table.getRowCount(); i++) {
                                table.setRowSelectionInterval(i, i);
                                String tableValue = table.getModel().getValueAt(i, 0).toString().trim();
                                if (songPath.equals(tableValue)) {
                                    selectedPath = tableValue;
                                }

                            }
                            playDB.deleteSong(name, selectedPath);
                            temp.populatePlaylist(name);
                            temp.scrollPane.setViewportView(temp.plTable);
                            //stopFileCol(temp.plTable);

                        }
                    }
                    library.deleteSong(highlightedSongPath.trim());
                    table.setModel(library.buildSongsTable()); //refresh table
                    redrawColumns(table);
                    //stopFileCol(table);
                    //hideAllCol(table);

                }
                //if it is a playlist
                else
                {
                        String songPath = highlightedSongPath.trim();
                        String selectedPath = "";
                        for (int i = 0; i < table.getRowCount(); i++) {
                            table.setRowSelectionInterval(i, i);
                            String tableValue = table.getModel().getValueAt(i, 0).toString().trim();
                            if (songPath.equals(tableValue)) {
                                selectedPath = tableValue;
                            }

                        }

                        playDB.deleteSong(playlistName, selectedPath);
                        populatePlaylist(playlistName);
                        scrollPane.setViewportView(plTable);
                       // stopFileCol(plTable);


                }

            }


        });

        addToPlaylist.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {
                addToPlaylist.doClick();
            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        Vector<String> names = playDB.buildPlaylistTree();
        menuItems = new ArrayList<JMenuItem>();

        for(int i = 0; i < names.size(); i++){
            String playName = names.get(i);
            menuItems.add(new JMenuItem(names.get(i)));
            menuItems.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //add song to the playlist
                    String selectedPath = table.getModel().getValueAt(currentSelectedRow, 0).toString().trim();
                    boolean playlistContains = false;
                    String currentList = playDB.searchDB(playName);
                    ArrayList<String> songPathList = new ArrayList<String>();

                    if(!currentList.trim().equals("empty")) {
                        String[] tokens = currentList.trim().split("\\s*,\\s*");
                        for (String s : tokens) {
                            songPathList.add(s);
                        }
                    }

                    for(int i = 0; i < songPathList.size(); i++) {
                        if(songPathList.get(i).equals(selectedPath)){
                            playlistContains = true;
                        }
                    }

                    if(!playlistContains) {
                        playDB.addSong(playName, selectedPath);
                        if (windowList.size() > 0) {
                            for (int i = 0; i < windowList.size(); i++) {
                                windowList.get(i).populatePlaylist(windowList.get(i).getName());
                                windowList.get(i).scrollPane.setViewportView(windowList.get(i).plTable);
                               // stopFileCol(windowList.get(i).plTable);
                            }
                        }
                    }

                }

            });
            addToPlaylist.add(menuItems.get(i));
        }


        popupLibraryMenu.add(newFilepop);

        popupLibraryMenu.add(deleteFilepop);

        popupLibraryMenu.add(addToPlaylist);


        mouseListenerpop = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                currentSelectedRow = table.getSelectedRow();
                highlightedSongPath = (table.getValueAt(currentSelectedRow,0)).toString();
            }
        };

        ////////////end popup//////////////////////
    }

    public String getName(){
        return playlistName;
    }

    public void updatePlaylistPopUp(){
        Vector<JMenuItem> temp = new Vector<JMenuItem>();
        Vector<String> names = playDB.buildPlaylistTree();
        int index = names.size()-1;
        String playName = names.get(index);
        temp.add(new JMenuItem(names.get(index)));
        temp.get(0).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //add song to the playlist
                //duplicate code
                String selectedPath = table.getModel().getValueAt(currentSelectedRow, 0).toString().trim();
                boolean playlistContains = false;
                String currentList = playDB.searchDB(playName);
                ArrayList<String> songPathList = new ArrayList<String>();

                if(!currentList.trim().equals("empty")) {
                    String[] tokens = currentList.trim().split("\\s*,\\s*");
                    for (String s : tokens) {
                        songPathList.add(s);
                    }
                }

                for(int i = 0; i < songPathList.size(); i++) {
                    if(songPathList.get(i).equals(selectedPath)){
                        playlistContains = true;
                    }
                }
                if(!playlistContains) {
                    playDB.addSong(playName, selectedPath);
                    if (windowList.size() > 0) {
                        for (int i = 0; i < windowList.size(); i++) {
                            windowList.get(i).populatePlaylist(windowList.get(i).getName());
                            windowList.get(i).scrollPane.setViewportView(windowList.get(i).plTable);
                            //stopFileCol(windowList.get(i).plTable);
                        }
                    }
                }

            }

        });
        addToPlaylist.add(temp.get(0));
        //menuItems.add(temp.get(0));


    }

    //create popup menu and options
    public void createTreePopup(){
        popupTreeMenu = new JPopupMenu();
        newWindowPop = new JMenuItem("Open in New Window");
        deleteplayPop = new JMenuItem("Delete");


        MusicPlayerGUI mainGUI = this;
        //listener for adding a file in popup
        newWindowPop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JFrame playlist = new JFrame("");
                // playlist.setName(selectedNode.getUserObject().toString());
                if(selectedNode.getUserObject() != "Library" && selectedNode.getUserObject() != "Playlists"){
                    scrollPane.setViewportView(table);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    tree.clearSelection();

                    //createPlaylistMain(playlist);
                    MusicPlayerGUI playlist = new MusicPlayerGUI(library,playDB, selectedNode.getUserObject().toString(), mainGUI, recentDB);
                    playlist.go();
                    windowList.add(playlist);
                    windowIndex++;
                }
            }
        });

        deleteplayPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                String name = selectedNode.getUserObject().toString().trim();
                model = (DefaultTreeModel)tree.getModel();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
                DefaultMutableTreeNode play = (DefaultMutableTreeNode)root.getChildAt(1);
                play.remove(selectedNode);
                model.reload(root);
                //if window is open, close it.
                for (int i = 0; i < windowList.size(); i++) {
                    JFrame temp = windowList.get(i).main;
                    String frameName = windowList.get(i).getName().trim();
                    if(frameName.equals(name)){
                        temp.dispatchEvent(new WindowEvent(temp, WindowEvent.WINDOW_CLOSING));
                        windowList.remove(i);
                    }
                }
                playDB.deletePlaylist(name);

                //repopulate popup playlist list
                Vector<String> names = playDB.buildPlaylistTree();
                addToPlaylist.removeAll();
                menuItems.removeAll(menuItems);
                for(int i = 0; i < names.size(); i++){
                    String playName = names.get(i);
                    menuItems.add(new JMenuItem(names.get(i)));
                    menuItems.get(i).addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //add song to the playlist
                            String selectedPath = table.getModel().getValueAt(currentSelectedRow, 0).toString().trim();
                            boolean playlistContains = false;
                            String currentList = playDB.searchDB(playName);
                            ArrayList<String> songPathList = new ArrayList<String>();

                            if(!currentList.trim().equals("empty")) {
                                String[] tokens = currentList.trim().split("\\s*,\\s*");
                                for (String s : tokens) {
                                    songPathList.add(s);
                                }
                            }

                            for(int i = 0; i < songPathList.size(); i++) {
                                if(songPathList.get(i).equals(selectedPath)){
                                    playlistContains = true;
                                }
                            }

                            if(!playlistContains) {
                                playDB.addSong(playName, selectedPath);
                                if (windowList.size() > 0) {
                                    for (int i = 0; i < windowList.size(); i++) {
                                        windowList.get(i).populatePlaylist(windowList.get(i).getName());
                                        windowList.get(i).scrollPane.setViewportView(windowList.get(i).plTable);
                                    }
                                }
                            }

                        }

                    });
                    addToPlaylist.add(menuItems.get(i));
                }
                scrollPane.setViewportView(table);
                scrollPane.revalidate();
                scrollPane.repaint();


            }
        });


        popupTreeMenu.add(newWindowPop);
        popupTreeMenu.add(deleteplayPop);
        //change view of the main window
        mouseListenerTree = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                if(e.getClickCount()==2){

                    if(selectedNode.getUserObject() == "Library"){
                        isPlaylist = false;
                        playlistName = "";
                        popupLibraryMenu.remove(deleteFilepop);
                        popupLibraryMenu.add(newFilepop);
                        popupLibraryMenu.add(deleteFilepop);
                        popupLibraryMenu.add(addToPlaylist);

                        header = table.getTableHeader();
                        createHeader();
                        header.setComponentPopupMenu(popupHeader);
                        table.setTableHeader(header);
                        //hideAllCol(table);

                        //stopFileCol(plTable);
                        redrawColumns(table);

                        table.setComponentPopupMenu(popupLibraryMenu);
                        scrollPane.setViewportView(table);
                        //currentTable = table;
                        scrollPane.revalidate();
                        scrollPane.repaint();
                    }
                    else if(selectedNode.getUserObject() != "Playlists") {
                        isPlaylist = true;
                        playlistName = selectedNode.getUserObject().toString();
                        populatePlaylist(selectedNode.getUserObject().toString());
                        popupLibraryMenu.remove(addToPlaylist);
                        popupLibraryMenu.remove(newFilepop);
                        plTable.setComponentPopupMenu(popupLibraryMenu);
                        header = plTable.getTableHeader();
                        createHeader();
                        header.setComponentPopupMenu(popupHeader);
                        plTable.setTableHeader(header);
                        redrawColumns(plTable);
                        scrollPane.setViewportView(plTable);
                        //currentTable = plTable;
                        createHeader();
                        scrollPane.revalidate();
                        scrollPane.repaint();
                    }
                }
            }
        };

        ////////////end popup//////////////////////
    }


    //Button listener instructions
    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(">".equals(e.getActionCommand())) {
                try {
                    if(isShuffled && !isRepeating){
                        Random rand = new Random();
                        int shuffledIndex;
                        if (!isPlaylist) {
                            shuffledIndex = rand.nextInt(table.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(table.getRowCount());
                            }
                        } else {
                            shuffledIndex = rand.nextInt(plTable.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(plTable.getRowCount());
                            }
                        }
                        currentSelectedRow = shuffledIndex;
                    }
                    if(!isPlaylist) {
                        currentSong.setContents((table.getValueAt(currentSelectedRow, 0)).toString(), (table.getValueAt(currentSelectedRow, 1)).toString(),
                                currentSelectedRow, 0);
                        table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                        player.open(new File(currentSong.getPath()));
                        currentTable = table;
                        player.play();

                    } else {
                        currentSong.setContents((plTable.getValueAt(currentSelectedRow, 0)).toString(), (plTable.getValueAt(currentSelectedRow, 1)).toString(),
                                currentSelectedRow, 0);
                        plTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                        player.open(new File(currentSong.getPath()));
                        currentTable = plTable;
                        player.play();
                    }
                    player.setGain(volume);
                } catch (BasicPlayerException ex) {
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if("=".equals(e.getActionCommand())) {
                try {
                    if(player.getStatus() == 1){
                        player.resume();
                        player.setGain(volume);
                    } else if(player.getStatus() == 0){
                        player.pause();
                    }
                } catch (BasicPlayerException ex) {
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if("[]".equals(e.getActionCommand())) {
                try {
                    manuallyClicked = true;
                    player.stop();
                } catch (BasicPlayerException ex) {
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if("|<".equals(e.getActionCommand())) {
                try {
                    if(isShuffled){
                        Random rand = new Random();
                        int shuffledIndex;
                        if (!isPlaylist) {
                            shuffledIndex = rand.nextInt(table.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(table.getRowCount());
                            }
                        } else {
                            shuffledIndex = rand.nextInt(plTable.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(plTable.getRowCount());
                            }
                        }
                        currentSelectedRow = shuffledIndex;
                        play.doClick();
                    } else if(!isPlaylist) {
                        manuallyClicked = true;
                        String firstSong = (table.getValueAt(0, 0).toString());
                        if (firstSong == currentSong.getPath()) {
                            currentSong.setContents((table.getValueAt(table.getModel().getRowCount() - 1, 0).toString()),(table.getValueAt(table.getModel().getRowCount() - 1, 1).toString()), table.getModel().getRowCount() - 1, 0);
                            table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            currentTable = table;
                            player.play();
                        } else {
                            currentSong.setContents((table.getValueAt(currentSong.getRow() - 1, 0)).toString(), (table.getValueAt(currentSong.getRow() - 1, 1)).toString(),currentSong.getRow() - 1, 0);
                            table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            currentTable = table;
                            player.play();
                        }
                    } else {
                        manuallyClicked = true;
                        String firstSong = (plTable.getValueAt(0,0).toString());
                        if(firstSong == currentSong.getPath()){
                            currentSong.setContents((plTable.getValueAt(plTable.getModel().getRowCount()-1,0).toString()),((plTable.getValueAt(currentSong.getRow() - 1, 1)).toString()), plTable.getModel().getRowCount()-1, 0);
                            plTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            currentTable = plTable;
                            player.play();
                        } else {
                            currentSong.setContents((plTable.getValueAt(currentSong.getRow()-1, 0)).toString(), (plTable.getValueAt(currentSong.getRow() - 1, 1)).toString(),currentSong.getRow()-1, 0);
                            plTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            currentTable = plTable;
                            player.play();
                        }
                    }
                    player.setGain(volume);
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(">|".equals(e.getActionCommand())) {
                try {
                    if(isShuffled){
                        Random rand = new Random();
                        int shuffledIndex;
                        if (!isPlaylist) {
                            shuffledIndex = rand.nextInt(table.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(table.getRowCount());
                            }
                        } else {
                            shuffledIndex = rand.nextInt(plTable.getRowCount());
                            while(currentSong.getRow() == shuffledIndex){
                                shuffledIndex = rand.nextInt(plTable.getRowCount());
                            }
                        }
                        currentSelectedRow = shuffledIndex;
                        play.doClick();
                    }else if(!isPlaylist){
                        manuallyClicked = true;
                        String lastSong = (table.getValueAt(table.getModel().getRowCount()-1,0).toString());
                        if(lastSong == currentSong.getPath()){
                            //highlightedSongPath = (table.getValueAt(0,0).toString());
                            currentSong.setContents((table.getValueAt(0,0).toString()), (table.getValueAt(0,1).toString()), 0, 0);
                            table.setRowSelectionInterval(0, 0);
                            player.open(new File(currentSong.getPath()));
                            currentTable = table;
                            player.play();
                        } else {
                            currentSong.setContents((table.getValueAt(currentSong.getRow()+1, 0)).toString(), (table.getValueAt(currentSong.getRow()+1, 1)).toString(), currentSong.getRow()+1, 0);
                            table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            currentTable = table;
                            player.play();
                        }
                    } else {
                        manuallyClicked = true;
                        String lastSong = (plTable.getValueAt(plTable.getModel().getRowCount() - 1, 0).toString());
                        if (lastSong == currentSong.getPath()) {
                            currentSong.setContents((plTable.getValueAt(0, 0).toString()), (plTable.getValueAt(0, 1).toString()), 0, 0);
                            plTable.setRowSelectionInterval(0, 0);
                            player.open(new File(currentSong.getPath()));
                            currentTable = plTable;
                            player.play();
                        } else {
                            currentSong.setContents((plTable.getValueAt(currentSong.getRow() + 1, 0)).toString(), (plTable.getValueAt(currentSong.getRow() + 1, 1)).toString(),  currentSong.getRow() + 1, 0);
                            plTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            currentTable = plTable;
                            player.play();
                        }
                    }
                    player.setGain(volume);
                } catch (BasicPlayerException ex) {

                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    //Creating a class for Drag and Drop

    //public class DropThatThing extends TransferHandler {


    //}

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
                    //String whatever = transfer.getTransferData(flavor).toString();
                    if(flavor.isFlavorJavaFileListType())
                    {

                        List<File> files = (List<File>) transfer.getTransferData(flavor);
                        for(File file : files)
                        {
                            if(library.insertSong(file.getAbsolutePath()) == false && !isPlaylist){
                                JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
                            }

                            table.setModel(library.buildSongsTable()); //refresh table
                            redrawColumns(table);
                            //stopFileCol(table);
                            //hideAllCol(table);

                            //adding song by menu
                            //Enter into playlist
                            if(isPlaylist) {
                                String songPath = file.getAbsolutePath();
                                String songName = "0";
                                for (int i = 0; i < table.getRowCount(); i++) {
                                    table.setRowSelectionInterval(i, i);
                                    String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                                    if(songPath.equals(tableValue)){
                                        songName = table.getModel().getValueAt(i,1).toString().trim();
                                        dragAndDropPlaylist(songName);
                                    }

                                }

                            }

                            table.setModel(library.buildSongsTable()); //refresh table
                            redrawColumns(table);
                            //stopFileCol(table);
                            //hideAllCol(table);


                        }
                    }
                    else
                    {
                        //transfer.getTransferData(flavor).toString()
                        String[] splits = transfer.getTransferData(flavor).toString().split("\n");
                        String[] goodSplits = new String[splits.length];
                        for (int i = 0; i < splits.length; i++)
                        {
                            goodSplits[i] = splits[i].split(".mp3")[0].concat(".mp3");
                        }

                        File tempFile;

                        for(int j = 0; j < goodSplits.length; j++)
                        {
                            tempFile = new File(goodSplits[j]);
                            if(library.insertSong(tempFile.getAbsolutePath()) == false && !isPlaylist){
                                JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            String currentList = playDB.searchDB(playlistName);

                            table.setModel(library.buildSongsTable()); //refresh table
                            redrawColumns(table);
                           // stopFileCol(table);
                           // hideAllCol(table);

                            //Enter into playlist
                            if(isPlaylist) {
                                //String songPath = dialog.getSelectedFile().getAbsolutePath();
                                String songPath = tempFile.getAbsolutePath();
                                String selectedPath = "0";
                                for (int i = 0; i < table.getRowCount(); i++) {
                                    table.setRowSelectionInterval(i, i);
                                    String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                                    if(songPath.equals(tableValue)){
                                        selectedPath = tableValue;
                                        dragAndDropPlaylist(selectedPath);
                                    }

                                }
                                scrollPane.setViewportView(plTable);
                                //stopFileCol(plTable);

                                table.setModel(library.buildSongsTable()); //refresh table
                                redrawColumns(table);
                                //stopFileCol(table);
                                if(origin != null){
                                    origin.table.setModel(library.buildSongsTable());
                                    redrawColumns(origin.table);
                                    //stopFileCol(origin.table);
                                    //hideAllCol(origin.table);
                                }


                            }
                        }
                    }

                    redrawColumns(table);
                }catch(Exception problem)
                {
                    JOptionPane.showMessageDialog(null, problem);
                }
                popupLibraryMenu.remove(addToPlaylist);
                popupLibraryMenu.remove(newFilepop);
                plTable.setComponentPopupMenu(popupLibraryMenu);
            }
        }
    }

    public void dragAndDropPlaylist(String selectedPath){
        //String indexStr = library.searchDB(songPath);
        boolean playlistContains = false;
        String currentList = playDB.searchDB(playlistName);
        ArrayList<String> songPathList = new ArrayList<String>();
        String temp = currentList.trim();
        if(!temp.equals("empty")) {
            String[] tokens = currentList.trim().split("\\s*,\\s*");
            for (String s : tokens) {
                songPathList.add(s);
            }
        }

        for(int i = 0; i < songPathList.size(); i++) {
            if(songPathList.get(i).equals(selectedPath)){
                playlistContains = true;
            }
        }

        if(!playlistContains) {
            playDB.addSong(playlistName, selectedPath);
            populatePlaylist(playlistName);
        }
        scrollPane.setViewportView(plTable);
        //stopFileCol(plTable);

        table.setModel(library.buildSongsTable()); //refresh table
        redrawColumns(table);
        //stopFileCol(table);
        //hideAllCol(table);
        if(origin != null){
            origin.table.setModel(library.buildSongsTable());
            redrawColumns(origin.table);
            //stopFileCol(origin.table);
            //hideAllCol(origin.table);
        }

    }

    public void populateRecents(){
        recentMenu.removeAll();
        songMenuItems.removeAll(songMenuItems);

        Vector<String> namesList = recentDB.buildNamesList();

        for(int i = 0; i < namesList.size(); i++){
            String songName = namesList.get(i).trim();
            songMenuItems.add(new JMenuItem(songName));
            songMenuItems.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(int i = 0; i < currentTable.getRowCount(); i++){
                        if(currentTable.getValueAt(i,1).toString().trim().equals(songName)){
                            currentSelectedRow = i;
                            play.doClick();
                        }
                    }
                }
            });
            recentMenu.add(songMenuItems.get(i));
        }
    }

    public void populatePlaylist(String playlistName){
        ArrayList<String> songPathList = new ArrayList<String>();
        String currentList = playDB.searchDB(playlistName);
        if(!currentList.trim().equals("empty")) {
            String[] tokens = currentList.trim().split("\\s*,\\s*");
            for (String s : tokens) {
                songPathList.add(s);
            }
            if (songPathList.size() > 0) {
                TableModel original = table.getModel();
                DefaultTableModel playlistModel = new DefaultTableModel(songPathList.size(), original.getColumnCount());
                plTable = new JTable(playlistModel);
                plTable.setDragEnabled(true);

                //take indexes of original table and put into playlist
                for (int i = 0; i < original.getColumnCount(); i++) {
                    plTable.getColumnModel().getColumn(i).setHeaderValue(original.getColumnName(i));
                    //playlistModel.addColumn(original.getColumnName(i));
                }


                for (int i = 0; i < songPathList.size(); i++) {
                    int index = 0;
                    for (int j = 0; j < table.getRowCount(); j++) {
                        table.setRowSelectionInterval(j, j);
                        String tableValue = table.getModel().getValueAt(j, 0).toString().trim();
                        if ((songPathList.get(i)).equals(tableValue)) {
                            index = j;
                        }
                    }
                    table.setRowSelectionInterval(index, index);
                    int row = table.getSelectedRow();
                    int modelRow = table.convertRowIndexToModel(row);
                    //playlistModel.setRowCount(songNameList.size());
                    for (int col = 0; col < original.getColumnCount(); col++) {
                        playlistModel.setValueAt(original.getValueAt(modelRow, col), i, col);
                    }

                }
                mouseListenerpop = new MouseAdapter() {
                    //this will print the selected row index when a user clicks the table
                    public void mousePressed(MouseEvent e) {
                        currentSelectedRow = plTable.getSelectedRow();
                        highlightedSongPath = (plTable.getValueAt(currentSelectedRow,0)).toString();
                    }
                };
                plTable.addMouseListener(mouseListenerpop);
                plTable.setDefaultEditor(Object.class, null);
            }
        } else {
            plTable = new JTable();
        }

    }

    //Start GUI
    public void go(){
        if(isPlaylist){
            main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        else {
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        main.setVisible(true);
    }

    public void redrawColumns(JTable table) {
        toggleColumn(table, "FILE", filePop.getState());
        toggleColumn(table, "TITLE", titlePop.getState());
        toggleColumn(table, "ALBUM", albumPop.getState());
        toggleColumn(table, "ARTIST", artistPop.getState());
        toggleColumn(table, "YEAR", yearPop.getState());
        toggleColumn(table, "GENRE", genrePop.getState());
        toggleColumn(table, "COMMENT", commentPop.getState());
    }



}


