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
import ikayaki.util.LastExecutor;

import javax.swing.*;
import java.io.File;
import java.util.Vector;

/**
 * Creates a history/autocomplete field (browserField) for choosing the project directory, a listing of project files in
 * that directory (explorerTable) and in that listing a line for creating new project, which has a textbox for project
 * name, an AF/TH ComboBox and a "Create new" button (createNewProjectButton) for actuating the creation. Also has a
 * right-click popup menu for exporting project files.
 *
 * @author
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
     * Text field for writing directory to change to. Autocomplete results appear to Combo Box’ popup window, scheduled
     * by LastExecutor. Directory history appears to the same popup window when the down-arrow right to text field is
     * clicked.
     */
    private JComboBox browserField;

    private JButton browseButton;

    private ProjectExplorerTable explorerTable;

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
    private Vector<File> files = new Vector<File>();

    /**
     * Creates all components, sets directory as the last open directory, initializes files with files from that
     * directory.
     */
    public ProjectExplorerPanel(Project project) {
        return; // TODO
    }

    /**
     * Call super.setProject(project), hilight selected project, or unhilight unselected project.
     */
    public void setProject(Project project) {
        return; // TODO
    }
}