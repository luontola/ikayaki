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

import javax.swing.*;
import javax.swing.table.TableModel;

/**
 * Creates a list of project files in directory. Handles loading selected projects and showing export popup menu
 * (ProjectExplorerPopupMenu). Inner class of ProjectExplorerPanel.
 *
 * @author
 */
public class ProjectExplorerTable extends JTable {
/*
Event A: On table click - call Project.loadProject(File) with clicked project file, call
MainViewPanel.changeProject(Project) with returned Project unless null, on which case
show error message and revert explorerTable selection to old project, if any.
*/
/*
Event B: On table mouse right-click - create a ProjectExplorerPopupMenu for rightclicked
project file.
*/
    
    /**
     * TableModel which handles data from files (in upper-class ProjectExplorerPanel). Unnamed inner class.
     */
    private TableModel projectExplorerTableModel;
}