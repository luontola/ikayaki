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
import java.awt.event.KeyEvent;

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
Event G: On help Clicked - Opens Help dialog (own frame?)
*/
/*
Event H: On about Clicked - Opens dialog with credits and version number
*/
/*
Event I: On exit Clicked - closes program
*/

    private MainViewPanel main;

    private JMenu fileMenu;
    private Action newProject;
    private Action openProject;
    private JMenu exportProjectMenu;
    private Action exportProjectToDAT;
    private Action exportProjectToDTD;
    private Action exportProjectToSRM;
    private Action exit;

    private JMenu optionsMenu;
    private Action configuration;

    private JMenu helpMenu;
    private Action help;
    private Action about;

    /**
     * Creates all components and makes menu and sets ActionListeners.
     */
    public MainMenuBar(MainViewPanel main) {
        this.main = main;
        initialize();

        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(newProject);
        fileMenu.add(openProject);
        {
            exportProjectMenu = new JMenu("Export");
            exportProjectMenu.setMnemonic(KeyEvent.VK_E);
            exportProjectMenu.add(exportProjectToDAT);
            exportProjectMenu.add(exportProjectToDTD);
            exportProjectMenu.add(exportProjectToSRM);
        }
        fileMenu.add(exportProjectMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        add(fileMenu);

        optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        optionsMenu.add(configuration);
        add(optionsMenu);

        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(help);
        helpMenu.add(about);
        add(helpMenu);
    }

    private void initialize() {
        /* FILE MENU ITEMS */
        newProject = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        newProject.putValue(Action.NAME, "New...");
        newProject.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);

        openProject = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        openProject.putValue(Action.NAME, "Open...");
        openProject.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);

        // File > Export Project menu items
        exportProjectToDAT = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        exportProjectToDAT.putValue(Action.NAME, "DAT File...");
        exportProjectToDAT.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);

        exportProjectToDTD = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        exportProjectToDTD.putValue(Action.NAME, "DTD File...");
        exportProjectToDTD.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);

        exportProjectToSRM = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        exportProjectToSRM.putValue(Action.NAME, "SRM File...");
        exportProjectToSRM.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);

        exit = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        exit.putValue(Action.NAME, "Exit");
        exit.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);

        /* OPTIONS MENU ITEMS */
        configuration = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                SettingsDialog.showSettingsDialog(main.getParentFrame(), "Configuration");
            }
        };
        configuration.putValue(Action.NAME, "Configuration");
        configuration.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);

        /* HELP MENU ITEMS */
        help = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        help.putValue(Action.NAME, "Help Topics");
        help.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);

        about = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // TODO
            }
        };
        about.putValue(Action.NAME, "About");
        about.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    }

    private class ItemAction extends AbstractAction {
        private String s;

        public ItemAction(String s) {
            this.s = s;
            putValue(Action.NAME, "" + s);
        }

        public void actionPerformed(ActionEvent e) {

        }
    }

}
