/*
* ProjectExplorerPopupMenu.java
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
 * Shows popup menu with export choices: AF (.dat), Thellier (.tdt) and Thermal (.tdt), and for each, "to current
 * directory", "to disk drive A:" and "...", which opens a standard file chooser for selecting dir and file to export
 * to. Executes selected export command.
 *
 * @author
 */
public class ProjectExplorerPopupMenu extends JPopupMenu {
/*
Event A: On menu click - call project.exportToXXX(File) according to selected menu
item; if false is returned, show error message.
*/
}