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
import ikayaki.util.LastExecutor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.File;
import java.io.*;
import java.text.DateFormat;

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
Event A: On browserField change - send autocomplete-results-finder with browserField’s
text to LastExecutor via lastExecutor.execute(Runnable), which schedules disk access and
displaying autocomplete results in browserField’s popup window.
*/
/*
Event B: On browserField down-arrow-click - show directory history in browserField’s
popup window.
*/
/*
Event C: On browserField popup window click - set clicked line as directory, update files
listing, update explorerTable and browserField.
*/
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

        // TODO: where is the last directory?
        if (project == null) setDirectory("."); // Settings.instance().getLastDirectory();
        else {
            setDirectory(
                    project.getFile().getAbsolutePath().substring(
                            0,
                            project.getFile().getAbsolutePath().lastIndexOf(System.getProperty("file.separator"))));
            //setDirectory(project.getFile().getPath());
        }
        System.out.println(

        );
        // combo box / text field
        browserField = new JComboBox(getDirectoryHistory());
        browserField.setSelectedItem(directory.getName());
        browserField.setEditable(true);
        browserField.setBackground(Color.WHITE);

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

        browserField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(browserField.getSelectedItem());
                if (!setDirectory((String) browserField.getSelectedItem())) {
                    // TODO: how to display error?
                    browserField.getEditor().selectAll();
                }
            }
        });

        return; // TODO
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
        if (!new File(dir).isDirectory()) return false;

        directory = new File(dir);
        files = getProjectFiles(directory);

        // update table with new directory
        if (explorerTableModel != null) explorerTableModel.fireTableDataChanged();

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
                return (file.isFile() && !file.getName().endsWith(".ika"));
            }
        });
    }

    /**
     * Gets current directory history from TODO.
     *
     * @return current directory history.
     */
    private String[] getDirectoryHistory() {
        return new String[] { directory.getAbsolutePath(), "resources", "mursukas", "heppa", "marsupapana" };

        // TODO:
        //return Settings.instance().getDirectoryHistory();
    }

    /**
     * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu
     * (ProjectExplorerPopupMenu). Inner class of ProjectExplorerPanel.
     *
     * @author Samuli Kaipiainen
     */
    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerPanel). Unnamed inner class.
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

        // TODO: probably not needed
        //public Class getColumnClass(int column) {
        //    return getValueAt(0, column).getClass();
        //}

        public int getRowCount() {
            return files.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0: return files[row].getName();
                // TODO: how to get the type?
                case 1: return "type"; //Project.loadProject(files[row]).getType().name();
                case 2: return DateFormat.getInstance().format(files[row].lastModified());
                default: assert(false); return null;
            }
        }
    }
}
