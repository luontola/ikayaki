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

import ikayaki.Settings;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Creates Menu items for Menubar and makes action listeners for them.
 *
 * @author Esko Luontola
 */
public class MainMenuBar extends JMenuBar {

    private MainViewPanel main;

    private JMenu fileMenu;
    private Action newProject;
    private Action openProject;
    private JMenu openRecentProjectMenu;
    private JMenu exportProjectMenu;
    private Action exportProjectToDAT;
    private Action exportProjectToDTD;
    private Action exportProjectToSRM;
    private Action exit;
    private Action print;

    private JMenu measurementMenu;
    private Action autoStep;
    private Action singleStep;
    private Action pause;
    private Action abort;

    private JMenu toolsMenu;
    private Action programSettings;
    private Action deviceSettings;

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
            openRecentProjectMenu = new JMenu("Open Recent");
            openRecentProjectMenu.setMnemonic(KeyEvent.VK_R);
        }
        fileMenu.add(openRecentProjectMenu);
        fileMenu.add(new JSeparator());
        {
            exportProjectMenu = new JMenu("Export");
            exportProjectMenu.setMnemonic(KeyEvent.VK_E);
            exportProjectMenu.add(exportProjectToDAT);
            exportProjectMenu.add(exportProjectToDTD);
            exportProjectMenu.add(exportProjectToSRM);
        }
        fileMenu.add(exportProjectMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(print);
        fileMenu.add(new JSeparator());
        fileMenu.add(exit);
        add(fileMenu);

        measurementMenu = new JMenu("Measurement");
        measurementMenu.setMnemonic(KeyEvent.VK_M);
        measurementMenu.add(autoStep);
        measurementMenu.add(singleStep);
        measurementMenu.add(pause);
        measurementMenu.add(abort);
        add(measurementMenu);

        toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        toolsMenu.add(programSettings);
        toolsMenu.add(deviceSettings);
        add(toolsMenu);

        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(help);
        helpMenu.add(about);
        add(helpMenu);

        // rebuilding of the file menu
        fileMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {

                // enable/disable export menu
                exportProjectMenu.setEnabled(exportProjectToDAT.isEnabled()
                        || exportProjectToDTD.isEnabled()
                        || exportProjectToSRM.isEnabled());

                // rebuild project history
                openRecentProjectMenu.removeAll();
                File[] files = Settings.getProjectHistory();
                if (files.length == 0) {
                    openRecentProjectMenu.setEnabled(false);
                } else {
                    openRecentProjectMenu.setEnabled(true);

                    for (int i = 0; i < files.length; i++) {
                        final File file = files[i];
                        JMenuItem item;
                        if (i < 10) {
                            int j = (i + 1) % 10;
                            item = new JMenuItem(j + ":   " + file.getAbsolutePath());
                            item.setMnemonic(KeyEvent.VK_0 + j);
                        } else {
                            item = new JMenuItem(file.getAbsolutePath());
                        }

                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                MainMenuBar.this.main.loadProject(file);
                            }
                        });
                        openRecentProjectMenu.add(item);
                    }
                }
            }

            public void menuDeselected(MenuEvent e) {
                // DO NOTHING
            }

            public void menuCanceled(MenuEvent e) {
                // DO NOTHING
            }
        });
    }

    /**
     * Initializes the private action fields of the class.
     */
    private void initialize() {

        /* File Menu */
        newProject = main.getNewProjectAction();
        openProject = main.getOpenProjectAction();
        exportProjectToDAT = main.getExportProjectToDATAction();
        exportProjectToDTD = main.getExportProjectToTDTAction();
        exportProjectToSRM = main.getExportProjectToSRMAction();
        print = main.getPrintAction();
        exit = main.getExitAction();

        /* Measurement Menu */
        autoStep = main.getMeasurementControlsPanel().getAutoStepAction();
        singleStep = main.getMeasurementControlsPanel().getSingleStepAction();
        pause = main.getMeasurementControlsPanel().getPauseAction();
        abort = main.getMeasurementControlsPanel().getAbortAction();

        /* Tools Menu */
        programSettings = main.getProgramSettingsAction();
        deviceSettings = main.getDeviceSettingsAction();

        /* Help Menu */
        help = main.getHelpAction();
        about = main.getAboutAction();
    }
}
