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

import ikayaki.Project;
import ikayaki.Settings;
import ikayaki.Ikayaki;
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
Event D: On browseButton click - open a FileChooser dialog for choosing new directory,
set it to directory, update files listing, update explorerTable and browserField.
*/
/*
Event E: On ProjectEvent - hilight project whose measuring started, or unhilight one
whose measuring ended.
*/

    /**
     * The component whose setProject() method will be called on opening a new project file.
     */
    private ProjectComponent parent;

    /**
     * Holds browserField and browseButton
     */
    private JPanel browsePanel = new JPanel();

    /**
     * Text field for writing directory to change to. Autocomplete results appear to Combo Box’ popup window, scheduled
     * by LastExecutor. Directory history appears to the same popup window when the down-arrow right to text field is
     * clicked.
     */
    private JComboBox browserField;
    private JTextField browserFieldEditor;

    /**
     * Tells whether the next-to-be-shown popup menu will be autocomplete list (and not directory history).
     */
    private boolean browserFieldNextPopupAutocomplete = false;

    private JButton browseButton;

    private JTable explorerTable;

    private ProjectExplorerTableModel explorerTableModel;

    private JScrollPane explorerTableScrollPane;

    private NewProjectPanel newProjectPanel;

    /**
     * LastExecutor for scheduling autocomplete results to separate thread (disk access and displaying).
     */
    private LastExecutor autocompleteExecutor = new LastExecutor(100, true);

    /**
     * Currently open directory.
     */
    private File directory = null;

    /**
     * Project files in current directory.
     */
    private File[] files = null;

    /**
     * Creates all components, sets directory as the last open directory, initializes files with files from that
     * directory.
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     */
    public ProjectExplorerPanel(ProjectComponent parent) {
        this(parent, null);
    }

    /**
     * Creates all components, sets directory to that of the specified project, initializes files with files from that
     * directory. Will NOT send an event to MainViewPanel to open the project.
     *
     * @param parent  the parent component whose setProject() method will be called on opening a new project file.
     * @param project the project whose directory is to be opened and which project is then selected, or null to use the
     *                last known directory.
     */
    public ProjectExplorerPanel(final ProjectComponent parent, Project project) {
        this.parent = parent;

        // set project directory to browserField
        if (project == null) setDirectory(getDirectoryHistory()[0].getAbsolutePath());
        else setDirectory(project.getFile().getAbsoluteFile().getParent());

        // combo box / text field
        browserField = new JComboBox(getDirectoryHistory());
        browserField.setEditable(true);
        browserField.setBackground(Color.WHITE);
        browserField.setPreferredSize(new Dimension(50, 20));
        // browserField.getEditor().getEditorComponent().setFocusTraversalKeysEnabled(false);
        browserFieldEditor = (JTextField) browserField.getEditor().getEditorComponent();

        // scroll to the end of the combo box's text field
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
                /* HACK:
                 * This hack will work only if we are currently in the event-dispatching thread.
                 * Otherwise the setCaretPosition will be executed before the GUI is visible,
                 * and the JTextField will not scroll automatically to show the caret.
                 */
                // UNHACK: works fine for me...
                // scroll the caret to be visible when the program starts
                setBrowserFieldCursorToEnd();
//            }
//        });

        // browse button
        browseButton = new JButton("Browse...");

        // add both into this
        browsePanel.setLayout(new BorderLayout());
        browsePanel.add(browserField, BorderLayout.CENTER);
        browsePanel.add(browseButton, BorderLayout.EAST);

        // project file table (and its table model)
        explorerTableModel = new ProjectExplorerTableModel();
        explorerTable = new JTable(explorerTableModel);
        explorerTableScrollPane = new JScrollPane(explorerTable);
        explorerTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        explorerTableScrollPane.getViewport().setBackground(Color.WHITE);
        // TODO: set column sizes somehow automatically, according to table contents?
        explorerTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        explorerTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        explorerTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        explorerTable.setPreferredScrollableViewportSize(new Dimension(280, 400));

        this.setLayout(new BorderLayout());
        this.add(browsePanel, BorderLayout.NORTH);
        this.add(explorerTableScrollPane, BorderLayout.CENTER);

        /**
         * Event C: On browserField popup window click - set clicked line as directory, update files
         * listing, update explorerTable and browserField.
         */
        browserField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println(e.getActionCommand());

                if (e.getActionCommand().equals("comboBoxEdited")) {
                    // the user pressed enter in the text field or selected an item by pressing enter
                    doAutoComplete();

                } else if (e.getActionCommand().equals("comboBoxChanged")) {


//                    System.out.println(browserField.getSelectedItem());
//
//                    // TODO: changing JComboBox popup-list content fires ActionEvents which we don't want... argh.
//                    if (!setDirectory((String) browserField.getSelectedItem())) {
//                        // TODO: how to display error?
//                        browserField.getEditor().selectAll();
//                    }
                }
            }
        });

        /**
         * Event B: On browserField down-arrow-click - show directory history in browserField’s popup window.
         */
        browserField.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (browserFieldNextPopupAutocomplete) browserFieldNextPopupAutocomplete = false;
                else setBrowserFieldPopup(getDirectoryHistory());
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        /**
         * Event A: On browserField change - send autocomplete-results-finder with browserField’s text
         * to LastExecutor via autocompleteExecutor.execute(Runnable), which schedules disk access and
         * displaying autocomplete results in browserField’s popup window.
         */
        browserFieldEditor.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE || e.getKeyChar() == KeyEvent.VK_ENTER) {
                    return;
                } else if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0 || (e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    // avoid the popup menu from showing, when the Project Explorer tab is hidden with ALT+P
                    browserField.hidePopup();
                    return;
                } else {
                    autocompleteExecutor.execute(new Runnable() {
                        public void run() {
                            doAutoComplete();
                        }
                    });
                }
            }
        });

        return; // TODO
    }

    private void doAutoComplete() {
        File[] files = getAutocompleteFiles(browserField.getEditor().getItem().toString());
        Arrays.sort(files);
        setBrowserFieldPopup(files);

        if (files.length > 0) {
            // gui updating must be done from event-dispatching thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // when the popup is hidden before showing, it will be automatically resized
                    // -- but, it flickers awkwardly; also don't know what you mean by that autoresize?
                    // if (browserField.isPopupVisible()) browserField.hidePopup();

                    browserFieldNextPopupAutocomplete = true;
                    browserField.showPopup();
                }
            });
        }
    }

    /**
     * Call super.setProject(project), hilight selected project, or unhilight unselected project.
     *
     * @param project project opened.
     */
    public void setProject(Project project) {
        return; // TODO
    }

    /**
     * Attempts to change to the selected directory.
     *
     * @param dir directory to change to.
     * @return true if succesful, false otherwise.
     */
    private boolean setDirectory(String dir) {
        if (dir == null || !new File(dir).isDirectory()) return false;

        directory = new File(dir);
        files = getProjectFiles(directory);
//        updateDirectoryHistory(directory); // this is already done in MainViewPanel when opening a project

        // update table with new directory
        if (explorerTableModel != null) explorerTableModel.fireTableDataChanged();

        return true;
    }

    /**
     * Reads project file listing from given directory.
     *
     * @param directory directory whose project file listing to read.
     * @return project files in that directory, sorted alphabetically.
     */
    private File[] getProjectFiles(File directory) {
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(Ikayaki.FILE_TYPE));
            }
        });
        Arrays.sort(files);
        return files;
    }

    /**
     * Reads current directory history from Settings. If directory history is empty, returns current directory instead.
     *
     * @return current directory history, or if empty, only current directory (as File[0]).
     */
    private File[] getDirectoryHistory() {
        File[] dirhist = Settings.instance().getDirectoryHistory();

        if (dirhist == null || dirhist.length == 0) return new File[] { new File("").getAbsoluteFile() };
        else return dirhist;
    }

//    /**
//     * Attemps to add given directory into dir history.
//     *
//     * @param dir directory to add.
//     * @deprecated This is done in MainViewPanel
//     */
//    private void updateDirectoryHistory(File dir) {
//        Settings.instance().updateDirectoryHistory(dir);
//    }

    /**
     * Reads matching directories from given directory name's parent.
     *
     * @param dirmatch beginning of directory to which match the directories in its parent directory...
     * @return matching directories.
     */
    private File[] getAutocompleteFiles(String dirmatch) {
        File dirfile = new File(dirmatch);
        if (!dirfile.isAbsolute()) {
            return File.listRoots();
        }
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
     * Sets browserField popup-menu-list as given files; also clears any selection.
     *
     * @param files list of files to set the list to.
     */
    private void setBrowserFieldPopup(File[] files) {
        // purkkaillaan -- some hardcore bubblegum stitching
        String browserFieldEditorText = browserFieldEditor.getText();
        int browserFieldEditorCursorPosition = browserFieldEditor.getCaretPosition();

        browserField.removeAllItems();
        for (File file : files) browserField.addItem(file.getAbsolutePath());

        browserField.setSelectedIndex(-1);
        browserFieldEditor.setText(browserFieldEditorText);
        browserFieldEditor.setCaretPosition(browserFieldEditorCursorPosition);
    }

    /**
     * Sets browserField's cursor to text field's (right) end.
     */
    private void setBrowserFieldCursorToEnd() {
        browserFieldEditor.setCaretPosition(browserFieldEditor.getDocument().getLength());
    }

    /**
     * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu
     * (ProjectExplorerPopupMenu). Inner class of ProjectExplorerPanel.
     *
     * @author Samuli Kaipiainen
     */
    // TODO: loose comment above...

    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerPanel).
     */
    private class ProjectExplorerTableModel extends AbstractTableModel {
        /*
             Event A: On table click - call Project.loadProject(File) with clicked project file, call
             MainViewPanel.changeProject(Project) with returned Project unless null, on which case
             show error message and revert explorerTable selection to old project, if any.
         */
        /*
             Event B: On table mouse right-click - create a ProjectExplorerPopupMenu for rightclicked
             project file.
         */

        private final String[] columns = { "filename", "type", "last modified" };

        public String getColumnName(int column) {
            return columns[column];
        }

        public int getRowCount() {
            return files.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0: return files[row].getName();
                case 1: return Project.getType(files[row]);
                case 2: return DateFormat.getInstance().format(files[row].lastModified());
                default: assert(false); return null;
            }
        }
    }
}
