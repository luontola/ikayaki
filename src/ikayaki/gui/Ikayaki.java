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

package ikayaki.gui;

import javax.swing.*;

/**
 * Starts the program. Lays out MainViewPanel, MainMenuBar and MainStatusBar in a JFrame.
 *
 * @author
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
     * Starts the program with the provided command line parameters. If the location of a project file is given as a
     * parameter, the program will try to load it.
     *
     * @param args command line parameters.
     */
    public static void main(String[] args) {
        return; // TODO
    }
}