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
import java.io.*;

/**
 * Creates a history/autocomplete field (browserField) for choosing the project directory, a listing of project files in
 * that directory (explorerTable) and in that listing a line for creating new project, which has a textbox for project
 * name, an AF/TH ComboBox and a "Create new" button (createNewProjectButton) for actuating the creation. Also has a
 * right-click popup menu for exporting project files.
 *
 * @author Samuli Kaipiainen
 */
public class ProjectExplorerPanel extends ProjectComponent {
    /**
     * The component (MainViewPanel) whose setProject() method will be called on opening a new project file.
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
    private final BrowserFieldRenderer browserFieldRenderer;
    private final JTextField browserFieldEditor; // WARNING: look-and-feel-dependant code
    private final ComponentFlasher browserFieldFlasher;

    /**
     * Tells whether current popup menu is autocomplete list (and not directory history).
     */
    boolean browserFieldPopupIsAutocomplete = false;

    /**
     * Tells whether the next-to-be-shown popup menu will be autocomplete list (and not directory history).
     */
    // private boolean browserFieldNextPopupIsAutocomplete = false;

    /**
     * Tells whether browserField's popup menu list is being updated, and we don't want those ActionEvents.
     */
    boolean browserFieldUpdatingPopup = false;

    private final JButton browseButton;

    private final ProjectExplorerTable explorerTable;
    private final JScrollPane explorerTableScrollPane;

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
     * Call next constructor...
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     */
    public ProjectExplorerPanel(ProjectComponent parent) {
        this(parent, null);
    }

    /**
     * Creates all components, sets directory as the last open directory or opened project's directory,
     * initializes files with files from that directory.
     *
     * @param parent the component whose setProject() method will be called on opening a new project file.
     * @param project project to load and whose directory to set as current directory.
     */
    public ProjectExplorerPanel(ProjectComponent parent, Project project) {
        this.parent = parent;

        // combo box / text field
        browserField = new JComboBox(getDirectoryHistory());
        browserField.setEditable(true);
        browserField.setBackground(Color.WHITE);
        browserFieldEditor = (JTextField) browserField.getEditor().getEditorComponent();
        browserFieldFlasher = new ComponentFlasher(browserFieldEditor);
        // browserFieldEditor.setFocusTraversalKeysEnabled(false); // disable tab-exiting from browserField

        // custom renderer for browserField's items so that long path names are right-justified in the popup menu
        browserFieldRenderer = new BrowserFieldRenderer();
        //DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        //renderer.setHorizontalAlignment(DefaultListCellRenderer.TRAILING);
        //browserField.setRenderer(renderer);
        //browserFieldRenderer.setPreferredSize(new Dimension(100, 20));
        browserField.setRenderer(browserFieldRenderer);

        // browse button
        browseButton = new JButton("Browse...");

        // add both into this
        browsePanel.setLayout(new BorderLayout());
        //browsePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        browsePanel.add(browserField, BorderLayout.CENTER);
        browsePanel.add(browseButton, BorderLayout.EAST);

        // project file table and its ScrollPane
        explorerTable = new ProjectExplorerTable(this.parent);
        explorerTableScrollPane = new JScrollPane(explorerTable);
        // explorerTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        explorerTableScrollPane.getViewport().setBackground(Color.WHITE);

        // new project panel
        newProjectPanel = new NewProjectPanel();

        this.setLayout(new BorderLayout());
        this.add(browsePanel, BorderLayout.NORTH);
        this.add(explorerTableScrollPane, BorderLayout.CENTER);
        this.add(newProjectPanel, BorderLayout.SOUTH);

        // set current directory to project dir or latest directory history dir
        if (project != null) setDirectory(project.getFile().getParentFile());
        else setDirectory(Settings.instance().getLastDirectory());

        // scroll to the end of the combo box's text field (after setting directory) -- seems to happen anyway
        // setBrowserFieldCursorToEnd();

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
                // System.out.println(e.getActionCommand() + ": " + browserField.getSelectedItem());

                // we only want changed-events, not duplicate edited-events -- actually, we want those too,
                // so that multiple tries to change to an invalid directory with enter will flash text field
                // if (!e.getActionCommand().equals("comboBoxChanged")) return;

                // TODO: cycling through popup menu list with up/down keys changes directory;
                // it shouldn't, but can't recognize those changed-events from mouse clicks

                // item is File if selected from list, String if written to text field
                Object item = browserField.getEditor().getItem();
                File dir = item instanceof File ? (File) item : new File((String) item);

                // try to set directory, flash browserField red if error
                if (!setDirectory(dir)) browserFieldFlasher.flash();
            }
        });

        /**
         * Event B: On browserField down-arrow-click - show directory history in browserField’s popup window.
         */
        browserField.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // set popup menu as directory history when closing popup, unless it's already dir history
                if (browserFieldPopupIsAutocomplete) {
                    browserFieldPopupIsAutocomplete = false;
                    // TODO: when mouseclicking autocomplete list item, textfield gets cleared because of this
                    //Object item = browserField.getSelectedItem();
                    setBrowserFieldPopup(getDirectoryHistory());
                    //browserField.setSelectedItem(item);
                }
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
                    // browserField.getEditor().selectAll();
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
    }

    /**
     * Call super.setProject(project), hilight selected project, or unhilight unselected project.
     *
     * @param project project opened, or null to open no project.
     */
    public void setProject(Project project) {
        super.setProject(project);

        // update directory history, as it might have changed
        browserField.hidePopup();
        setBrowserFieldPopup(getDirectoryHistory());

        // change directory, if not calibration project; in that case just update selected project in explorerTable
        if (project != null && project.getType() != Project.Type.CALIBRATION) {
            setDirectory(project.getFile().getParentFile());
        } else explorerTable.setDirectory(directory);

        // add explorerTable as a ProjectListener so it can update current project's timestamps
        if (project != null) project.addProjectListener(explorerTable);
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

        // update browserField and explorerTable with new directory
        browserField.setSelectedItem(this.directory);
        explorerTable.setDirectory(this.directory);

        return true;
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
        final File[] files = getAutocompleteFiles(browserField.getEditor().getItem().toString());

        // gui updating must be done from event-dispatching thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // when the popup is hidden before showing, it will be automatically resized
                // (disable change-to-dirhistory-on-popup-hide for this)
                browserFieldPopupIsAutocomplete = false;
                browserField.hidePopup();

                browserFieldPopupIsAutocomplete = true;
                setBrowserFieldPopup(files);
                if (files.length > 0) browserField.showPopup();
            }
        });
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
        for (File file : files) browserField.addItem(file);

        browserField.setSelectedIndex(-1);
        browserFieldEditor.setText(browserFieldEditorText);
        browserFieldEditor.setCaretPosition(browserFieldEditorCursorPosition);

        browserFieldUpdatingPopup = false;
    }

    /**
     * Sets browserField's cursor to text field's (right) end.
     *
     * @deprecated not needed anymore; cursor seems to be there anyway?
     */
    private void setBrowserFieldCursorToEnd() {
        browserFieldEditor.setCaretPosition(browserFieldEditor.getDocument().getLength());
    }

    /**
     * Custom renderer for browserField's popup menu items.
     */
    private class BrowserFieldRenderer extends JLabel implements ListCellRenderer {

        private int height;

        /**
         * Creates an opaque JLabel with a small border.
         */
        public BrowserFieldRenderer() {
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 2));
            //setEnabled(false);
            //setHorizontalAlignment(RIGHT);
            //System.out.println(getPreferredSize());
            height = getPreferredSize().height;
        }

        /**
         * Returns a JLabel with long directory names right-justified.
         *
         * @param list a JList object used behind the scenes to display the items.
         * @param value the Object to render; the directory (File) that is.
         * @param index the index of the object to render.
         * @param isSelected indicates whether the object to render is selected.
         * @param cellHasFocus indicates whether the object to render has the focus.
         * @return custom renderer Component (JLabel).
         */
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(browserFieldEditor.getSelectionColor());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setText(value.toString());
            //setSize(browserField.getWidth(), getHeight());
            setMaximumSize(new Dimension(browserField.getWidth(), height));
            list.setMaximumSize(new Dimension(browserField.getWidth(), height));
            //setCaretPosition(getText().length());
            //setCaretPosition(getDocument().getLength());
            repaint();

            //System.out.println(browserField.getWidth() + " " + height);
            //System.out.println(list.getWidth() + " " + list.getFixedCellHeight());

            return this;
        }
    }

    /**
     * Panel with components for creating a new project. This Panel will be somewhere below the project file listing...
     */
    private class NewProjectPanel extends JPanel {

        private final JTextField newProjectName;
        private final JComboBox newProjectType;
        private final JButton createNewProjectButton;
        private final JPanel flowPanel;
        private final ComponentFlasher newProjectNameFlasher;

        public NewProjectPanel() {
            super(new BorderLayout());

            newProjectName = new JTextField();
            newProjectType = new JComboBox(Project.Type.values());
            newProjectType.removeItem(Project.Type.CALIBRATION); // calibration projects are created from the File menu
            newProjectType.setSelectedItem(Project.Type.AF);
            newProjectType.setBackground(Color.WHITE);
            createNewProjectButton = new JButton("Create New");

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
}
