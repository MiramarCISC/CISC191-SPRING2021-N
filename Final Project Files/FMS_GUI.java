package edu.sdccd.cisc191.n;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

public class FMS_GUI extends JFrame{
    private final FileSystemView fileSystemView;
    private final JTree tree;
    private final ListSelectionListener listSelectionListener;
    private final JTable table;
    private FileTable fileTableModel;
    private boolean cellSizesSet = false;
    private final JLabel fileName;
    private final JTextField path;
    private final JLabel date;
    private final JLabel size;
    private File currentFile;

    public FMS_GUI(){

        setTitle("File Manager System");
        setSize(600, 400);
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel dirPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        dirPanel.setPreferredSize(new Dimension(200,250));
        bottomPanel.setPreferredSize(new Dimension(600, 150));

        add(dirPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        pack();
        setLocationByPlatform(true);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(root);

        TreeSelectionListener treeSelectionListener = tse -> {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode)tse.getPath().getLastPathComponent();
            showChildren(node);
            setFileDetails((File)node.getUserObject());
        };

        fileSystemView = FileSystemView.getFileSystemView();
        File[] roots = fileSystemView.getRoots();
        for (File fileSystemRoot : roots) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
            root.add( node );
            File[] files = fileSystemView.getFiles(fileSystemRoot, true);
            for (File file : files) {
                if (file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(file));
                }
            }
        }

        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(treeSelectionListener);
        tree.setCellRenderer(new FileTreeDirectory());
        tree.expandRow(0);
        tree.setVisibleRowCount(15);

        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setPreferredSize(new Dimension(200,400));

        dirPanel.setLayout(new BorderLayout(5, 5));
        dirPanel.add(treeScroll, BorderLayout.CENTER);

        listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                int row = table.getSelectionModel().getLeadSelectionIndex();
                setFileDetails( ((FileTable)table.getModel()).getFile(row) );
            }
        };

        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(true);
        table.getSelectionModel().addListSelectionListener(listSelectionListener);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(400, 400));

        rightPanel.setLayout(new BorderLayout(5,5));
        rightPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel fileMainDetails = new JPanel(new BorderLayout(5,5));

        JPanel fileDetailsLabels = new JPanel(new GridLayout(0,1,2,2));
        fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

        JPanel fileDetailsValues = new JPanel(new GridLayout(0,1,2,2));
        fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

        fileDetailsLabels.add(new JLabel("File", JLabel.TRAILING));
        fileName = new JLabel();
        fileDetailsValues.add(fileName);
        fileDetailsLabels.add(new JLabel("Path/name", JLabel.TRAILING));
        path = new JTextField(5);
        path.setEditable(false);
        fileDetailsValues.add(path);
        fileDetailsLabels.add(new JLabel("Last Modified", JLabel.TRAILING));
        date = new JLabel();
        fileDetailsValues.add(date);
        fileDetailsLabels.add(new JLabel("File size", JLabel.TRAILING));
        size = new JLabel();
        fileDetailsValues.add(size);

        //holds management buttons
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton locateFile = new JButton("Locate");
        Desktop desktop = Desktop.getDesktop();
        locateFile.addActionListener(ae -> {
            try {
                System.out.println("Locate: " + currentFile.getParentFile());
                desktop.open(currentFile.getParentFile());
            } catch(Throwable t) {
                t.printStackTrace();
            }
        });
        toolBar.add(locateFile);

        JButton openFile = new JButton("Open");
        openFile.addActionListener(ae -> {
            try {
                System.out.println("Open: " + currentFile);
                desktop.open(currentFile);
            } catch(Throwable t) {
                t.printStackTrace();
            }
        });
        toolBar.add(openFile);

        JButton editFile = new JButton("Edit");
        editFile.addActionListener(ae -> {
            try {
                desktop.edit(currentFile);
            } catch(Throwable t) {
                t.printStackTrace();
            }
        });
        toolBar.add(editFile);

        //prints file if is a text file
        JButton printFile = new JButton("Print");
        printFile.addActionListener(ae -> {
            try {
                desktop.print(currentFile);
            } catch(Throwable t) {
                t.printStackTrace();
            }
        });
        toolBar.add(printFile);

        JButton deleteFile = new JButton("Delete");
        deleteFile.addActionListener(ae -> {
            try{
                if (currentFile != null) {
                    Files.deleteIfExists(Paths.get(currentFile.getAbsolutePath()));
                    System.out.println("File deleted!");
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

        });
        toolBar.add(deleteFile);

        JButton renameFile = new JButton("Rename");
        /*renameFile.addActionListener(ae -> {
            try{

            }
        });*/
        toolBar.add(renameFile);

        openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
        editFile.setEnabled(desktop.isSupported(Desktop.Action.EDIT));
        printFile.setEnabled(desktop.isSupported(Desktop.Action.PRINT));

        int count = fileDetailsLabels.getComponentCount();
        for (int i=0; i<count; i++) {
            fileDetailsLabels.getComponent(i).setEnabled(false);
        }

        bottomPanel.setLayout(new BorderLayout(5,5));
        bottomPanel.add(toolBar, BorderLayout.NORTH);
        bottomPanel.add(fileMainDetails, BorderLayout.CENTER);

    }

    private void showChildren(final DefaultMutableTreeNode node) {
        JProgressBar progressBar = new JProgressBar();

        tree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        SwingWorker<Void, File> worker = new SwingWorker<>() {
            @Override
            public Void doInBackground() {
                File file = (File) node.getUserObject();
                if (file.isDirectory()) {
                    File[] files = fileSystemView.getFiles(file, true); //!!
                    if (node.isLeaf()) {
                        for (File child : files) {
                            if (child.isDirectory()) {
                                publish(child);
                            }
                        }
                    }
                    setTableData(files);
                }
                return null;
            }

            @Override
            protected void process(List<File> chunks) {
                for (File child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                tree.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width<0) {
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            width = (int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    private void setTableData(final File[] files) {
        int rowIconPadding = 6;
        SwingUtilities.invokeLater(() -> {
            if (fileTableModel==null) {
                fileTableModel = new FileTable();
                table.setModel(fileTableModel);
            }
            table.getSelectionModel().removeListSelectionListener(listSelectionListener);
            fileTableModel.setFiles(files);
            table.getSelectionModel().addListSelectionListener(listSelectionListener);
            if (!cellSizesSet) {
                Icon icon = fileSystemView.getSystemIcon(files[0]);

                table.setRowHeight( icon.getIconHeight()+rowIconPadding );

                setColumnWidth(0,-1);
                setColumnWidth(3,60);
                table.getColumnModel().getColumn(3).setMaxWidth(120);
                setColumnWidth(4,-1);
                setColumnWidth(5,-1);
                setColumnWidth(6,-1);
                setColumnWidth(7,-1);
                setColumnWidth(8,-1);
                setColumnWidth(9,-1);

                cellSizesSet = true;
            }
        });
    }

    private void setFileDetails(File file) {
        currentFile = file;
        Icon icon = fileSystemView.getSystemIcon(file);
        fileName.setIcon(icon);
        fileName.setText(fileSystemView.getSystemDisplayName(file));
        path.setText(file.getPath());
        date.setText(new Date(file.lastModified()).toString());
        size.setText(file.length() + " bytes");
    }
}
