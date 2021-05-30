//class fills JTable with file information and loads images 
package edu.sdccd.cisc191.n;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.Date;

class FileTable extends AbstractTableModel {

    private File[] files;
    private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    //title for columns (R = Readable, W = Writable, E = Executable, D = Directory/Folder, F = File)
    private final String[] columns = {"Icon", "File", "Path/name", "Size", "Last Modified", "R", "W", "E", "D", "F"};

    FileTable() {
        this(new File[0]);
    }

    FileTable(File[] files) {
        this.files = files;
    }

    public Object getValueAt(int row, int column) {
        File file = files[row];
        switch (column) {
            case 0:
                return fileSystemView.getSystemIcon(file);
            case 1:
                return fileSystemView.getSystemDisplayName(file);
            case 2:
                return file.getPath();
            case 3:
                return file.length();
            case 4:
                return file.lastModified();
            case 5:
                return file.canRead();
            case 6:
                return file.canWrite();
            case 7:
                return file.canExecute();
            case 8:
                return file.isDirectory();
            case 9:
                return file.isFile();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 0 -> ImageIcon.class;
            case 3 -> Long.class;
            case 4 -> Date.class;
            case 5, 6, 7, 8, 9 -> Boolean.class;
            default -> String.class;
        };
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.length;
    }

    public File getFile(int row) {
        return files[row];
    }

    public void setFiles(File[] files) {
        this.files = files;
        fireTableDataChanged();
    }
}
