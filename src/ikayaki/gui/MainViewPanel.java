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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;

/**
 * Creates the main view panels (split panels) and Squid and Project components. It also tells everybody if the current
 * project is changed.
 *
 * @author Esko Luontola
 */
public class MainViewPanel extends ProjectComponent {

    private static final int DIVIDER_DEFAULT_LOCATION = 300;

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

    /* GUI components */
    private MainMenuBar menuBar;
    private MainStatusBar statusBar;

    private JSplitPane splitPane;
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

        // if project is null, load the last open project
        if (project == null) {
            File[] projectHistory = Settings.instance().getProjectHistory();
            if (projectHistory.length > 0) {
                project = Project.loadProject(projectHistory[0]);
            }
        }

        /* Init SQUID interface */
        try {
            squid = Squid.instance();
        } catch (IOException e) {
            // TODO: what should be done now? give error message?
            e.printStackTrace();
        }

        /* Init GUI components */
        menuBar = new MainMenuBar(this);
        statusBar = new MainStatusBar();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        projectExplorer = new ProjectExplorerPanel(this, project);
        calibration = new CalibrationPanel(this);

        projectInformation = new ProjectInformationPanel();
        measurementSequence = new MeasurementSequencePanel();
        measurementControls = new MeasurementControlsPanel();
        measurementDetails = new MeasurementDetailsPanel();
        measurementGraphs = new MeasurementGraphsPanel();

        /* Lay out GUI components */
        final JPanel left = new JPanel(new GridBagLayout());
        final JPanel right = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;

        // build left tab
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        calibration.setBorder(BorderFactory.createTitledBorder("Calibration"));
        left.add(calibration, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        projectExplorer.setBorder(BorderFactory.createTitledBorder("Project Explorer"));
        left.add(projectExplorer, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        projectInformation.setBorder(BorderFactory.createTitledBorder("Project Information"));
        left.add(projectInformation, gc);

        // build right tab
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        measurementSequence.setBorder(BorderFactory.createTitledBorder("Sequence"));
        right.add(measurementSequence, gc);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 0.0;
        gc.weighty = 1.0;
        measurementControls.setBorder(BorderFactory.createTitledBorder("Controls"));
        right.add(measurementControls, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        measurementDetails.setBorder(BorderFactory.createTitledBorder("Details"));
        right.add(measurementDetails, gc);
        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = 0.0;
        gc.weighty = 0.0;
        measurementGraphs.setBorder(BorderFactory.createTitledBorder("Graphs"));
        right.add(measurementGraphs, gc);

        // configure tabs
        splitPane.setLeftComponent(left);
        splitPane.setRightComponent(right);
        //splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        //splitPane.setDividerLocation(Math.max(DIVIDER_DEFAULT_LOCATION, left.getPreferredSize().width));
        splitPane.setDividerLocation(DIVIDER_DEFAULT_LOCATION);
        splitPane.setResizeWeight(0.0);
        //splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0);

        // prevent the left tab from being resized when the window is resized
        Dimension d = left.getMinimumSize();
        d.width = DIVIDER_DEFAULT_LOCATION / 2;
        left.setMinimumSize(d);

        // button for hiding the tabs
        Box tabControls = new Box(BoxLayout.Y_AXIS);
        final Icon tabButtonDown = new ImageIcon(ClassLoader.getSystemResource("resources/projectExplorerTabDown.png"));
        final Icon tabButtonUp = new ImageIcon(ClassLoader.getSystemResource("resources/projectExplorerTabUp.png"));
        final JButton tabButton = new JButton(tabButtonDown);
        tabButton.setContentAreaFilled(false);
        tabButton.setBorder(null);
        tabButton.setFocusable(false);
        tabButton.setMnemonic('P');
        tabButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (splitPane.getDividerLocation() == 0) {
                    // show tab
                    splitPane.setDividerLocation(splitPane.getLastDividerLocation());
                    tabButton.setIcon(tabButtonDown);
                } else {
                    // hide tab
                    splitPane.setDividerLocation(0);
                    tabButton.setIcon(tabButtonUp);
                }
            }
        });
        tabControls.add(tabButton);
        tabControls.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        // master layout
        setLayout(new BorderLayout());
        add(splitPane, "Center");
        add(tabControls, "West");
        setBackground(new Color(247, 243, 239));

        /* Finalize */
        setProject(project);
    }

    public MainMenuBar getMenuBar() {
        return menuBar;
    }

    public MainStatusBar getStatusBar() {
        return statusBar;
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
            Settings.instance().updateProjectHistory(project.getFile());
            Settings.instance().updateDirectoryHistory(project.getFile().getAbsoluteFile().getParentFile());

            project.addProjectListener(this);
            project.setSquid(squid);        // will do nothing if another project has a measurement running
            projectInformation.setBorder(
                    BorderFactory.createTitledBorder(project.getName() + " (" + project.getType() + " Project)"));
        } else {
            projectInformation.setBorder(BorderFactory.createTitledBorder("Project Information"));
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
     * Deprecates a method from the super class.
     *
     * @return null
     * @deprecated access the project variable directly.
     */
    @Deprecated @Override public Project getProject() {
        return null;
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
        System.exit(0);
    }
}
