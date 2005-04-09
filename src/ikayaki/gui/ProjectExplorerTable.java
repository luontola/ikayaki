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

import ikayaki.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu
 * (ProjectExplorerPopupMenu).
 *
 * @author Samuli Kaipiainen
 */
public class ProjectExplorerTable extends JTable implements ProjectListener {

    /**
     * The component (MainViewPanel) whose setProject() method will be called on opening a new project file.
     */
    private final ProjectComponent parent;

    /**
     * Tells whether this table is calibration project table or all-project table.
     */
    private boolean isCalibration;

    private final ProjectExplorerTableModel explorerTableModel;

    private final Comparator<File> explorerTableComparator = new ProjectExplorerTableComparator();

    /**
     * Builds the project type cache for each directory. If the thread is still working when a new request arrives, the
     * old thread should be interrupted.
     */
    private Thread projectTypeCacher = new Thread();

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
    private static final int COLUMN_LASTMEASURE = 3;
    private static final int COLUMN_UNMEASURED = 4;
    private static final String[] column_name = {"filename", "type", "last modified", "last measure", "time"};

    /**
     * Builds ProjectExplorerTable for displaying all project files.
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     */
    public ProjectExplorerTable(ProjectComponent parent) {
        this(parent, false);
    }

    /**
     * Builds ProjectExplorerTable.
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     * @param isCalibration if true, this table will display only calibration projects and related columns.
     */
    public ProjectExplorerTable(ProjectComponent parent, boolean isCalibration) {
        this.parent = parent;
        this.isCalibration = isCalibration;

        explorerTableModel = new ProjectExplorerTableModel();
        this.setModel(explorerTableModel);

        // TODO: should be able to select and export multiple files at a time
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);
        // this.getTableHeader().setResizingAllowed(false);
        // TODO: the grid still shows up when selecting rows. must make a custom cell renderer to change that
        this.setShowGrid(false);
        this.setDefaultRenderer(StyledWrapper.class, new StyledTableCellRenderer());

        // get each column for easier access...
        TableColumn[] column = new TableColumn[this.getColumnModel().getColumnCount()];
        for (int n = 0; n < this.getColumnModel().getColumnCount(); n++)
            column[n] = this.getColumnModel().getColumn(n);

        // TODO: set column sizes somehow automatically, according to table contents?
        column[COLUMN_FILENAME].setPreferredWidth(130);
        column[COLUMN_TYPE].setPreferredWidth(50);
        column[COLUMN_LASTMOD].setPreferredWidth(80);
        column[COLUMN_LASTMEASURE].setPreferredWidth(80);
        column[COLUMN_UNMEASURED].setPreferredWidth(20);
        this.setPreferredScrollableViewportSize(new Dimension(280, 400));

        // remove "type" and "last modified" columns for calibration project table,
        // "last measure" and "unmeasured" for all projects table
        if (this.isCalibration) {
            this.getColumnModel().removeColumn(column[COLUMN_TYPE]);
            this.getColumnModel().removeColumn(column[COLUMN_LASTMOD]);
        } else {
            this.getColumnModel().removeColumn(column[COLUMN_LASTMEASURE]);
            this.getColumnModel().removeColumn(column[COLUMN_UNMEASURED]);
        }

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
                    if (selectedFile == -1) clearSelection();
                    else setRowSelectionInterval(selectedFile, selectedFile);
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
                cm.getColumn(viewColumn).setHeaderValue(explorerTableModel.getColumnName(explorerTableSortColumn));
                th.repaint();

                // update table with sorted data, update selected file and table selection
                setDirectory(directory);
            }
        });
    }

    /**
     * Updates table contents, sets selectedFile index and table selection to selected project file, or -1.
     *
     * @param directory directory whose project files to display, or null to just update the table.
     */
    public void setDirectory(File directory) {
        this.directory = directory;
        this.files = getProjectFiles(this.directory);

        // sort files if needed before updating table
        if (explorerTableSortColumn != COLUMN_UNDEFINED) Arrays.sort(files, explorerTableComparator);

        // search if any file in current directory matches currently open project file
        selectedFile = -1;
        if (parent.getProject() != null) for (int n = 0; n < files.length; n++)
            if (parent.getProject().getFile().equals(files[n])) selectedFile = n;

        // update table data and selected project file
        explorerTableModel.fireTableDataChanged();
        if (selectedFile != -1) setRowSelectionInterval(selectedFile, selectedFile);
    }

    /**
     * Reads project file listing from given directory.
     *
     * @param directory directory whose project file listing to read.
     * @return project files in that directory; new File[0] if directory is null or invalid.
     */
    private File[] getProjectFiles(File directory) {
        if (directory == null) return new File[0];

        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(Ikayaki.FILE_TYPE)
                    && (!isCalibration || Project.getType(file) == Project.Type.CALIBRATION);
            }
        });

        if (files == null) return new File[0];

        // build project type cache (Project caches those; we only need to call Project.getType(File) for each file)
        // by doing this, scrolling in a big directory for the first time becomes smoother

        // stop the old thread before messing with cacheFiles and starting a new thread
        try {
            projectTypeCacher.interrupt();
            projectTypeCacher.join(); // wait for old thread to die
        } catch (InterruptedException e) { }

        // need a copy of files to work on, as they might get sorted any time in setDirectory(File)
        final File[] cacheFiles = files.clone();

        projectTypeCacher = new Thread(new Runnable() {
            public void run() {
                for (File file : cacheFiles) {
                    if (Thread.interrupted()) return;
                    if (file.canRead()) Project.getType(file);
                }
            }
        });

        // start the worker thread
        projectTypeCacher.setPriority(Thread.MIN_PRIORITY);
        projectTypeCacher.start();

        return files;
    }

    /**
     * Forwards ProjectEvents to the table model.
     */
    public void projectUpdated(ProjectEvent event) {
        explorerTableModel.projectUpdated(event);
    }

    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerTable).
     */
    private class ProjectExplorerTableModel extends AbstractTableModel implements ProjectListener {

        private final StyledWrapper defaultWrapper = new StyledWrapper();               // defaults
        private final StyledWrapper calibrationNoticeWrapper = new StyledWrapper();     // bold text

        public ProjectExplorerTableModel() {
            calibrationNoticeWrapper.font = ProjectExplorerTable.this.getFont().deriveFont(Font.BOLD);

            /*
             * Refresh the data at regular intervals (5 min) even if no other events would refresh it.
             * This is especially to update the time elapsed value of a calibration panel.
             */
            new Timer(5*60*1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireTableDataChanged();
                }
            }).start();
        }

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
            File file = files[row];
            Object value;
            switch (column) {
                case COLUMN_FILENAME:
                    String filename = file.getName();
                    value = filename.substring(0, filename.length() - Ikayaki.FILE_TYPE.length());
                    break;
                case COLUMN_TYPE:
                    value = Project.getType(file);
                    break;
                case COLUMN_LASTMOD:
                    value = DateFormat.getInstance().format(file.lastModified());
                    break;
                case COLUMN_LASTMEASURE:
                    Date date = Project.loadProject(file).getTimestamp();
                    if (date == null) value = null;
                    else value = DateFormat.getInstance().format(date);
                    break;
                case COLUMN_UNMEASURED:
                    date = Project.loadProject(file).getTimestamp();
                    if (date == null) value = null;
                    else value = (new Date().getTime() - date.getTime()) / 3600000 + " h";
                    break;
                default:
                    assert false;
                    value = null;
                    break;
            }

            // wrap to the style
            StyledWrapper wrapper;
            if (isCalibration) {
                Date date = Project.loadProject(file).getTimestamp();
                if (date == null) {
                    wrapper = calibrationNoticeWrapper;
                } else {
                    // alert the user if the calibration has not been done today
                    int hoursElapsed = (int)(new Date().getTime() - date.getTime()) / 3600000;
                    if (hoursElapsed >= 18) {
                        wrapper = calibrationNoticeWrapper;
                    } else {
                        wrapper = defaultWrapper;
                    }
                }
            } else {
                wrapper = defaultWrapper;
            }
            wrapper.value = value;
            return wrapper;
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return StyledWrapper.class;
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
                case COLUMN_TYPE:
                    // WARNING: might choke Project.getType(File) with O(n log n) requests
                    Project.Type atype = Project.getType(a), btype = Project.getType(b);
                    if (atype == null && btype == null)return 0;
                    if (atype == null)return 1;
                    if (btype == null)return -1;
                    // NOTE: calibration-projects appear first because of enum-compareTo, but that's just fine, right?
                    return atype.compareTo(btype);
                case COLUMN_LASTMOD:
                    long diff = a.lastModified() - b.lastModified();
                    return diff == 0 ? 0 : (diff < 0 ? -1 : 1);
                case COLUMN_LASTMEASURE:
                    return compareTimestamps(a, b);
                case COLUMN_UNMEASURED:
                    return -compareTimestamps(a, b);
                default:
                    return 0;
            }
        }

        /**
         * Helper method for comparing project timestamps.
         *
         * @param a project file a
         * @param b project file b
         * @return <0 if a's timestamp < b's timestamp, 0 if the same, >0 if a's timestamp > b's timestamp
         */
        private int compareTimestamps(File a, File b) {
            Project aproject = Project.loadProject(a);
            Project bproject = Project.loadProject(b);
            Date adate = aproject == null ? null : aproject.getTimestamp();
            Date bdate = bproject == null ? null : bproject.getTimestamp();
            if (adate == null && bdate == null) return 0;
            if (adate == null) return -1;
            if (bdate == null) return 1;
            return adate.compareTo(bdate);
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
            if (filename.toLowerCase().endsWith(Ikayaki.FILE_TYPE)) {
                basename = filename.substring(0, filename.length() - Ikayaki.FILE_TYPE.length());
            }

            JMenuItem export = new JMenuItem("Export '" + filename + "' to");
            export.setFont(export.getFont().deriveFont(Font.BOLD));
            export.setEnabled(false);
            this.add(export);

            // TODO: some portable way to get a File for disk drive? Or maybe a Setting for export-dirs?
            for (File dir : new File[]{null, directory, new File("A:/")}) {
                for (String type : new String[]{"dat", "tdt", "srm"}) {
                    JMenuItem exportitem;
                    if (dir == null) exportitem = new JMenuItem(type.toUpperCase() + " file...");
                    else exportitem = new JMenuItem(new File(dir, basename + "." + type).toString());

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
                                chooser.setFileFilter(new GenericFileFilter(filetype.toUpperCase() + " File", filetype));

                                if (chooser.showSaveDialog(ProjectExplorerTable.this) == JFileChooser.APPROVE_OPTION) {
                                    exportfile = chooser.getSelectedFile();
                                }

                            } else exportfile = new File(filename);

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
