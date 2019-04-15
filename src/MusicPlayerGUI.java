import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import javax.swing.JPopupMenu;
import javax.swing.tree.*;

import com.mpatric.mp3agic.*;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

public class MusicPlayerGUI extends JFrame{

    BasicPlayer player;

    String highlightedSongPath;
    JFrame  main = new JFrame("Music Player");
    JTable table, plTable;
    JTree tree;
    JScrollPane scrollPane, sourceScrollPane;
    JSplitPane splitPane;
    JButton play, stop, pause, skip, previous;
    JPanel buttonPanel, musicPanel;
    JMenuBar menuBar;
    JMenu menu,addToPlaylist;
    JPopupMenu popupLibraryMenu, popupTreeMenu;
    JMenuItem newFile, deleteFile, open, newPlaylist, close;
    JMenuItem newFilepop, deleteFilepop, newWindowPop, deleteplayPop;
    MouseListener mouseListenerpop,mouseListenerTree;
    DefaultTreeModel model;
    ButtonListener bl;
    ActionListener al;
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    List<MusicPlayerGUI> windowList = new ArrayList<MusicPlayerGUI>();
    int windowIndex = 0;
    DefaultMutableTreeNode playlistNode, selectedNode;
    int CurrentSelectedRow;
    Mp3File songNotInLibrary;
    boolean isExistingInLibrary = true;
    boolean isPlaylist = false;
    Library library;
    PlaylistDatabase playDB;
    Song currentSong = new Song();
    String playlistName;
    double volume;
    //Connecting the DragAndDrop class to the main JFrame
    DragAndDrop dndObj = new DragAndDrop();
    DropTarget targetOfDND = new DropTarget(main,dndObj);

    public MusicPlayerGUI(Library lib, PlaylistDatabase playDataB) {
        library = lib;
        playDB = playDataB;
        player = new BasicPlayer();
        buttonPanel = new JPanel();
        musicPanel = new JPanel();

        createMain();
    }

    //constructor for playlists
    public MusicPlayerGUI(Library lib, PlaylistDatabase playDataB, String name) {
        library = lib;
        playDB = playDataB;
        player = new BasicPlayer();
        buttonPanel = new JPanel();
        musicPanel = new JPanel();
        isPlaylist = true;
        createPlaylistMain(name);
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
        createLibraryPopup();
        createTable();
        displayLibraryTable();
        createButtons();

        main.setSize(1500,700);
        main.setJMenuBar(menuBar);
        main.add(splitPane, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);
        main.setLocationRelativeTo(null);
    }

    public void createPlaylistMain(String name){
        main.setLayout(new BorderLayout());
        playlistName = name;

        bl=new ButtonListener();
        menuBar = new JMenuBar();

        createMenu();
        createLibraryPopup();
        createTable();
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

    //table creation and mouseListener.
    public void createTable(){

        table = new JTable(library.buildSongsTable());
        table.setDefaultEditor(Object.class, null);
        /***
         MouseListener mouseListener = new MouseAdapter() {
         //this will print the selected row index when a user clicks the table
         public void mousePressed(MouseEvent e) {
         CurrentSelectedRow = table.getSelectedRow();
         highlightedSongPath = (table.getValueAt(CurrentSelectedRow,0)).toString();
         }
         };
         table.addMouseListener(mouseListener);
         ***/
        // sets the popup menu for the table
        table.setComponentPopupMenu(popupLibraryMenu);
        table.addMouseListener(mouseListenerpop);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(250);
        column = table.getColumnModel().getColumn(1);
        column.setPreferredWidth(20);

        createTree();


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
        scrollPane = new JScrollPane(plTable);

        Dimension minimumSize = new Dimension(100, 50);
        scrollPane.setMinimumSize(minimumSize);

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
            JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
        }

        getNode.setUserObject(input);
        model.insertNodeInto(getNode, playlistNode, playlistNode.getChildCount());
        expandAllNodes(1,tree.getRowCount());
        TreeNode[] nodes = model.getPathToRoot(getNode);
        TreePath treePath = new TreePath(nodes);
        tree.scrollPathToVisible(treePath);
        tree.setSelectionPath(treePath);
        updatePlaylistPopUp();
        plTable = new JTable();
        scrollPane.setViewportView(plTable);
        scrollPane.revalidate();
        scrollPane.repaint();
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
        JSlider slider = new JSlider();
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
                    if(isPlaylist) {
                        String songPath = dialog.getSelectedFile().getAbsolutePath();
                        String index = "0";

                        for (int i = 0; i < table.getRowCount(); i++) {
                            table.setRowSelectionInterval(i, i);
                            //.out.println("Song Path: " + songPath);
                            //System.out.println("Table Path: " + table.getModel().getValueAt(i,0));
                            String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                            if(songPath.equals(tableValue)){
                                index = Integer.toString(i);
                                //System.out.println("library song index: " + index);
                            }

                        }
                        //String indexStr = library.searchDB(songPath);
                        boolean playlistContains = false;
                        String currentList = playDB.searchDB(playlistName);
                        ArrayList<Integer> songIndexList = new ArrayList<Integer>();

                        if(!currentList.trim().equals("empty")) {
                            //System.out.println("INSIDE PLAYLIST");
                            String[] tokens = currentList.trim().split("\\s*,\\s*");
                            for (String s : tokens) {
                                songIndexList.add(Integer.parseInt(s));
                            }
                        }

                        for(int i = 0; i < songIndexList.size(); i++) {
                            if(songIndexList.get(i) == Integer.parseInt(index)){
                                playlistContains = true;
                            }
                        }

                        if(!playlistContains) {
                            playDB.addSong(playlistName, index);
                            populatePlaylist(playlistName);
                        }
                        scrollPane.setViewportView(plTable);

                    }
                }
            }

        });

        //listener for deleting a file
        deleteFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                library.deleteSong(highlightedSongPath);
                table.setModel(library.buildSongsTable()); //refresh table
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
        menu.addSeparator();
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
                }
            }

        });

        //listener for deleting a file in popup
        deleteFilepop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                library.deleteSong(highlightedSongPath);
                table.setModel(library.buildSongsTable()); //refresh table
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

        ArrayList<JMenuItem> menuItems;
        Vector<String> names = playDB.buildPlaylistTree();
        menuItems = new ArrayList<JMenuItem>();

        for(int i = 0; i < names.size(); i++){
            String playName = names.get(i);
            menuItems.add(new JMenuItem(names.get(i)));
            menuItems.get(i).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //add song to the playlist
                    String songIndex = Integer.toString(CurrentSelectedRow);
                    boolean playlistContains = false;
                    String currentList = playDB.searchDB(playName);
                    ArrayList<Integer> songIndexList = new ArrayList<Integer>();

                    if(!currentList.trim().equals("empty")) {
                        //System.out.println("INSIDE PLAYLIST");
                        String[] tokens = currentList.trim().split("\\s*,\\s*");
                        for (String s : tokens) {
                            songIndexList.add(Integer.parseInt(s));
                        }
                    }

                    for(int i = 0; i < songIndexList.size(); i++) {
                        if(songIndexList.get(i) == CurrentSelectedRow){
                            playlistContains = true;
                        }
                    }

                    if(!playlistContains) {
                        playDB.addSong(playName, songIndex);
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

        popupLibraryMenu.add(newFilepop);
        popupLibraryMenu.add(deleteFilepop);
        popupLibraryMenu.add(addToPlaylist);

        mouseListenerpop = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                CurrentSelectedRow = table.getSelectedRow();
                highlightedSongPath = (table.getValueAt(CurrentSelectedRow,0)).toString();
            }
        };

        ////////////end popup//////////////////////
    }

    public String getName(){
        return playlistName;
    }

    public void updatePlaylistPopUp(){
        ArrayList<JMenuItem> menuItems;
        Vector<String> names = playDB.buildPlaylistTree();
        menuItems = new ArrayList<JMenuItem>();
        int index = names.size()-1;
        String playName = names.get(index);
        menuItems.add(new JMenuItem(names.get(index)));
        menuItems.get(0).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //add song to the playlist
                String songIndex = Integer.toString(CurrentSelectedRow);
                boolean playlistContains = false;
                String currentList = playDB.searchDB(playName);
                ArrayList<Integer> songIndexList = new ArrayList<Integer>();

                if(!currentList.trim().equals("empty")) {
                    //System.out.println("INSIDE PLAYLIST");
                    String[] tokens = currentList.trim().split("\\s*,\\s*");
                    for (String s : tokens) {
                        songIndexList.add(Integer.parseInt(s));
                    }
                }

                for(int i = 0; i < songIndexList.size(); i++) {
                    if(songIndexList.get(i) == CurrentSelectedRow){
                        playlistContains = true;
                    }
                }
                if(!playlistContains) {
                    playDB.addSong(playName, songIndex);
                    if (windowList.size() > 0) {
                        for (int i = 0; i < windowList.size(); i++) {
                            windowList.get(i).populatePlaylist(windowList.get(i).getName());
                            windowList.get(i).scrollPane.setViewportView(windowList.get(i).plTable);
                        }
                    }
                }

            }

        });
        addToPlaylist.add(menuItems.get(0));


    }

    //create popup menu and options
    public void createTreePopup(){
        popupTreeMenu = new JPopupMenu();
        newWindowPop = new JMenuItem("Open in New Window");


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
                    MusicPlayerGUI playlist = new MusicPlayerGUI(library,playDB, selectedNode.getUserObject().toString());
                    playlist.go();
                    System.out.println("Added playlist: " +  playlist.getName());
                    windowList.add(playlist);
                    windowIndex++;
                }
            }
        });


        popupTreeMenu.add(newWindowPop);

        mouseListenerTree = new MouseAdapter() {
            //this will print the selected row index when a user clicks the table
            public void mousePressed(MouseEvent e) {
                selectedNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                if(e.getClickCount()==2){

                    if(selectedNode.getUserObject() == "Library"){
                        scrollPane.setViewportView(table);
                        scrollPane.revalidate();
                        scrollPane.repaint();
                    }
                    else if(selectedNode.getUserObject() != "Playlists") {
                        populatePlaylist(selectedNode.getUserObject().toString());
                        scrollPane.setViewportView(plTable);
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
                    // if(player.getStatus() == 1){
                    //  player.resume();
                    //} else {
                    //Mp3File mp3file = new Mp3File(highlightedSongPath);
                    currentSong.setContents((table.getValueAt(CurrentSelectedRow,0)).toString(),
                            CurrentSelectedRow, 0);
                    player.open(new File(currentSong.getPath()));
                    player.play();
                    //System.out.println(player.getStatus());
                    // }
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
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
                    if(firstSong == currentSong.getPath()){
                        currentSong.setContents((table.getValueAt(table.getModel().getRowCount()-1,0).toString()), table.getModel().getRowCount()-1, 0);
                        table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                        player.open(new File(currentSong.getPath()));
                        player.play();
                    } else {
                        currentSong.setContents((table.getValueAt(currentSong.getRow()-1, 0)).toString(), currentSong.getRow()-1, 0);
                        table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                        player.open(new File(currentSong.getPath()));
                        player.play();
                    }
                    //table.setModel(library.buildSongsTable()); //refresh table
                    //System.out.println(player.getStatus());
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(">|".equals(e.getActionCommand())) {
                try {
                    String lastSong = (table.getValueAt(table.getModel().getRowCount()-1,0).toString());
                    if(lastSong == currentSong.getPath()){
                        //highlightedSongPath = (table.getValueAt(0,0).toString());
                        currentSong.setContents((table.getValueAt(0,0).toString()), 0, 0);
                        table.setRowSelectionInterval(0, 0);
                        player.open(new File(currentSong.getPath()));
                        player.play();
                    } else {
                        //highlightedSongPath = (table.getValueAt(++CurrentSelectedRow, 0)).toString();
                        currentSong.setContents((table.getValueAt(currentSong.getRow()+1, 0)).toString(), currentSong.getRow()+1, 0);
                        table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                        player.open(new File(currentSong.getPath()));
                        player.play();
                    }
                    //table.setModel(library.buildSongsTable()); //refresh table
                    //System.out.println(highlightedSongPath);
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
                            if(library.insertSong(file.getAbsolutePath()) == false){
                                JOptionPane.showMessageDialog(musicPanel, "Song already exists", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                            table.setModel(library.buildSongsTable()); //refresh table
                        }
                    }
                }catch(Exception problem)
                {
                    JOptionPane.showMessageDialog(null, problem);
                }

            }
        }
    }

    public void populatePlaylist(String playlistName){
        ArrayList<Integer> songIndexes = new ArrayList<Integer>();
        String indexes = playDB.searchDB(playlistName);
        System.out.println(indexes);
        if(!indexes.trim().equals("empty")) {
            //System.out.println("INSIDE PLAYLIST");
            String[] tokens = indexes.trim().split("\\s*,\\s*");
            for (String s : tokens) {
                songIndexes.add(Integer.parseInt(s));
            }
            if (songIndexes.size() > 0) {
                TableModel original = table.getModel();
                DefaultTableModel playlistModel = new DefaultTableModel(songIndexes.size(), original.getColumnCount());
                plTable = new JTable(playlistModel);


                //take indexes of original table and put into playlist
                for (int i = 0; i < original.getColumnCount(); i++) {
                    plTable.getColumnModel().getColumn(i).setHeaderValue(original.getColumnName(i));
                    //playlistModel.addColumn(original.getColumnName(i));
                }

                for (int i = 0; i < songIndexes.size(); i++) {
                    table.setRowSelectionInterval(songIndexes.get(i), songIndexes.get(i));
                    int row = table.getSelectedRow();
                    int modelRow = table.convertRowIndexToModel(row);
                    //playlistModel.setRowCount(songIndexes.size());
                    for (int col = 0; col < original.getColumnCount(); col++) {
                        playlistModel.setValueAt(original.getValueAt(modelRow, col), i, col);
                    }
                }
                mouseListenerpop = new MouseAdapter() {
                    //this will print the selected row index when a user clicks the table
                    public void mousePressed(MouseEvent e) {
                        CurrentSelectedRow = plTable.getSelectedRow();
                        highlightedSongPath = (plTable.getValueAt(CurrentSelectedRow,0)).toString();
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
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }
}
