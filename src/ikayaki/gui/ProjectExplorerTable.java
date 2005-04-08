/*
* ProjectExplorerTable.java
*
* Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
*
* This file is part of Ikayaki.
*
* Ikayaki is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* Ikayaki is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Ikayaki; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/

package ikayaki.gui;

import ikayaki.Ikayaki;
import ikayaki.Project;
import ikayaki.ProjectEvent;
import ikayaki.ProjectListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu
 * (ProjectExplorerPopupMenu).
 *
 * @author Samuli Kaipiainen
 */
public class ProjectExplorerTable extends JTable {

    /**
     * The component (MainViewPanel) whose setProject() method will be called on opening a new project file.
     */
    private final ProjectComponent parent;

    private final ProjectExplorerTableModel explorerTableModel;

    private final Comparator<File> explorerTableComparator = new ProjectExplorerTableComparator();

    /**
     * Currently open directory.
     */
    private File directory;

    /**
     * Project files to in current directory. Set to File[0] so that ProjectExplorerTableModel can be created.
     */
    private File[] files = new File[0];

    /**
     * Selected project file index, or -1 if none selected in current directory.
     */
    private int selectedFile = -1;

    private int explorerTableSortColumn = COLUMN_FILENAME;

    private static final int COLUMN_UNDEFINED = -1;
    private static final int COLUMN_FILENAME = 0;
    private static final int COLUMN_TYPE = 1;
    private static final int COLUMN_LASTMOD = 2;
    private static final String[] column_name = {"filename", "type", "last modified"};

    /**
     * Builds the project type cache for each directory. If the thread is still working when a new request arrives, the
     * old thread should be interrupted.
     */
    private Thread projectTypeCacher = new Thread();

    /**
     * Builds ProjectExplorerTable.
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     */
    public ProjectExplorerTable(ProjectComponent parent) {
        this.parent = parent;

        explorerTableModel = new ProjectExplorerTableModel();
        this.setModel(explorerTableModel);

        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);
        // this.getTableHeader().setResizingAllowed(false);
        // TODO: the grid still shows up when selecting rows. must make a custom cell renderer to change that
        this.setShowGrid(false);

        // TODO: set column sizes somehow automatically, according to table contents?
        this.getColumnModel().getColumn(0).setPreferredWidth(130);
        this.getColumnModel().getColumn(1).setPreferredWidth(50);
        this.getColumnModel().getColumn(2).setPreferredWidth(80);
        this.setPreferredScrollableViewportSize(new Dimension(280, 400));

        // ProjectExplorerTable events

        /**
         * Event A: On table click - call Project.loadProject(File) with clicked project file, call
         * (MainViewPanel) parent.setProject(Project) with returned Project unless null, on which case
         * show error message and revert explorerTable selection to old project, if any.
         */
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // we only want the actually selected row, and don't want to react to an already selected line
                // (which could also mean that we had a load error, and that selection was reverted)
                if (e.getValueIsAdjusting() || getSelectedRow() == selectedFile) return;
                if (getSelectedRow() == -1) return; // otherwise will crash the program upon loading a file

                Project project = Project.loadProject(files[getSelectedRow()]);

                // if load error, revert back to old selection
                if (project == null) {
                    // TODO: flash selected row red for 100 ms, perhaps? - might require a custom cell renderer
                    if (selectedFile == -1) {
                        clearSelection();
                    } else {
                        setRowSelectionInterval(selectedFile, selectedFile);
                    }
                } else {
                    ProjectExplorerTable.this.parent.setProject(project);
                }
            }
        });

        /**
         * Event B: On table mouse right-click - create a ProjectExplorerPopupMenu for rightclicked
         * project file.
         */
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // only right-click brings popup menu
                if (e.getButton() != MouseEvent.BUTTON3) return;

                int row = rowAtPoint(e.getPoint());

                // construct a new popup menu for every click
                ProjectExplorerPopupMenu explorerTablePopup = new ProjectExplorerPopupMenu(files[row]);
                explorerTablePopup.show(ProjectExplorerTable.this, e.getX(), e.getY());
            }
        });

        /**
         * ExplorerTable sorting.
         */
        this.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // only left-click changes sorting
                if (e.getButton() != MouseEvent.BUTTON1) return;

                JTableHeader th = (JTableHeader) e.getSource();
                TableColumnModel cm = th.getColumnModel();
                int viewColumn = cm.getColumnIndexAtX(e.getX());

                // TODO: what the sick hell of chainsaw internals do I have to touch just to update table headers?!?
                // ... Hope it's at least correct, not that I care anymore

                // reset all column header names
                for (int col = 0; col < cm.getColumnCount(); col++) {
                    cm.getColumn(col).setHeaderValue(column_name[cm.getColumn(col).getModelIndex()]);
                }

                // set sort column header name
                explorerTableSortColumn = cm.getColumn(viewColumn).getModelIndex();
                cm.getColumn(viewColumn).setHeaderValue(column_name[explorerTableSortColumn] + " *");
                th.repaint();

                // update table with sorted data, update selected file and table selection
                setDirectory(null);
            }
        });
    }

    /**
     * Updates table contents, sets selectedFile index and table selection to selected project file, or -1.
     *
     * @param directory directory whose project files to display, or null to just update the table.
     */
    public void setDirectory(File directory) {
        if (directory != null) this.directory = directory;
        this.files = getProjectFiles(this.directory);

        // sort files if needed before updating table
        if (explorerTableSortColumn != COLUMN_UNDEFINED) {
            Arrays.sort(files, explorerTableComparator);
        }

        // search if any file in current directory matches currently open project file
        selectedFile = -1;
        if (parent.getProject() != null) {
            for (int n = 0; n < files.length; n++) {
                if (parent.getProject().getFile().equals(files[n])) selectedFile = n;
            }
        }

        // update table data and selected project file
        explorerTableModel.fireTableDataChanged();
        if (selectedFile != -1) setRowSelectionInterval(selectedFile, selectedFile);
    }

    /**
     * Reads project file listing from given directory.
     *
     * @param directory directory whose project file listing to read.
     * @return project files in that directory.
     */
    private File[] getProjectFiles(File directory) {
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(Ikayaki.FILE_TYPE));
            }
        });

        // build project type cache
        final File[] cacheFiles = new File[files.length];   // need a copy of the array to work on
        for (int i = 0; i < files.length; i++) {
            cacheFiles[i] = files[i];
        }
        projectTypeCacher.interrupt();      // stop the old thread before starting a new one
        projectTypeCacher = new Thread() {
            @Override public void run() {
                int i = 0;
                for (File file : cacheFiles) {
                    if (interrupted()) {
                        return;
                    }
                    if (file.canRead()) {
                        Project.getType(file);
                        if (++i % 10 == 0) {
                            Thread.yield();
                        }
                    }
                }
            }
        };
        projectTypeCacher.start();

        return files;
    }

    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerTable).
     */
    private class ProjectExplorerTableModel extends AbstractTableModel implements ProjectListener {

        public String getColumnName(int column) {
            return column_name[column] + (column == explorerTableSortColumn ? " *" : "");
        }

        public int getRowCount() {
            return files.length;
        }

        public int getColumnCount() {
            return column_name.length;
        }

        public Object getValueAt(int row, int column) {
            switch (column) {
            case COLUMN_FILENAME:
                return files[row].getName();
            case COLUMN_TYPE:
                return Project.getType(files[row]);
            case COLUMN_LASTMOD:
                return DateFormat.getInstance().format(files[row].lastModified());
            default:
                assert false;
                return null;
            }
        }

        /**
         * Updates the file list when a project file has been saved.
         *
         * @param event ProjectEvent received.
         */
        public void projectUpdated(ProjectEvent event) {
            if (event.getType() == ProjectEvent.Type.FILE_SAVED) {
                File saved = event.getProject().getFile();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equals(saved)) {
                        explorerTableModel.fireTableRowsUpdated(i, i);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Comparator used for ProjectExplorerTable sorting.
     */
    private class ProjectExplorerTableComparator implements Comparator<File> {
        public int compare(File a, File b) {
            switch (explorerTableSortColumn) {
            case COLUMN_FILENAME:
                return a.compareTo(b);
                // WARNING: might chocke Project.getType(File)
            case COLUMN_TYPE:
                return Project.getType(a).compareTo(Project.getType(b));
                // TODO: int-cast changes sign if difference larger than maxint
            case COLUMN_LASTMOD:
                return (int) (a.lastModified() - b.lastModified());
            default:
                return 0;
            }
        }
    }

    /**
     * Shows popup menu with export choices: AF (.dat), Thellier (.tdt) and Thermal (.tdt), and for each, "to current
     * directory", "to disk drive A:" and "...", which opens a standard file chooser for selecting dir and file to
     * export to. Executes selected export command.
     */
    private class ProjectExplorerPopupMenu extends JPopupMenu {

        /**
         * Builds the popup menu, but doesn't show it; use show(...) to do that.
         *
         * @param file file to show export menu for.
         */
        public ProjectExplorerPopupMenu(File file) {
            final File directory = file.getParentFile();
            String filename = file.getName();
            String basename = filename;
            if (basename.toLowerCase().endsWith(Ikayaki.FILE_TYPE)) {
                basename = basename.substring(0, basename.length() - Ikayaki.FILE_TYPE.length());
            }

            JMenuItem export = new JMenuItem("Export '" + filename + "' to");
            export.setFont(export.getFont().deriveFont(Font.BOLD));
            export.setEnabled(false);
            this.add(export);

            // TODO: some portable way to get a File for disk drive? Or maybe a Setting for export-dirs?
            for (File dir : new File[]{null, directory, new File("A:/")}) {
                for (String type : new String[]{"dat", "tdt", "srm"}) {
                    JMenuItem exportitem;
                    if (dir == null) {
                        exportitem = new JMenuItem(type.toUpperCase() + " file...");
                    } else {
                        exportitem = new JMenuItem(new File(dir, basename + "." + type).toString());
                    }

                    this.add(exportitem);

                    /**
                     * Event A: On menu click - call project.exportToXXX(File) according to selected menu
                     * item; if false is returned, show error message.
                     */
                    exportitem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String filename = e.getActionCommand();
                            String filetype = filename.substring(filename.length() - 3);
                            File exportfile;

                            if (filetype.equals("...")) {
                                filetype = filename.substring(0, 3).toLowerCase();
                                JFileChooser chooser = new JFileChooser(directory);
                                chooser.setFileFilter(
                                        new GenericFileFilter(filetype.toUpperCase() + " File", filetype));

                                if (chooser.showSaveDialog(ProjectExplorerTable.this) == JFileChooser.APPROVE_OPTION) {
                                    exportfile = chooser.getSelectedFile();
                                }

                            } else {
                                exportfile = new File(filename);
                            }

                            // TODO: which one of these two?
                            //Project.export(exportfile, filetype);
                            //Project.loadProject(exportfile).export(filetype);     // TODO <-- this one. There are no static export methods in Project class. Take a look at exportProject method in MainViewPanel. 

                            // TODO: tell somehow if export was successful; statusbar perhaps?
                        }
                    });
                }
            }
        }
    }
} // NOTE: what a nice end-brace-fallout :)
