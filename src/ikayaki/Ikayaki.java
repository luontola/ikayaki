/*
* Ikayaki.java
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

package ikayaki;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyBlue;
import ikayaki.gui.MainViewPanel;
import jutil.JUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Starts the program. Lays out MainViewPanel, MainMenuBar and MainStatusBar in a JFrame.
 *
 * @author Esko Luontola
 */
public class Ikayaki extends JFrame {
/*
Event A: On window close - checks that no measurement is running. Saves all opened
project files and settings. Closes the program, or notifies the user if the program may not
be closed.
*/

    /* Application information */
    public static final String APP_NAME = "Ikayaki";
    public static final String APP_VERSION = "0.1 CVS";
    public static final String APP_HOME_PAGE = "http://www.cs.helsinki.fi/group/squid/";
    public static final String FILE_TYPE = ".ika";

    /**
     * Starts the program Ikayaki.
     *
     * @param project a project to be opened when the program starts, or null to open no project.
     * @throws HeadlessException if GraphicsEnvironment.isHeadless() returns true.
     */
    public Ikayaki(Project project) throws HeadlessException {
        super(APP_NAME + " " + APP_VERSION);


        PlasticLookAndFeel.setMyCurrentTheme(new SkyBlue());
        try {
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final MainViewPanel main = new MainViewPanel(project);

        setLayout(new BorderLayout());
        setJMenuBar(main.getMenuBar());
        add(main, "Center");
        add(main.getStatusBar(), "South");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        pack();

        // restore size and position
        setSize(Settings.instance().getWindowWidth(), Settings.instance().getWindowHeight());
        setVisible(true);

        if (Settings.instance().getWindowMaximized() && System.getProperty("os.name").startsWith("Windows")) {
            // native code for maximizing the window
            try {
                int hwnd = JUtil.getHwnd(getTitle());
                JUtil.setWindowMaximized(hwnd);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            setLocationByPlatform(true);
        }

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
                    Settings.instance().setWindowWidth(getWidth());
                    Settings.instance().setWindowHeight(getHeight());
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if ((getExtendedState() & MAXIMIZED_BOTH) != 0) {
                    Settings.instance().setWindowMaximized(true);
                } else {
                    Settings.instance().setWindowMaximized(false);
                }
                main.exitProgram();
            }
        });
    }

    /**
     * Starts the program with the provided command line parameters. If the location of a project file is given as a
     * parameter, the program will try to load it.
     *
     * @param args command line parameters.
     */
    public static void main(String[] args) {
        Project project = null;
        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists() && file.isFile()) {
                project = Project.loadProject(file);
            }
        }
        new Ikayaki(project);
    }
}