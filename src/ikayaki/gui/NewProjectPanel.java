/*
* NewProjectPanel.java
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

/**
 * Panel with components for creating a new project. This Panel will be somewhere below the project file listing...
 *
 * @author
 */
public class NewProjectPanel extends JPanel {
/*
Event A: On createNewProjectButton click - call Project.createXXXProject(File) with
filename from newProjectField; if returns null, show error message and do nothing. Otherwise,
update file listing, set new project active, tell explorerTable to reset newProjectField and
newProjectType and call MainViewPanel.changeProject(Project) with returned Project.
*/

    private JTextField newProject;

    private JComboBox newProjectType; // = AF / Thellier / Thermal

    private JButton createNewProjectButton;

}