/*
* MainMenuBar.java
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
import java.awt.event.ActionEvent;

/**
 * Creates Menu items for Menubar and makes action listeners for them
 *
 * @author
 */
public class MainMenuBar extends JMenuBar {
/*
Event A: On newProject Clicked - Opens File chooser and opens new file in selected
folder
*/
/*
Event B: On openProject Clicked - Opens File chooser and opens selected file
*/
/*
Event C: On exportToDAT Clicked - Opens File chooser and tells Project to export in
selected file
*/
/*
Event D: On exportToTDT Clicked - Opens File chooser and tells Project to export in
selected file
*/
/*
Event E: On exportToSRM Clicked - Opens File chooser and tells Project to export in
selected file
*/
/*
Event F: On configuration Clicked - Opens SettingsDialog (frame)
*/
/*
Event G: On helpItem Clicked - Opens Help dialog (own frame?)
*/
/*
Event H: On about Clicked - Opens dialog with credits and version number
*/
/*
Event I: On exit Clicked - closes program
*/

    private MainViewPanel main;

    private JMenu file;
    private JMenu options;
    private JMenu help;
    private Action newProject;
    private Action openProject;
    private JMenu exportProject;
    private Action exportProjectToDAT;
    private Action exportProjectToDTD;
    private Action exportProjectToSRM;
    private Action exit;
    private Action configuration;
    private Action helpItem;
    private Action about;

    /**
     * Creates all components and makes menu and sets ActionListeners.
     */
    public MainMenuBar(MainViewPanel main) {
        this.main = main;

        add(new JMenu("Menu bar"));
        options = new JMenu("Options");
        add(options);
        configuration = new ItemAction("Configuration");
        options.add(configuration);

        return; // TODO
    }

    private class ItemAction extends AbstractAction {
        private String s;

        public ItemAction(String s) {
            this.s = s;
            putValue(Action.NAME, "" + s);
        }

        public void actionPerformed(ActionEvent e) {
            SettingsDialog.showSettingsDialog(main.getParentFrame(),
                    "Configuration");
        }
    }

}
