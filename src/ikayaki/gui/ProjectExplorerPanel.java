/*
* ProjectExplorerPanel.java
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
import ikayaki.util.LastExecutor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.File;
import java.io.*;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Creates a history/autocomplete field (browserField) for choosing the project directory, a listing of project files in
 * that directory (explorerTable) and in that listing a line for creating new project, which has a textbox for project
 * name, an AF/TH ComboBox and a "Create new" button (createNewProjectButton) for actuating the creation. Also has a
 * right-click popup menu for exporting project files.
 *
 * @author Samuli Kaipiainen
 */
public class ProjectExplorerPanel extends ProjectComponent {
/*
Event E: On ProjectEvent - hilight project whose measuring started, or unhilight one
whose measuring ended.
*/

    /**
     * The component whose setProject() method will be called on opening a new project file.
     */
    private final ProjectComponent parent;

    /**
     * Holds browserField and browseButton
     */
    private final JPanel browsePanel = new JPanel();

    /**
     * Text field for writing directory to change to. Autocomplete results appear to Combo Box’ popup window, scheduled
     * by LastExecutor. Directory history appears to the same popup window when the down-arrow right to text field is
     * clicked.
     */
    private final JComboBox browserField;
    private final JTextField browserFieldEditor; // WARNING: look-and-feel-dependant code
    private final ComponentFlasher browserFieldFlasher;

    /**
     * Tells whether the next-to-be-shown popup menu will be autocomplete list (and not directory history).
     */
    private boolean browserFieldNextPopupIsAutocomplete = false;

    /**
     * Tells whether browserField's popup menu list is being updated, and we don't want those ActionEvents.
     */
    boolean browserFieldUpdatingPopup = false;

    private final JButton browseButton;

    private final JTable explorerTable;

    private final ProjectExplorerTableModel explorerTableModel;

    private final JScrollPane explorerTableScrollPane;

    private final Comparator <File> explorerTableComparator = new ExplorerTableComparator();

    private int explorerTableSortColumn = COLUMN_UNDEFINED;

    private static final int COLUMN_UNDEFINED = -1;
    private static final int COLUMN_FILENAME = 0;
    private static final int COLUMN_TYPE = 1;
    private static final int COLUMN_LASTMOD = 2;
    private static final String[] column_name = { "filename", "type", "last modified" };

    private NewProjectPanel newProjectPanel;

    /**
     * LastExecutor for scheduling autocomplete results to separate thread (disk access and displaying).
     */
    private final LastExecutor autocompleteExecutor = new LastExecutor(100, true);

    /**
     * Currently open directory.
     */
    private File directory = null;

    /**
     * Project files in current directory.
     */
    private File[] files = new File[0];

    /**
     * Selected project file index, or -1 if none selected in current directory.
     */
    private int selectedFile = -1;

    /**
     * Creates all components, sets directory as the last open directory, initializes files with files from that
     * directory.
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     */
    public ProjectExplorerPanel(ProjectComponent parent) {
        this.parent = parent;

        // set current directory to latest directory history dir
        setDirectory(Settings.instance().getLastDirectory());

        // combo box / text field
        browserField = new JComboBox();
        browserField.setEditable(true);
        browserField.setBackground(Color.WHITE);
        browserFieldEditor = (JTextField) browserField.getEditor().getEditorComponent();
        browserFieldFlasher = new ComponentFlasher(browserFieldEditor);
        // browserFieldEditor.setFocusTraversalKeysEnabled(false); // disable tab-exiting from browserField
        setBrowserFieldPopup(getDirectoryHistory());

        // scroll to the end of the combo box's text field
        setBrowserFieldCursorToEnd();

        // browse button
        browseButton = new JButton("Browse...");

        // add both into this
        browsePanel.setLayout(new BorderLayout());
        //browsePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        browsePanel.add(browserField, BorderLayout.CENTER);
        browsePanel.add(browseButton, BorderLayout.EAST);

        // project file table (and its table model)
        // TODO: these should be in inner class ProjectExplorerTable
        explorerTableModel = new ProjectExplorerTableModel();
        explorerTable = new JTable(explorerTableModel);
        explorerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        explorerTable.getTableHeader().setReorderingAllowed(false);
        // explorerTable.getTableHeader().setResizingAllowed(false);
        // TODO: the grid still shows up when selecting rows. must make a custom cell renderer to change that
        explorerTable.setShowGrid(false);
        explorerTableScrollPane = new JScrollPane(explorerTable);
        // explorerTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        explorerTableScrollPane.getViewport().setBackground(Color.WHITE);
        // TODO: set column sizes somehow automatically, according to table contents?
        explorerTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        explorerTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        explorerTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        explorerTable.setPreferredScrollableViewportSize(new Dimension(280, 400));

        // new project panel
        newProjectPanel = new NewProjectPanel();

        this.setLayout(new BorderLayout());
        this.add(browsePanel, BorderLayout.NORTH);
        this.add(explorerTableScrollPane, BorderLayout.CENTER);
        this.add(newProjectPanel, BorderLayout.SOUTH);

        // ProjectExplorer events

        /**
         * Event D: On browseButton click - open a FileChooser dialog for choosing new directory,
         * set it to directory, update files listing, update explorerTable and browserField.
         */
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(directory);
                fc.setDialogTitle("Change directory");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fc.showOpenDialog(browseButton) == JFileChooser.APPROVE_OPTION)
                    setDirectory(fc.getSelectedFile());
            }
        });

        /**
         * Event C: On browserField popup window click - set clicked line as directory, update files
         * listing, update explorerTable and browserField.
         */
        browserField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // check if event is caused by popup menu update
                if (browserFieldUpdatingPopup) return;

                // JComboBox event types:
                // Edited -- pressed enter in the text field or selected an item by pressing enter
                // Changed -- cycled item list with up/down, selected item with mouse or pressed enter in text field
                System.out.println(e.getActionCommand() + ": " + browserField.getSelectedItem());

                // we only want changed-events, not duplicate edited-events
                if (!e.getActionCommand().equals("comboBoxChanged")) return;

                // TODO: cycling through popup menu list with up/down keys changes directory;
                // it shouldn't, but can't recognize those changed-events from mouse clicks

                // try to set directory, flash browserField red if error
                if (!setDirectory(new File((String) browserField.getSelectedItem())))
                    browserFieldFlasher.flash();
            }
        });

        /**
         * Event B: On browserField down-arrow-click - show directory history in browserField’s popup window.
         */
        browserField.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // set popup menu as directory history when closing popup, unless autocomplete-resize-closing
                // (popupmenu directory history might lag when hidden)
                if (browserFieldNextPopupIsAutocomplete) browserFieldNextPopupIsAutocomplete = false;
                else setBrowserFieldPopup(getDirectoryHistory());
                // TODO: browserField's text disappears when selectiog item with mouse, because of
                // this stuff here instead of popupMenuWillBecomeVisible, but that caused other probrems
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
            public void popupMenuCanceled(PopupMenuEvent e) { }
        });

        /**
         * Event A: On browserField change - send autocomplete-results-finder with browserField’s text
         * to LastExecutor via autocompleteExecutor.execute(Runnable), which schedules disk access and
         * displaying autocomplete results in browserField’s popup window.
         */
        browserFieldEditor.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    browserField.setSelectedItem(directory.getPath());
                    browserField.getEditor().selectAll();
                    return;
                } else if (e.getKeyChar() == KeyEvent.VK_ENTER) return;

                if (e.getModifiers() == KeyEvent.CTRL_MASK && e.getKeyChar() == KeyEvent.VK_DELETE) {
                    // delete one directory name at a time
                    int pos = browserFieldEditor.getCaretPosition();
                    String text = browserFieldEditor.getText();
                    String textA = text.substring(0, pos);
                    String textB = text.substring(pos);
                    textA = textA.substring(0, Math.max(0, textA.lastIndexOf(System.getProperty("file.separator"))));
                    browserFieldEditor.setText(textA + textB);
                    browserFieldEditor.setCaretPosition(textA.length());

                } else if ( (e.getModifiers() & KeyEvent.ALT_MASK) != 0 || (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    // avoid the popup menu from showing, when the Project Explorer tab is hidden with ALT+P
                    browserField.hidePopup();
                    return;
                }

                autocompleteExecutor.execute(new Runnable() {
                    public void run() {
                        doAutoComplete();
                    }
                });
            }
        });

        // ProjectExplorerTable events
        // TODO: these should be in inner class ProjectExplorerTable

        /**
         * Event A: On table click - call Project.loadProject(File) with clicked project file, call
         * (MainViewPanel) parent.setProject(Project) with returned Project unless null, on which case
         * show error message and revert explorerTable selection to old project, if any.
         */
        explorerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // we only want the actually selected row, and don't want to react to an already selected line
                // (which could also mean that we had a load error, and that selection was reverted)
                if (e.getValueIsAdjusting() || explorerTable.getSelectedRow() == selectedFile) return;
                if (explorerTable.getSelectedRow() == -1) return; // otherwise will crash the program upon loading a file

                Project project = Project.loadProject(files[explorerTable.getSelectedRow()]);

                // load error, revert back to old selection
                if (project == null) {
                    // TODO: flash selected row red for 100 ms, perhaps? - might require a custom cell renderer
                    if (selectedFile == -1) explorerTable.clearSelection();
                    else explorerTable.setRowSelectionInterval(selectedFile, selectedFile);
                } else {
                    // super.setProject nod needed; MainViewPanel (parent) calls our setProject anyway
                    // ProjectExplorerPanel.super.setProject(project);
                    ProjectExplorerPanel.this.parent.setProject(project);
                }
            }
        });

        /**
         * Event B: On table mouse right-click - create a ProjectExplorerPopupMenu for rightclicked
         * project file.
         */
        explorerTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // only right-click brings popup menu
                if (e.getButton() != MouseEvent.BUTTON3) return;

                int row = explorerTable.rowAtPoint(e.getPoint());

                String basename = files[row].getName();
                if (basename.toLowerCase().endsWith(Ikayaki.FILE_TYPE))
                    basename = basename.substring(0, basename.length() - Ikayaki.FILE_TYPE.length());

                // construct the popup menu for every click
                JPopupMenu explorerTablePopup = new JPopupMenu();
                JMenuItem filename = new JMenuItem("Export '" + files[row].getName() + "' to");
                filename.setFont(filename.getFont().deriveFont(Font.BOLD));
                filename.setEnabled(false);
                explorerTablePopup.add(filename);

                // TODO: some portable way to get a File for disk drive? Or maybe a Setting for export-dirs?
                for (File dir : new File[] { null, directory, new File("A:/") }) {
                    for (String type : new String[] {"dat", "tdt", "srm"}) {
                        File exportFile = new File(dir, basename + "." + type);

                        JMenuItem item;
                        if (dir == null) item = new JMenuItem(type.toUpperCase() + " file...");
                        else item = new JMenuItem(exportFile.toString());

                        explorerTablePopup.add(item);

                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                String filename = e.getActionCommand();
                                String filetype = filename.substring(filename.length() - 3);
                                File file;

                                if (filetype.equals("...")) {
                                    filetype = filename.substring(0, 3).toLowerCase();
                                    JFileChooser chooser = new JFileChooser(directory);
                                    chooser.setFileFilter(new GenericFileFilter(filetype.toUpperCase() + " File", filetype));

                                    if (chooser.showSaveDialog(explorerTable) == JFileChooser.APPROVE_OPTION)
                                        file = chooser.getSelectedFile();

                                } else file = new File(filename);

                                // TODO: which one of these two?
                                //Project.export(file, filetype);
                                //Project.loadProject(file).export(filetype);

                                // TODO: tell somehow if export was successful; statusbar perhaps?
                            }
                        });
                    }
                }

                explorerTablePopup.show(explorerTable, e.getX(), e.getY());
            }
        });

        /**
         * ExplorerTable sorting.
         */
        explorerTable.getTableHeader().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // only left-click changes sorting
                if (e.getButton() != MouseEvent.BUTTON1) return;

                JTableHeader th = (JTableHeader) e.getSource();
                TableColumnModel cm = th.getColumnModel();
                int viewColumn = cm.getColumnIndexAtX(e.getX());

                // TODO: what the sick hell of chainsaw internals do I have to touch just to update table headers?!?
                // ... Hope it's at least correct, not that I care anymore

                // reset all column header names
                for (int col = 0; col < cm.getColumnCount(); col++)
                    cm.getColumn(col).setHeaderValue(column_name[cm.getColumn(col).getModelIndex()]);

                // set sort column header name
                explorerTableSortColumn = cm.getColumn(viewColumn).getModelIndex();
                cm.getColumn(viewColumn).setHeaderValue(column_name[explorerTableSortColumn] + " *");
                th.repaint();

                Arrays.sort(files, explorerTableComparator);

                // update table with sorted data, update selected file and table selection
                explorerTableModel.fireTableDataChanged();
                updateSelectedFile();
            }
        });
    }

    /**
     * Call super.setProject(project), hilight selected project, or unhilight unselected project.
     *
     * @param project project opened.
     */
    public void setProject(Project project) {
        super.setProject(project);
        if (project != null) {
            setDirectory(project.getFile().getParentFile());
            project.addProjectListener(explorerTableModel);
        }
        else setDirectory(directory);
    }

    /**
     * Attempts to change to the given directory. Updates browserField and explorerTable with new directory.
     *
     * @param directory directory to change to.
     * @return true if succesful, false otherwise.
     */
    private boolean setDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) return false;

        this.directory = directory;
        files = getProjectFiles(directory);

        // needs to be sorted here, because MainViewPanel calls our setProject when changing project
        if (explorerTableSortColumn != COLUMN_UNDEFINED) Arrays.sort(files, explorerTableComparator);

        // update browserField and explorerTable with new directory
        if (browserField != null) browserField.setSelectedItem(directory.getPath());
        if (explorerTableModel != null) explorerTableModel.fireTableDataChanged();
        if (explorerTable != null) updateSelectedFile();

        return true;
    }

    /**
     * Reads project file listing from given directory.
     *
     * @param directory directory whose project file listing to read.
     * @return project files in that directory.
     */
    private File[] getProjectFiles(File directory) {
        return directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                // TODO: shouldn't this return only a list of valid project files? so why is Ikayaki.FILE_TYPE commented out?
                // - for explorerTable testing, need some (working perhaps) project file expamples :)
                return (file.isFile() && file.getName().endsWith(/*Ikayaki.FILE_TYPE*/ ""));
            }
        });
    }

    /**
     * Reads current directory history from Settings.
     *
     * @return current directory history. Should never return null.
     */
    private File[] getDirectoryHistory() {
        return Settings.instance().getDirectoryHistory();
    }

    /**
     * Reads matching directories from given directory name's parent.
     *
     * @param dirmatch beginning of directory to which match the directories in its parent directory...
     * @return matching directories.
     */
    private File[] getAutocompleteFiles(String dirmatch) {
        File dirfile = new File(dirmatch);
        if (!dirfile.isAbsolute()) return File.listRoots();
        File dir = dirfile.isDirectory() ? dirfile : dirfile.getParentFile();

        // protect against no-parent-null and invalid-dir-list-null
        if (dir == null) dir = directory;
        else if (!dir.isDirectory()) return new File[0];

        final String match = dirfile.isDirectory() ? "" : dirfile.getName();

        return dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory() && file.getName().toLowerCase().startsWith(match.toLowerCase()));
            }
        });
    }

    /**
     * Updates autocomplete popup-menu.
     */
    private void doAutoComplete() {
        File[] files = getAutocompleteFiles(browserField.getEditor().getItem().toString());
        setBrowserFieldPopup(files);

        if (files.length > 0) {
            // gui updating must be done from event-dispatching thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    browserFieldNextPopupIsAutocomplete = true;
                    // when the popup is hidden before showing, it will be automatically resized
                    if (browserField.isPopupVisible()) browserField.hidePopup();
                    browserField.showPopup();
                }
            });
        }
    }

    /**
     * Updates selectedFile index and explorerTable selection with selected project file, or -1.
     */
    private void updateSelectedFile() {
        selectedFile = -1;
        if (getProject() != null) for (int n = 0; n < files.length; n++)
            if (getProject().getFile().equals(files[n])) selectedFile = n;

        if (selectedFile != -1) explorerTable.setRowSelectionInterval(selectedFile, selectedFile);
    }

    /**
     * Sets browserField popup-menu-list as given files; also clears any selection.
     *
     * @param files list of files to set the list to.
     */
    private void setBrowserFieldPopup(File[] files) {
        // purkkaillaan -- some hardcore bubblegum stitching (the whole method)
        browserFieldUpdatingPopup = true;

        String browserFieldEditorText = browserFieldEditor.getText();
        int browserFieldEditorCursorPosition = browserFieldEditor.getCaretPosition();

        browserField.removeAllItems();
        for (File file : files) browserField.addItem(file.getAbsolutePath());

        browserField.setSelectedIndex(-1);
        browserFieldEditor.setText(browserFieldEditorText);
        browserFieldEditor.setCaretPosition(browserFieldEditorCursorPosition);

        browserFieldUpdatingPopup = false;
    }

    /**
     * Sets browserField's cursor to text field's (right) end.
     */
    private void setBrowserFieldCursorToEnd() {
        browserFieldEditor.setCaretPosition(browserFieldEditor.getDocument().getLength());
    }

    /**
     * Comparator used for ExplorerTable sorting.
     */
    private class ExplorerTableComparator implements Comparator <File> {
        public int compare(File a, File b) {
            switch (explorerTableSortColumn) {
                case COLUMN_FILENAME: return a.compareTo(b);
                // WARNING: might chocke Project.getType(File)
                case COLUMN_TYPE: return Project.getType(a).compareTo(Project.getType(b));
                // TODO: int-cast changes sign if difference larger than maxint
                case COLUMN_LASTMOD: return (int) (a.lastModified() - b.lastModified());
                default: return 0;
            }
        }
    }

    /**
     * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu
     * (ProjectExplorerPopupMenu). Inner class of ProjectExplorerPanel.
     *
     * @author Samuli Kaipiainen
     */
    // TODO: comment above awaiting for refactoring into ProjectExplorerTable class

    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerPanel).
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
                case COLUMN_FILENAME: return files[row].getName();
                case COLUMN_TYPE: return Project.getType(files[row]);
                case COLUMN_LASTMOD: return DateFormat.getInstance().format(files[row].lastModified());
                default: assert false; return null;
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
                        fireTableRowsUpdated(i, i);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Panel with components for creating a new project. This Panel will be somewhere below the project file listing...
     */
    public class NewProjectPanel extends JPanel {

        private final JTextField newProjectName;
        private final JComboBox newProjectType;
        private final JButton createNewProjectButton;
        private final JPanel flowPanel;
        private final ComponentFlasher newProjectNameFlasher;

        public NewProjectPanel() {
            super(new BorderLayout());

            newProjectName = new JTextField();
            newProjectType = new JComboBox(Project.Type.values());
            newProjectType.setSelectedItem(Project.Type.AF);
            newProjectType.setBackground(Color.WHITE);
            createNewProjectButton = new JButton("Create new");

            flowPanel = new JPanel(new BorderLayout());
            flowPanel.add(newProjectType, BorderLayout.WEST);
            flowPanel.add(createNewProjectButton, BorderLayout.EAST);

            this.add(newProjectName, BorderLayout.CENTER);
            this.add(flowPanel, BorderLayout.EAST);

            newProjectNameFlasher = new ComponentFlasher(newProjectName);

            /**
             * Event A: On createNewProjectButton click - call Project.createProject(File, Type) with
             * filename from newProjectField; if returns null, show error message and do nothing. Otherwise,
             * update file listing, set new project active, tell explorerTable to reset newProjectField and
             * newProjectType and call (MainViewPanel) parent.setProject(Project) with returned Project.
             */
            createNewProjectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String name = newProjectName.getText();
                    if (name.length() > 0 && !name.toLowerCase().endsWith(Ikayaki.FILE_TYPE))
                        name += Ikayaki.FILE_TYPE;

                    Project.Type type = (Project.Type) newProjectType.getSelectedItem();

                    File file = new File(directory, name);

                    // TODO: should we check here if the file is legitimate?

                    Project created = Project.createProject(file, type);

                    if (created == null) newProjectNameFlasher.flash();
                    else parent.setProject(created);
                }
            });

            /**
             * Pressing enter in newProjectName text field.
             */
            newProjectName.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createNewProjectButton.doClick();
                }
            });
        }
    }

    /**
     * Timer used for flashing a JComponent background light red (or given color), for 100 ms (or given time).
     */
    private static class ComponentFlasher extends Timer {

        private final JComponent component;
        private final Color componentBG;
        private final Color flashcolor;
        private static final Color defauldFlashColor = new Color(0xff8080);

        public ComponentFlasher(JComponent component) {
            this(component, defauldFlashColor, 100);
        }

        public ComponentFlasher(JComponent component, Color flashcolor) {
            this(component, flashcolor, 100);
        }

        public ComponentFlasher(JComponent component, int flashtime) {
            this(component, defauldFlashColor, flashtime);
        }

        public ComponentFlasher(JComponent component, Color flashcolor, int flashtime) {
            super(flashtime, null);
            this.component = component;
            this.componentBG = component.getBackground();
            this.flashcolor = flashcolor;
            this.setRepeats(false);

            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ComponentFlasher.this.component.setBackground(componentBG);
                }
            });
        }

        public void flash() {
            component.setBackground(flashcolor);
            super.start();
        }
    }
}
