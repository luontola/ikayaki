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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
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
import java.util.Date;

/**
 * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu.
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
     * Project files to in current directory. Set to new File[0] so that ProjectExplorerTableModel can be created.
     */
    private File[] files = new File[0];

    /**
     * Selected project file index, or -1 if none selected in current directory.
     */
    private int selectedFile = -1;

    /**
     * Current sort column; must be set to an untranslated column index.
     */
    private int explorerTableSortColumn = 0;

    // possible columns and their names
    private static final int COLUMN_UNDEFINED = -1;
    public static final int COLUMN_FILENAME = 0;
    public static final int COLUMN_TYPE = 1;
    public static final int COLUMN_LASTMOD = 2;
    public static final int COLUMN_LASTMEASURE = 3;
    public static final int COLUMN_UNMEASURED = 4;
    public static final String[] column_name = {"Name", "Type", "Modified", "Measured", "Elapsed"};

    // default column configurations for different table types
    public static final int[] default_columns = {COLUMN_FILENAME, COLUMN_TYPE, COLUMN_LASTMOD};
    public static final int[] calibration_columns = {COLUMN_FILENAME, COLUMN_LASTMEASURE, COLUMN_UNMEASURED};

    /**
     * Visible columns in this table (as in column translation table); can be set with setColumns(int[]). Initialized to
     * new int[0] so that ProjectExplorerTableModel can be created.
     */
    private int[] columns = new int[0];

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
     * @param parent        the component whose setProject() method will be called on opening a new project file.
     * @param isCalibration if true, this table will display only calibration projects and related columns.
     */
    public ProjectExplorerTable(ProjectComponent parent, boolean isCalibration) {
        this.parent = parent;
        this.isCalibration = isCalibration;

        // create TableModel only after columns are set
        explorerTableModel = new ProjectExplorerTableModel();
        this.setModel(explorerTableModel);

        // allow multiple line selection for multi-file-export
        // this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setResizingAllowed(false);
        this.setShowGrid(false);
        this.setIntercellSpacing(new Dimension(0, 0));
        this.setDefaultRenderer(StyledWrapper.class, new StyledTableCellRenderer());

        // TODO: what should be here anyway?
        // this.setPreferredScrollableViewportSize(new Dimension(280, 400));

        // set the right visible columns for table type
        if (this.isCalibration) {
            setColumns(calibration_columns);
        } else {
            setColumns(default_columns);
        }

        // ProjectExplorerTable events

        /**
         * Event A: On table click - call Project.loadProject(File) with clicked project file, call
         * (MainViewPanel) parent.setProject(Project) with returned Project unless null, on which case
         * show error message and revert explorerTable selection to old project, if any.
         */
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {

                /* TODO:
                 * It is possible by CTRL-clicking to deselect the selected row. This can cause problems
                 * because after that the user will not anymore see which of the files is open. Change it
                 * so that if for some reason no row is selected and the currently open project is in this
                 * directory, select that project's file.
                 */
                // - done, see below

                // we only want the actually selected row, and don't want to react to an already selected line
                // (which could also mean that we had a load error, and that selection was reverted)
                if (e.getValueIsAdjusting() || getSelectedRow() == selectedFile) return;
                if (getSelectedRowCount() > 1) return; // do nothing if multiple files selected

                Project project = getSelectedRow() == -1 ? null : Project.loadProject(files[getSelectedRow()]);

                // if load error, or nothing selected, revert back to old selection
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

                int[] row;
                if (getSelectedRowCount() > 1) {
                    row = getSelectedRows();
                } else {
                    row = new int[]{rowAtPoint(e.getPoint())};
                }

                // copy files matching selected (or clicked) rows
                File[] file = new File[row.length];
                for (int n = 0; n < row.length; n++) file[n] = files[row[n]];

                // construct a new popup menu for every click
                ProjectExplorerPopupMenu explorerTablePopup = new ProjectExplorerPopupMenu(file);
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

                TableColumnModel cm = getColumnModel();
                int viewColumn = cm.getColumnIndexAtX(e.getX());
                explorerTableSortColumn = cm.getColumn(viewColumn).getModelIndex();

                // update all column headers and repaint header
                for (int col = 0; col < getColumnCount(); col++) {
                    cm.getColumn(col).setHeaderValue(getColumnName(col));
                }
                getTableHeader().repaint();

                // update table with sorted data, update selected file and table selection
                setDirectory(directory);
            }
        });
    }

    /**
     * Sets the columns displayed in this table.
     *
     * @param columns int-table with COLUMN_xxx values, or null to just update table.
     */
    public void setColumns(int[] columns) {
        if (columns != null) this.columns = columns;

        // StructureChanged resets columns' PreferredWidths, so they must be set again...
        explorerTableModel.fireTableStructureChanged();

        // set column default widths. if these are not wide enough, fitColumnWidths() will make them wider.
        for (int col = 0; col < this.columns.length; col++) {
            TableColumn column = this.getColumnModel().getColumn(col);
            switch (this.columns[col]) {
            case COLUMN_FILENAME:
                column.setPreferredWidth(130);
                break;
            case COLUMN_TYPE:
                column.setMinWidth(55);
                column.setMaxWidth(55);
                break;
            case COLUMN_LASTMOD:
                column.setMinWidth(95);
                column.setMaxWidth(95);
                break;
            case COLUMN_LASTMEASURE:
                column.setMinWidth(100);
                column.setMaxWidth(100);
                break;   // should be wide enough for dates like "22.12.2005 22:22" with bold font
            case COLUMN_UNMEASURED:
                column.setMinWidth(45);
                column.setMaxWidth(45);
                break;
            }
        }
    }

    /**
     * Makes sure that all data fits in their columns (excluding the COLUMN_FILENAME column). Renders every cell of the
     * table to find out their preferred width, and makes the column wider if the contents does not fit the column.
     * <p/>
     * If rendering all cells in the table (such as the project type) will take a long time, it might be good to run
     * this in a separate thread.
     */
    public void fitColumnWidths() {
        for (int col = 0; col < columns.length; col++) {
            if (columns[col] != COLUMN_FILENAME) {

                // find out the column's preferred width using the actual cell contents
                int width = 0;
                Component comp;
                for (int row = 0; row < getRowCount(); row++) {
                    comp = getCellRenderer(row, col).getTableCellRendererComponent(this,
                            getValueAt(row, col), false, false, row, col);
                    width = Math.max(width, comp.getPreferredSize().width);
                }
                width += 5;
                if (columnModel.getColumn(col).getMaxWidth() < width) {
                    // setting min and max width must be done in the event thread
                    final TableColumn c = columnModel.getColumn(col);
                    final int w = width;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            c.setMaxWidth(w); // must set max first to avoid "min > max"
                            c.setMinWidth(w);
                        }
                    });
                }
            }
        }
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
        if (parent.getProject() != null) {
            for (int n = 0; n < files.length; n++) {
                if (parent.getProject().getFile().equals(files[n])) selectedFile = n;
            }
        }

        // update table data and selected project file
        explorerTableModel.fireTableDataChanged();
        if (selectedFile != -1) {
            setRowSelectionInterval(selectedFile, selectedFile);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    scrollToRow(selectedFile);
                }
            });
        }
    }

    /**
     * Scrolls the table to show the specified row.
     *
     * @param rowIndex row to scroll to.
     */
    private void scrollToRow(int rowIndex) {
        scrollRectToVisible(getCellRect(rowIndex, rowIndex, true));
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
        } catch (InterruptedException e) {
        }

        // need a copy of files to work on, as they might get sorted any time in setDirectory(File)
        final File[] cacheFiles = files.clone();

        projectTypeCacher = new Thread(new Runnable() {
            public void run() {
                for (File file : cacheFiles) {
                    if (Thread.interrupted()) return;
                    if (file.canRead()) Project.getType(file);

                    // when everything is cached, resize the columns if all data does not fit
                    fitColumnWidths();
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
     *
     * @param event ProjectEvent received.
     */
    public void projectUpdated(ProjectEvent event) {
        explorerTableModel.projectUpdated(event);
    }

    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerTable).
     */
    private class ProjectExplorerTableModel extends AbstractTableModel implements ProjectListener {

        private final StyledWrapper defaultWrapper = Settings.getDefaultWrapperInstance();
        private final StyledWrapper measuringWrapper = Settings.getMeasuringWrapperInstance();
        private final StyledWrapper doneRecentlyWrapper = Settings.getDoneRecentlyWrapperInstance();
        private final Font calibrationNoticeFont = ProjectExplorerTable.this.getFont().deriveFont(Font.BOLD);

        /**
         * The project's file who currently has a measurement running, or null if no measurements are active
         */
        private File measuringProjectFile;

        /**
         * The project's file who last completed a measurement, or null if no recent measurements exists.
         */
        private File doneRecentlyProjectFile;

        public ProjectExplorerTableModel() {

            // hide the ugly table borders
            Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
            defaultWrapper.border = emptyBorder;
            defaultWrapper.selectedBorder = emptyBorder;
            defaultWrapper.focusBorder = emptyBorder;
            defaultWrapper.selectedFocusBorder = emptyBorder;
            measuringWrapper.border = emptyBorder;
            measuringWrapper.selectedBorder = emptyBorder;
            measuringWrapper.focusBorder = emptyBorder;
            measuringWrapper.selectedFocusBorder = emptyBorder;
            doneRecentlyWrapper.border = emptyBorder;
            doneRecentlyWrapper.selectedBorder = emptyBorder;
            doneRecentlyWrapper.focusBorder = emptyBorder;
            doneRecentlyWrapper.selectedFocusBorder = emptyBorder;

            /*
             * Refresh the data at regular intervals (5 min) even if no other events would refresh it.
             * This is especially to update the time elapsed value of a calibration panel.
             */
            new Timer(5 * 60 * 1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // don't want to mess with table selection, so just update all lines, as this
                    // doesn't mess with table selection (unlike fireTableDataChanged), which is nice :)
                    explorerTableModel.fireTableRowsUpdated(0, getRowCount() - 1);
                    // ProjectExplorerTable.this.setDirectory(directory);
                }
            }).start();
        }

        public String getColumnName(int column) {
            // translate visible column -> all columns for column_name, _not_ for sort column
            // TODO: does this look better without the "*"?
            // - not to me, but don't really care anymore :)
            return column_name[columns[column]] /* + (column == explorerTableSortColumn ? " *" : "") */;
        }

        public int getRowCount() {
            return files.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int column) {
            File file = files[row];
            Object value;
            switch (columns[column]) { // translate visible column -> all columns
            case COLUMN_FILENAME:
                String filename = file.getName();
                value = filename.substring(0, filename.length() - Ikayaki.FILE_TYPE.length());
                break;
            case COLUMN_TYPE:
                value = Project.getType(file);
                break;
            case COLUMN_LASTMOD:
                value = DateFormat.getInstance().format(file.lastModified());
//                    value = "22.12.2005 22:22"; // testing if this fits to the table
                break;
            case COLUMN_LASTMEASURE:
                Project p = Project.loadProject(file);
                if (p == null) {
                    return null;
                }
                Date date = p.getTimestamp();
//                    date = new Date(105, 11, 22, 22, 22, 22); // testing if this fits to the table
                if (date == null) {
                    value = null;
                } else {
                    value = DateFormat.getInstance().format(date);
                }
                break;
            case COLUMN_UNMEASURED:
                p = Project.loadProject(file);
                if (p == null) {
                    return null;
                }
                date = p.getTimestamp();
                if (date == null) {
                    value = null;
                } else {
                    value = (new Date().getTime() - date.getTime()) / 3600000 + " h";
                }
                break;
            default:
                assert false;
                value = null;
                break;
            }

            // choose the style according to the project's state
            StyledWrapper wrapper;
            if (file.equals(measuringProjectFile)) {
                wrapper = measuringWrapper;
            } else if (file.equals(doneRecentlyProjectFile)) {
                wrapper = doneRecentlyWrapper;
            } else {
                wrapper = defaultWrapper;
            }
            wrapper.font = null;        // reset calibration notice font

            // styles for the calibration panel
            if (isCalibration) {
                Project p = Project.loadProject(file);
                if (p == null) {
                    return null;
                }
                Date date = p.getTimestamp();
                if (date == null) {
                    wrapper.font = calibrationNoticeFont;
                } else {
                    // alert the user if the calibration has not been done today
                    int hoursElapsed = (int) (new Date().getTime() - date.getTime()) / 3600000;
                    if (hoursElapsed >= 18) {
                        wrapper.font = calibrationNoticeFont;
                    }
                }
            }

            // return the wrapped value
            wrapper.value = value;
            return wrapper;
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return StyledWrapper.class;
        }

        /**
         * Event E: On ProjectEvent - hilight project whose measuring started, or unhilight one
         * whose measuring ended.
         */

        /**
         * Updates the file list when a project file has been saved and which project has a measurement running.
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

            } else if (event.getType() == ProjectEvent.Type.STATE_CHANGED) {
                File file = event.getProject().getFile();
                boolean repaintTable = false;

                Project.State state = event.getProject().getState();
                if (state == null) {
                    state = Project.State.IDLE; // avoid NullPointerException when recieving events from closed projects
                }
                switch (state) {
                case IDLE:
                    if (file.equals(measuringProjectFile)) {
                        // the project's measurement has just ended
                        measuringProjectFile = null;
                        doneRecentlyProjectFile = file;
                        repaintTable = true;
                    }
                    break;
                case MEASURING:
                case PAUSED:
                case ABORTED:
                    // the project has an active measurement
                    measuringProjectFile = file;
                    doneRecentlyProjectFile = null;
                    repaintTable = true;
                    break;
                default:
                    assert false;
                    break;
                }

                // repaint the table to show updated project states
                if (repaintTable) {
                    // save the selections, or they will be lost in fireTableDataChanged()
                    int[] selectedRows = ProjectExplorerTable.this.getSelectedRows();
                    fireTableDataChanged();
                    for (int i : selectedRows) {
                        ProjectExplorerTable.this.getSelectionModel().addSelectionInterval(i, i);
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
            switch (columns[explorerTableSortColumn]) { // translate sort column
            case COLUMN_FILENAME:
                return a.compareTo(b);
            case COLUMN_TYPE:
                // WARNING: might choke Project.getType(File) with O(n log n) requests
                Project.Type atype = Project.getType(a), btype = Project.getType(b);
                if (atype == null && btype == null) return 0;
                if (atype == null) return 1;
                if (btype == null) return -1;
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
         * files to export
         */
        private File[] files;

        /**
         * directory where to export by default
         */
        private File directory;

        /**
         * Builds the popup menu, but doesn't show it; use show(...) to do that.
         *
         * @param xfiles file(s) to show export menu for.
         */
        public ProjectExplorerPopupMenu(File[] xfiles) {
            if (xfiles == null || xfiles.length == 0) return; // stupid caller

            this.files = xfiles;
            this.directory = files[0].getParentFile();

            String filename, basename;
            if (files.length == 1) {
                filename = files[0].getName();
                basename = filename;
                if (basename.toLowerCase().endsWith(Ikayaki.FILE_TYPE)) {
                    basename = basename.substring(0, basename.length() - Ikayaki.FILE_TYPE.length());
                }
            } else {
                filename = "selected " + files.length + " files";
                basename = "*";
            }

            JMenuItem export = new JMenuItem("Export " + filename + " to");
            export.setFont(export.getFont().deriveFont(Font.BOLD));
            export.setEnabled(false);
            this.add(export);

            // TODO: some portable way to get a File for disk drive? Or maybe a Setting for export-dirs?
            for (File dir : new File[]{null, directory, new File("A:/")}) {
                for (String type : new String[]{"dat", "tdt", "srm"}) {
                    JMenuItem exportitem;
                    if (dir == null) {
                        exportitem = new JMenuItem(type.toUpperCase() + " file" +
                                (files.length > 1 ? "s" : "") + "...");
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
                            File exportdir;
                            boolean dirHasFile = false;

                            if (filetype.equals("...")) {
                                filetype = filename.substring(0, 3).toLowerCase();
                                JFileChooser chooser = new JFileChooser(directory);

                                if (files.length > 1) {
                                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                } else {
                                    dirHasFile = true; // we have explicit export-filename in exportdir
                                }

                                chooser.setFileFilter(
                                        new GenericFileFilter(filetype.toUpperCase() + " File", filetype));

                                if (chooser.showSaveDialog(ProjectExplorerTable.this) == JFileChooser.APPROVE_OPTION) {
                                    exportdir = chooser.getSelectedFile();
                                } else {
                                    return;
                                }

                            } else {
                                exportdir = new File(filename).getParentFile();
                            }

                            // execute export
                            for (File f : files) {
                                File exportfile;
                                if (dirHasFile) {
                                    exportfile = exportdir;
                                    if (!exportfile.getName().toLowerCase().endsWith("." + filetype)) {
                                        exportfile = new File(exportfile.toString() + "." + filetype);
                                    }
                                } else {
                                    String exportname = f.getName();
                                    if (exportname.toLowerCase().endsWith(Ikayaki.FILE_TYPE)) {
                                        exportname = exportname.substring(0,
                                                exportname.length() - Ikayaki.FILE_TYPE.length());
                                    }
                                    exportname += "." + filetype;
                                    exportfile = new File(exportdir, exportname);
                                }

                                System.out.print("Exporting " + exportfile + "... ");

                                boolean ok = false;
                                if (filetype.equals("dat")) {
                                    ok = Project.loadProject(f).exportToDAT(exportfile);
                                } else if (filetype.equals("tdt")) {
                                    ok = Project.loadProject(f).exportToTDT(exportfile);
                                } else if (filetype.equals("srm")) ok = Project.loadProject(f).exportToSRM(exportfile);

                                System.out.println(ok ? "ok" : "ERROR");
/*
                                // TODO: tell somehow, not with popup, if export was successful; statusbar perhaps?
                                Component c = ProjectExplorerTable.this;
                                while (c.getParent() != null) c = c.getParent();
                                if (!ok) JOptionPane.showMessageDialog(c, "Unable to write to " + exportfile,
                                                                       "Error exporting files", JOptionPane.ERROR_MESSAGE);
*/
                            }
                        }
                    });
                }
            }
        }
    }
} // NOTE: what a nice end-brace-fallout :)
