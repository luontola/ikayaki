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
import ikayaki.ProjectEvent;
import ikayaki.Settings;
import ikayaki.squid.Squid;

import javax.swing.*;

/**
 * Creates the main view panels (split panels) and Squid and Project components. It also tells everybody if the current
 * project is changed.
 *
 * @author Aki Korpua (?), Esko Luontola
 */
public class MainViewPanel extends ProjectComponent {

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

    private ProjectExplorerPanel projectExplorer;
    private CalibrationPanel calibration;

    private ProjectInformationPanel projectInformation;
    private MeasurementSequencePanel measurementSequence;
    private MeasurementControlsPanel measurementControls;
    private MeasurementDetailsPanel measurementDetails;
    private MeasurementGraphsPanel measurementGraphs;

    /**
     * Loads default view and creates all components and panels. Splitpanel between Calibration, Explorer, Information
     * and rest.
     *
     * @param project the project to be opened, or null to open the last known project.
     */
    public MainViewPanel(Project project) {

        /* init SQUID interface */
        squid = Squid.instance();

        /* init GUI components */
        menuBar = new MainMenuBar();
        statusBar = new MainStatusBar();

        projectExplorer = new ProjectExplorerPanel(this, project);
        calibration = new CalibrationPanel(this);

        projectInformation = new ProjectInformationPanel();
        measurementSequence = new MeasurementSequencePanel();
        measurementControls = new MeasurementControlsPanel();
        measurementDetails = new MeasurementDetailsPanel();
        measurementGraphs = new MeasurementGraphsPanel();

        setProject(project);

        return; // TODO
    }

    /**
     * Loads a new project to all GUI components. This method will be called by the Project Explorer and Calibration
     * panels.
     *
     * @param project the project to be opened, or null to close the previous one.
     */
    @Override public void setProject(Project project) {

        // close the previous project if it has no measurements running
        // (it will be closed by projectUpdated() if there is a measurement running)
        if (this.project != null && this.project != measuringProject) {
            if (Project.closeProject(this.project)) {
                this.project = null;
            } else {
                JOptionPane.showMessageDialog(this, "Unable to close the project " + this.project.getName(), "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // register the new project
        if (project != null) {
            project.addProjectListener(this);
            project.setSquid(squid);        // will do nothing if another project has a measurement running
        }
        this.project = project;
        projectExplorer.setProject(project);
        calibration.setProject(project);
        projectInformation.setProject(project);
        measurementSequence.setProject(project);
        measurementControls.setProject(project);
        measurementDetails.setProject(project);
        measurementGraphs.setProject(project);
    }

    /**
     * Keeps track of which project has a measurement running.
     */
    @Override public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.STATE_CHANGED) {

            // check if the last measurement has stopped
            if (measuringProject != null && measuringProject.getState() == Project.State.IDLE) {

                // close the project if it is not anymore open
                if (measuringProject != project) {
                    if (Project.closeProject(measuringProject)) {
                        measuringProject = null;
                        project.setSquid(squid);
                    } else {
                        JOptionPane.showMessageDialog(this, "Unable to close the project " + measuringProject.getName(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // check if a new measurement has started
            if (project != null && project.getState() != Project.State.IDLE) {
                measuringProject = project;
            }
        }
    }

    public MainMenuBar getMenuBar() {
        return menuBar;
    }

    public MainStatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Tries to exit the program. Will do nothing if a measurement is running. Saves all settings and project files
     * before exiting.
     */
    public void exitProgram() {
        if (measuringProject != null) {
            System.err.println("Can not exit: a measurement is running");
            return;
        }
        if (!Settings.instance().saveNow()) {
            System.err.println("Can not exit: unable to save settings");
            return;
        }
        if (project != null) {
            if (!Project.closeProject(project)) {
                System.err.println("Can not exit: unable to close project " + project.getName());
                return;
            }
        }
    }
}