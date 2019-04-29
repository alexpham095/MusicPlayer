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
    JPopupMenu popupLibraryMenu, popupTreeMenu, popupHeader;
    JMenuItem newFile, deleteFile, open, newPlaylist, close;
    JMenuItem newFilepop, deleteFilepop, newWindowPop, deleteplayPop;
    JCheckBoxMenuItem albumPop, artistPop, yearPop, genrePop, commentPop;
    JTableHeader header;
    MouseListener mouseListenerpop,mouseListenerTree;
    DefaultTreeModel model;
    ButtonListener bl;
    ActionListener al;
    JFileChooser dialog = new JFileChooser(System.getProperty("user.dir"));
    List<MusicPlayerGUI> windowList = new ArrayList<MusicPlayerGUI>();
    ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();
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
    MusicPlayerGUI origin;

    public MusicPlayerGUI(Library lib, PlaylistDatabase playDataB) {
        library = lib;
        playDB = playDataB;
        player = new BasicPlayer();
        buttonPanel = new JPanel();
        musicPanel = new JPanel();

        createMain();
    }

    //constructor for playlists
    public MusicPlayerGUI(Library lib, PlaylistDatabase playDataB, String name, MusicPlayerGUI origin) {
        library = lib;
        playDB = playDataB;
        player = new BasicPlayer();
        buttonPanel = new JPanel();
        musicPanel = new JPanel();
        isPlaylist = true;
        this.origin = origin;
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
        //stopFileCol(table);
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
        main.setTitle(playlistName);

        bl=new ButtonListener();
        menuBar = new JMenuBar();

        createMenu();
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

    //table creation and mouseListener.
    public void createTable(){

        table = new JTable(library.buildSongsTable());
        table.setDefaultEditor(Object.class, null);
        table.setDragEnabled(true);
        DragAndDrop plDnD = new DragAndDrop();
        DropTarget targetOfPLDND = new DropTarget(main,plDnD);

        // sets the popup menu for the table
        table.setComponentPopupMenu(popupLibraryMenu);
        table.addMouseListener(mouseListenerpop);
        TableColumn column = table.getColumnModel().getColumn(0);
        //column.setMinWidth(0);
        //column.setMaxWidth(0);
        //column.setPreferredWidth(0);
        column = table.getColumnModel().getColumn(1);
        column.setPreferredWidth(200);

        //createHeader();
        //header = table.getTableHeader();
        //header.setComponentPopupMenu(popupHeader);
        //hideAllCol(table);

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
        popupLibraryMenu.remove(addToPlaylist);
        popupLibraryMenu.remove(newFilepop);
        plTable.setComponentPopupMenu(popupLibraryMenu);
        scrollPane = new JScrollPane(plTable);
        //stopFileCol(plTable);


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
        JCheckBoxMenuItem[] activeCol = {albumPop, artistPop, yearPop, genrePop, commentPop};
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
                    //stopFileCol(table);
                    //hideAllCol(table);

                    if(isPlaylist) {
                        String songPath = dialog.getSelectedFile().getAbsolutePath();
                        String index = "0";

                        for (int i = 0; i < table.getRowCount(); i++) {
                            table.setRowSelectionInterval(i, i);
                            String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                            if(songPath.equals(tableValue)){
                                index = Integer.toString(i);
                            }

                        }
                        //String indexStr = library.searchDB(songPath);
                        boolean playlistContains = false;
                        String currentList = playDB.searchDB(playlistName);
                        ArrayList<Integer> songIndexList = new ArrayList<Integer>();

                        if(!currentList.trim().equals("empty")) {
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
                        //stopFileCol(plTable);

                    }
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
                        String index = "";
                        for (int i = 0; i < table.getRowCount(); i++) {
                            table.setRowSelectionInterval(i, i);

                            String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                            if(songPath.equals(tableValue)){
                                index = Integer.toString(i);
                            }

                        }
                        playDB.deleteSong(name, index);
                        temp.populatePlaylist(name);
                        temp.scrollPane.setViewportView(temp.plTable);
                        //stopFileCol(plTable);

                    }
                    library.deleteSong(highlightedSongPath);
                    table.setModel(library.buildSongsTable()); //refresh table
                    //stopFileCol(table);
                   // hideAllCol(table);
                }
                else
                {
                    String songPath = highlightedSongPath.trim();
                    String index = "";
                    for (int i = 0; i < table.getRowCount(); i++) {
                        table.setRowSelectionInterval(i, i);
                        String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                        if(songPath.equals(tableValue)){
                            index = Integer.toString(i);
                        }

                    }
                    playDB.deleteSong(playlistName, index);
                    populatePlaylist(playlistName);
                    scrollPane.setViewportView(plTable);
                    //stopFileCol(plTable);
                }
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

    public void createHeader(){
        popupHeader = new JPopupMenu();
        albumPop = new JCheckBoxMenuItem("ALBUM");
        artistPop = new JCheckBoxMenuItem("ARTIST");
        yearPop = new JCheckBoxMenuItem("YEAR");
        genrePop = new JCheckBoxMenuItem("GENRE");
        commentPop = new JCheckBoxMenuItem("COMMENT");


        albumPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                //toggleColumn(table, "ALBUM", albumPop.getState());

            }
        });

        artistPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                //toggleColumn(table, "ARTIST", artistPop.getState());

            }
        });

        yearPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                //toggleColumn(table, "YEAR", yearPop.getState());

            }
        });

        genrePop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                //toggleColumn(table, "GENRE", genrePop.getState());

            }
        });

        commentPop.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
               // toggleColumn(table, "COMMENT", commentPop.getState());

            }
        });

        popupHeader.add(albumPop);
        popupHeader.add(artistPop);
        popupHeader.add(yearPop);
        popupHeader.add(genrePop);
        popupHeader.add(commentPop);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });
    }

    public void toggleColumn(JTable table, String column, boolean check){

        if(table.getRowCount() != 0) {
            int toggle = 0;
            if(check){
                toggle = 250;
            }

            for(int i = 2; i < table.getColumnCount(); i++){
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
                    if(windowList.size() == 0){
                        Vector<String> playlistList = playDB.buildPlaylistTree();
                        for (int j = 0; j < playlistList.size(); j++) {
                            String name = playlistList.get(j);
                            String songPath = highlightedSongPath.trim();
                            String index = "";
                            for (int i = 0; i < table.getRowCount(); i++) {
                                table.setRowSelectionInterval(i, i);

                                String tableValue = table.getModel().getValueAt(i, 0).toString().trim();
                                if (songPath.equals(tableValue)) {
                                    index = Integer.toString(i);
                                }

                            }

                            if(name != null) {
                                playDB.deleteSong(name, index);
                                populatePlaylist(name);
                            }

                        }
                    }
                    else {
                        for (int j = 0; j < windowList.size(); j++) {
                            MusicPlayerGUI temp = windowList.get(j);
                            String name = temp.playlistName;
                            String songPath = highlightedSongPath.trim();
                            String index = "";
                            for (int i = 0; i < table.getRowCount(); i++) {
                                table.setRowSelectionInterval(i, i);
                                String tableValue = table.getModel().getValueAt(i, 0).toString().trim();
                                if (songPath.equals(tableValue)) {
                                    index = Integer.toString(i);
                                }

                            }
                            playDB.deleteSong(name, index);
                            temp.populatePlaylist(name);
                            temp.scrollPane.setViewportView(temp.plTable);
                            //stopFileCol(temp.plTable);

                        }
                    }
                    library.deleteSong(highlightedSongPath.trim());
                    table.setModel(library.buildSongsTable()); //refresh table
                    //stopFileCol(table);
                    //hideAllCol(table);

                }
                else
                {
                        String songPath = highlightedSongPath.trim();
                        String index = "";
                        for (int i = 0; i < table.getRowCount(); i++) {
                            table.setRowSelectionInterval(i, i);
                            String tableValue = table.getModel().getValueAt(i, 0).toString().trim();
                            if (songPath.equals(tableValue)) {
                                index = Integer.toString(i);
                            }

                        }

                        playDB.deleteSong(playlistName, index);
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
                    String songIndex = Integer.toString(CurrentSelectedRow);
                    boolean playlistContains = false;
                    String currentList = playDB.searchDB(playName);
                    ArrayList<Integer> songIndexList = new ArrayList<Integer>();

                    if(!currentList.trim().equals("empty")) {
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
        Vector<JMenuItem> temp = new Vector<JMenuItem>();
        Vector<String> names = playDB.buildPlaylistTree();
        int index = names.size()-1;
        String playName = names.get(index);
        temp.add(new JMenuItem(names.get(index)));
        temp.get(0).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //add song to the playlist
                String songIndex = Integer.toString(CurrentSelectedRow);
                boolean playlistContains = false;
                String currentList = playDB.searchDB(playName);
                ArrayList<Integer> songIndexList = new ArrayList<Integer>();

                if(!currentList.trim().equals("empty")) {
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
                    MusicPlayerGUI playlist = new MusicPlayerGUI(library,playDB, selectedNode.getUserObject().toString(), mainGUI);
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
                            String songIndex = Integer.toString(CurrentSelectedRow);
                            boolean playlistContains = false;
                            String currentList = playDB.searchDB(playName);
                            ArrayList<Integer> songIndexList = new ArrayList<Integer>();

                            if(!currentList.trim().equals("empty")) {
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

                        table.setComponentPopupMenu(popupLibraryMenu);
                        scrollPane.setViewportView(table);
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
                        //stopFileCol(plTable);
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
                    if(!isPlaylist) {
                        currentSong.setContents((table.getValueAt(CurrentSelectedRow, 0)).toString(),
                                CurrentSelectedRow, 0);
                        player.open(new File(currentSong.getPath()));
                        player.play();
                    } else {
                        currentSong.setContents((plTable.getValueAt(CurrentSelectedRow, 0)).toString(),
                                CurrentSelectedRow, 0);
                        player.open(new File(currentSong.getPath()));
                        player.play();
                    }
                } catch (BasicPlayerException ex) {
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if("=".equals(e.getActionCommand())) {
                try {
                    if(player.getStatus() == 1){
                        player.resume();
                    } else if(player.getStatus() == 0){
                        player.pause();
                    }

                } catch (BasicPlayerException ex) {
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if("[]".equals(e.getActionCommand())) {
                try {
                    player.stop();
                } catch (BasicPlayerException ex) {
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if("|<".equals(e.getActionCommand())) {
                try {
                    if(!isPlaylist) {
                        String firstSong = (table.getValueAt(0, 0).toString());
                        if (firstSong == currentSong.getPath()) {
                            currentSong.setContents((table.getValueAt(table.getModel().getRowCount() - 1, 0).toString()), table.getModel().getRowCount() - 1, 0);
                            table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        } else {
                            currentSong.setContents((table.getValueAt(currentSong.getRow() - 1, 0)).toString(), currentSong.getRow() - 1, 0);
                            table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        }
                    } else {
                        String firstSong = (plTable.getValueAt(0,0).toString());
                        if(firstSong == currentSong.getPath()){
                            currentSong.setContents((plTable.getValueAt(plTable.getModel().getRowCount()-1,0).toString()), plTable.getModel().getRowCount()-1, 0);
                            plTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        } else {
                            currentSong.setContents((table.getValueAt(currentSong.getRow()-1, 0)).toString(), currentSong.getRow()-1, 0);
                            plTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        }
                    }
                } catch (BasicPlayerException ex) {
                    System.out.println("BasicPlayer exception");
                    Logger.getLogger(MusicPlayerGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(">|".equals(e.getActionCommand())) {
                try {
                    if(!isPlaylist){
                        String lastSong = (table.getValueAt(table.getModel().getRowCount()-1,0).toString());
                        if(lastSong == currentSong.getPath()){
                            //highlightedSongPath = (table.getValueAt(0,0).toString());
                            currentSong.setContents((table.getValueAt(0,0).toString()), 0, 0);
                            table.setRowSelectionInterval(0, 0);
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        } else {
                            currentSong.setContents((table.getValueAt(currentSong.getRow()+1, 0)).toString(), currentSong.getRow()+1, 0);
                            table.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        }
                    } else {
                        String lastSong = (plTable.getValueAt(plTable.getModel().getRowCount() - 1, 0).toString());
                        if (lastSong == currentSong.getPath()) {
                            currentSong.setContents((plTable.getValueAt(0, 0).toString()), 0, 0);
                            plTable.setRowSelectionInterval(0, 0);
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        } else {
                            currentSong.setContents((plTable.getValueAt(currentSong.getRow() + 1, 0)).toString(), currentSong.getRow() + 1, 0);
                            plTable.setRowSelectionInterval(currentSong.getRow(), currentSong.getRow());
                            player.open(new File(currentSong.getPath()));
                            player.play();
                        }
                    }
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
                            //stopFileCol(table);
                            //hideAllCol(table);

                            //adding song by menu
                            //Enter into playlist
                            if(isPlaylist) {
                                String songPath = file.getAbsolutePath();
                                String index = "0";
                                for (int i = 0; i < table.getRowCount(); i++) {
                                    table.setRowSelectionInterval(i, i);
                                    String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                                    if(songPath.equals(tableValue)){
                                        index = Integer.toString(i);
                                        dragAndDropPlaylist(index);
                                    }

                                }

                            }

                            table.setModel(library.buildSongsTable()); //refresh table
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
                           // stopFileCol(table);
                           // hideAllCol(table);

                            //Enter into playlist
                            if(isPlaylist) {
                                //String songPath = dialog.getSelectedFile().getAbsolutePath();
                                String songPath = tempFile.getAbsolutePath();
                                String index = "0";
                                for (int i = 0; i < table.getRowCount(); i++) {
                                    table.setRowSelectionInterval(i, i);
                                    String tableValue = table.getModel().getValueAt(i,0).toString().trim();
                                    if(songPath.equals(tableValue)){
                                        index = Integer.toString(i);
                                        dragAndDropPlaylist(index);
                                    }

                                }
                                scrollPane.setViewportView(plTable);
                                //stopFileCol(plTable);

                                table.setModel(library.buildSongsTable()); //refresh table
                                //stopFileCol(table);
                                if(origin != null){
                                    origin.table.setModel(library.buildSongsTable());
                                    //stopFileCol(origin.table);
                                    //hideAllCol(origin.table);
                                }


                            }
                        }
                    }
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

    public void dragAndDropPlaylist(String index){
        //String indexStr = library.searchDB(songPath);
        boolean playlistContains = false;
        String currentList = playDB.searchDB(playlistName);
        ArrayList<Integer> songIndexList = new ArrayList<Integer>();
        String temp = currentList.trim();
        if(!temp.equals("empty")) {
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
        //stopFileCol(plTable);

        table.setModel(library.buildSongsTable()); //refresh table
        //stopFileCol(table);
        //hideAllCol(table);
        if(origin != null){
            origin.table.setModel(library.buildSongsTable());
            //stopFileCol(origin.table);
            //hideAllCol(origin.table);
        }

    }

    public void populatePlaylist(String playlistName){
        ArrayList<Integer> songIndexes = new ArrayList<Integer>();
        String indexes = playDB.searchDB(playlistName);
        if(!indexes.trim().equals("empty")) {
            String[] tokens = indexes.trim().split("\\s*,\\s*");
            for (String s : tokens) {
                songIndexes.add(Integer.parseInt(s));
            }
            if (songIndexes.size() > 0) {
                TableModel original = table.getModel();
                DefaultTableModel playlistModel = new DefaultTableModel(songIndexes.size(), original.getColumnCount());
                plTable = new JTable(playlistModel);
                plTable.setDragEnabled(true);

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
        if(isPlaylist){
            main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        else {
            main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        main.setVisible(true);
    }
}
