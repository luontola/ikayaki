/*
* MainViewPanel.java
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
import ikayaki.squid.Squid;

import javax.swing.*;

/**
 * Creates the main view panels (split panels) and Squid and Project components. It also tells everybody if the current
 * project is changed.
 *
 * @author
 */
public class MainViewPanel extends JPanel {

    private ProjectExplorerPanel projectExplorer;
    private CalibrationPanel calibration;

    /**
     * Front-end for controlling the SQUID. Only one project at a time may have access to the SQUID.
     */
    private Squid squid;

    /**
     * Currently opened project.
     */
    private Project project;

    /**
     * Project which has an ongoing measurement, or null if no measurement is running.
     */
    private Project measuringProject;

    private MainMenuBar menuBar;
    private MainStatusBar statusBar;
    private ProjectInformationPanel projectInformation;
    private MeasurementSequencePanel measurementSequence;
    private MeasurementControlsPanel measurementControls;
    private MeasurementDetailsPanel measurementDetails;
    private MeasurementGraphsPanel measurementGraphs;

    /**
     * Loads default view and creates all components and panels. Splitpanel between Calibration,Explorer,Information and
     * rest.
     */
    public MainViewPanel() {
        return; // TODO
    }

    /**
     * Looks for file with filename, if not exist creates new other wise opens it. Then updates current project and
     * tells Panels new project is opened.
     */
    public boolean changeProject(Project project) {
        return false; // TODO
    }
}