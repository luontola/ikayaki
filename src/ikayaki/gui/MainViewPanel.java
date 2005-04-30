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

import ikayaki.Ikayaki;
import ikayaki.Project;
import ikayaki.ProjectEvent;
import ikayaki.Settings;
import ikayaki.squid.SerialIO;
import ikayaki.squid.Squid;
import ikayaki.util.ComponentPrinter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
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
    private static final int DIVIDER_SIZE = 5;

    /**
     * Front-end for controlling the SQUID. Only one project at a time may have access to the SQUID.
     */
    private Squid squid;

    /**
     * Currently opened project, or null of no project is open.
     */
    private Project project = null;

    /**
     * Project which has had the latest measurement, or null if no measurements have been made..
     */
    private Project latestMeasuringProject = null;

    /* GUI Components */
    private MainMenuBar menuBar;
    private MainStatusBar statusBar;

    private JSplitPane splitPane;
    private ProjectExplorerPanel projectExplorerPanel;
    private CalibrationPanel calibrationPanel;

    private ProjectInformationPanel projectInformationPanel;
    private MeasurementSequencePanel measurementSequencePanel;
    private MeasurementControlsPanel measurementControlsPanel;
    private MeasurementDetailsPanel measurementDetailsPanel;
    private MeasurementGraphsPanel measurementGraphsPanel;

    /* Swing Actions */
    private Action newProjectAction;
    private Action openProjectAction;
    private Action exportProjectToDATAction;
    private Action exportProjectToDTDAction;
    private Action exportProjectToSRMAction;
    private Action printAction;
    private Action printPreviewAction;
    private Action exitAction;
    private Action programSettingsAction;
    private Action deviceSettingsAction;
    private Action helpAction;
    private Action aboutAction;

    /**
     * Loads default view and creates all components and panels. Splitpanel between Calibration, Explorer, Information
     * and rest.
     *
     * @param project the project to be opened, or null to open the last known project.
     */
    public MainViewPanel(Project project) {

        // if project is null, load the last open project
        if (project == null) {
            File[] projectHistory = Settings.getProjectHistory();
            if (projectHistory.length > 0) {
                project = Project.loadProject(projectHistory[0]);
            }
        }

        /* Init SQUID interface */
        new Thread() {
            @Override public void run() {
                try {
                    final Squid squid = Squid.instance();       // might take a long time
                    if (!squid.isOK()) {
                        JOptionPane.showMessageDialog(getParentFrame(),
                                "SQUID is not OK!", "Squid error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            setSquid(squid);
                        }
                    });

                } catch (IOException e) {
                    // TODO: what should be done now? give error message?
                    //e.printStackTrace();
                    System.err.println("Unable to initialize the SQUID interface.");
                }
            }
        }.start();

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
        left.add(getCalibrationPanel(), gc);
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        left.add(getProjectExplorerPanel(), gc);
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        left.add(getProjectInformationPanel(), gc);

        // build right tab
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        right.add(getMeasurementSequencePanel(), gc);
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 0.0;
        gc.weighty = 1.0;
        right.add(getMeasurementControlsPanel(), gc);
        if (false) {
            gc.gridx = 0;
            gc.gridy = 1;
            gc.weightx = 1.0;
            gc.weighty = 0.0;
            right.add(getMeasurementDetailsPanel(), gc);
            gc.gridx = 1;
            gc.gridy = 1;
            gc.weightx = 0.0;
            gc.weighty = 0.0;
            right.add(getMeasurementGraphsPanel(), gc);
        } else {
            // more space for the graphs
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.add(getMeasurementDetailsPanel(), BorderLayout.WEST);
            bottom.add(getMeasurementGraphsPanel(), BorderLayout.CENTER);
            gc.gridx = 0;
            gc.gridy = 1;
            gc.gridwidth = 2;
            gc.weightx = 1.0;
            gc.weighty = 0.0;
            right.add(bottom, gc);
        }

        // configure tabs
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(left);
        splitPane.setRightComponent(right);
        //splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        //splitPane.setDividerLocation(Math.max(DIVIDER_DEFAULT_LOCATION, left.getPreferredSize().width));
        splitPane.setDividerLocation(DIVIDER_DEFAULT_LOCATION);
        splitPane.setResizeWeight(0.0);
        //splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(DIVIDER_SIZE);

        // prevent the left tab from being resized when the window is resized
        Dimension d = left.getMinimumSize();
        d.width = DIVIDER_DEFAULT_LOCATION / 2;
        left.setMinimumSize(d);

        // button for hiding the tabs
        // TODO: make this as an Action?
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
                    splitPane.setDividerSize(DIVIDER_SIZE);
                    tabButton.setIcon(tabButtonDown);
                } else {
                    // hide tab
                    splitPane.setDividerLocation(0);
                    splitPane.setDividerSize(0);
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

//        // TODO: testing... initialize with no project
//        setProject(null);

        /* Initialize everything with the loaded project or null */
        setProject(project);
    }

    /**
     * Returns the Squid instance used for communicating with the hardware, or null if the connection has not yet been
     * fully initialized.
     */
    public Squid getSquid() {
        return squid;
    }

    /**
     * Sets the fully initialized Squid interface for the use of the program. Sets the active project the owner of the
     * squid by re-setting the active project.
     *
     * @param squid an instance of the Squid.
     * @throws NullPointerException  if squid is null.
     * @throws IllegalStateException if the squid has already been set.
     */
    public void setSquid(Squid squid) {
        if (squid == null) {
            throw new NullPointerException();
        }
        if (this.squid != null) {
            throw new IllegalStateException();
        }
        this.squid = squid;
        setProject(getProject());
    }

    /**
     * Returns the active project, or null if no project is active.
     */
    @Override public Project getProject() {
        return project;
    }

    /**
     * Loads a new project to all GUI components. This method will be called by the Project Explorer and Calibration
     * panels. It is possible to reopen the same project, in which case all GUI components will as well be updated.
     *
     * @param project the project to be opened, or null to close the previous one.
     */
    @Override public void setProject(Project project) {
        if (project != null) {
            // update history logs
            if (project.getType() != Project.Type.CALIBRATION) {
                Settings.updateProjectHistory(project.getFile());
                Settings.updateDirectoryHistory(project.getFile().getAbsoluteFile().getParentFile());
            }

            // register the new project
            project.addProjectListener(this);
            project.setSquid(squid);        // will do nothing if another project has a measurement running

            // update GUI components
            Frame parent = getParentFrame();
            if (parent != null) {
                parent.setTitle(project.getFile().getAbsolutePath());
            } else {
                // set the title after the program has fully started
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        getParentFrame().setTitle(getProject().getFile().getAbsolutePath());
                    }
                });
            }
            getProjectInformationPanel().setBorder(
                    BorderFactory.createTitledBorder(project.getName() + " (" + project.getType() + " Project)"));
            getExportProjectToDATAction().setEnabled(true);
            getExportProjectToTDTAction().setEnabled(true);
            getExportProjectToSRMAction().setEnabled(true);
            getPrintAction().setEnabled(true);
            getPrintPreviewAction().setEnabled(true);
        } else {
            // no project open, update GUI components
            Frame parent = getParentFrame();
            if (parent != null) {
                getParentFrame().setTitle(null);
            }
            getProjectInformationPanel().setBorder(BorderFactory.createTitledBorder("Project Information"));
            getExportProjectToDATAction().setEnabled(false);
            getExportProjectToTDTAction().setEnabled(false);
            getExportProjectToSRMAction().setEnabled(false);
            getPrintAction().setEnabled(false);
            getPrintPreviewAction().setEnabled(false);
        }

        // switch to the new project
        Project oldProject = this.project;
        this.project = project;

        getProjectExplorerPanel().setProject(project);
        getCalibrationPanel().setProject(project);
        getProjectInformationPanel().setProject(project);
        getMeasurementSequencePanel().setProject(project);
        getMeasurementControlsPanel().setProject(project);
        getMeasurementDetailsPanel().setProject(project);
        getMeasurementGraphsPanel().setProject(project);

        // close the previous project if it is not the latest measuring project
        if (oldProject != null && oldProject != project && oldProject != latestMeasuringProject) {
            if (!Project.closeProject(oldProject)) {
                JOptionPane.showMessageDialog(getParentFrame(),
                        "Unable to close the project " + oldProject.getName(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Keeps track of which project has a measurement running.
     */
    @Override public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.STATE_CHANGED) {

            // keep track of which project has a measurement running
            if (event.getProject().getState() != Project.State.IDLE) {

                // close the previous measuring project if it is no more open
                if (latestMeasuringProject != null
                        && latestMeasuringProject != project
                        && latestMeasuringProject != event.getProject()) {
                    if (!Project.closeProject(latestMeasuringProject)) {
                        JOptionPane.showMessageDialog(getParentFrame(),
                                "Unable to close the project " + latestMeasuringProject.getName(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                latestMeasuringProject = event.getProject();
            }
            return;
        }
    }

    /**
     * Tries to exit the program. Will do nothing if a measurement is running. Saves all settings and project files
     * before exiting.
     */
    public void exitProgram() {

        // must not exit if a measurement is running
        if (latestMeasuringProject != null && latestMeasuringProject.getState() != Project.State.IDLE) {
            JOptionPane.showMessageDialog(getParentFrame(),
                    "Can not exit. A measurement is running.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ensure that all settings will be saved
        if (!Settings.saveNow()) {
            JOptionPane.showMessageDialog(getParentFrame(),
                    "Can not exit. Unable to save the settings.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // close the active project
        setProject(null);

        // close the latest measuring project (if same as the active project, it was not close by setProject(null))
        if (latestMeasuringProject != null) {
            if (!Project.closeProject(latestMeasuringProject)) {
                JOptionPane.showMessageDialog(getParentFrame(),
                        "Can not exit. Unable to close the project " + latestMeasuringProject.getName() + ".",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // close all projects in the cache
        Project[] cachedProjects = Project.getCachedProjects();
        for (Project cached : cachedProjects) {
            System.err.println("Found a cached project, closing it: " + cached.getFile());
            if (!Project.closeProject(cached)) {
                JOptionPane.showMessageDialog(getParentFrame(),
                        "Can not exit. Unable to close the (cached) project " + cached.getName() + ".",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // close serial port connections
        SerialIO.closeAllPorts();

        // all preparations successful, exit the program
        System.exit(0);
    }

    /**
     * Loads a project file and tries to set it as the active project. Will show an error dialog if operation failed.
     *
     * @param file the project file to be loaded.
     * @throws NullPointerException if file is null.
     */
    public void loadProject(File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        Project project = Project.loadProject(file);
        if (project != null) {
            setProject(project);
        } else {
            JOptionPane.showMessageDialog(getParentFrame(),
                    "Unable to open the file " + file,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates a project file and tries to set it as the active project. Will show an error dialog if operation failed.
     *
     * @param file the project file to be created.
     * @param type the type of the project.
     * @throws NullPointerException if file or type is null.
     */
    public void createProject(File file, Project.Type type) {
        if (file == null || type == null) {
            throw new NullPointerException();
        }
        Project project = Project.createProject(file, type);
        if (project != null) {
            setProject(project);
        } else {
            JOptionPane.showMessageDialog(getParentFrame(),
                    "Unable to create the file " + file,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a file chooser and exports the project to a different file format.
     *
     * @param project the project to be exported.
     * @param type    the type of file to export from the current project.
     * @param output  the file to write to, or null to open a file chooser.
     * @throws NullPointerException     if type or the current project is null.
     * @throws IllegalArgumentException if type is not "dat", "tdt" or "srm".
     */
    public void exportProject(Project project, String type, File output) {
        if (project == null) {
            throw new NullPointerException();
        }
        type = type.toLowerCase();
        JFileChooser chooser = new JFileChooser(Settings.getLastDirectory());
        chooser.setFileFilter(new GenericFileFilter(type.toUpperCase() + " File", type));

        do {
            // open a file dialog (or emulate it, if an output file has been specified)
            int returnVal;
            if (output == null) {
                returnVal = chooser.showSaveDialog(MainViewPanel.this);
            } else {
                chooser.setSelectedFile(output);
                returnVal = JFileChooser.APPROVE_OPTION;
                output = null;
            }

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                if (!file.getName().toLowerCase().endsWith("." + type)) {
                    file = new File(file.getAbsolutePath() + "." + type);
                }

                // overwrite old file?
                if (file.exists()) {
                    returnVal = JOptionPane.showConfirmDialog(getParentFrame(),
                            "Overwrite the file " + file + "?",
                            "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (returnVal == JOptionPane.NO_OPTION) {
                        continue; // retry
                    } else if (returnVal == JOptionPane.CANCEL_OPTION) {
                        break; // cancel
                    }
                }

                // write new file
                boolean ok;
                if (type.equals("dat")) {
                    ok = project.exportToDAT(chooser.getSelectedFile());
                } else if (type.equals("tdt")) {
                    ok = project.exportToTDT(chooser.getSelectedFile());
                } else if (type.equals("srm")) {
                    ok = project.exportToSRM(chooser.getSelectedFile());
                } else {
                    throw new IllegalArgumentException("Unkown export type: " + type);
                }
                if (!ok) {
                    JOptionPane.showMessageDialog(getParentFrame(),
                            "Unable to write the file " + file,
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            break;
        } while (true);
    }

    /* Getters for GUI Components */

    public MainMenuBar getMenuBar() {
        if (menuBar == null) {
            menuBar = new MainMenuBar(this);
        }
        return menuBar;
    }

    public MainStatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new MainStatusBar();
        }
        return statusBar;
    }

    public MeasurementGraphsPanel getMeasurementGraphsPanel() {
        if (measurementGraphsPanel == null) {
            measurementGraphsPanel = new MeasurementGraphsPanel();
            measurementGraphsPanel.setBorder(BorderFactory.createTitledBorder("Graphs"));
        }
        return measurementGraphsPanel;
    }

    public MeasurementDetailsPanel getMeasurementDetailsPanel() {
        if (measurementDetailsPanel == null) {
            measurementDetailsPanel = getMeasurementSequencePanel().getDetailsPanel();
            measurementDetailsPanel.setBorder(BorderFactory.createTitledBorder("Details"));
        }
        return measurementDetailsPanel;
    }

    public MeasurementControlsPanel getMeasurementControlsPanel() {
        if (measurementControlsPanel == null) {
            measurementControlsPanel = new MeasurementControlsPanel();
            measurementControlsPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        }
        return measurementControlsPanel;
    }

    public MeasurementSequencePanel getMeasurementSequencePanel() {
        if (measurementSequencePanel == null) {
            measurementSequencePanel = new MeasurementSequencePanel();
            measurementSequencePanel.setBorder(BorderFactory.createTitledBorder("Sequence"));
        }
        return measurementSequencePanel;
    }

    public ProjectInformationPanel getProjectInformationPanel() {
        if (projectInformationPanel == null) {
            projectInformationPanel = new ProjectInformationPanel();
            projectInformationPanel.setBorder(BorderFactory.createTitledBorder("Project Information"));
        }
        return projectInformationPanel;
    }

    public CalibrationPanel getCalibrationPanel() {
        if (calibrationPanel == null) {
            calibrationPanel = new CalibrationPanel(this);
            calibrationPanel.setBorder(BorderFactory.createTitledBorder("Calibration"));
        }
        return calibrationPanel;
    }

    public ProjectExplorerPanel getProjectExplorerPanel() {
        if (projectExplorerPanel == null) {
            projectExplorerPanel = new ProjectExplorerPanel(this, project);
            projectExplorerPanel.setBorder(BorderFactory.createTitledBorder("Project Explorer"));
        }
        return projectExplorerPanel;
    }

    /* Getters for Swing Actions */

    public Action getNewProjectAction() {
        if (newProjectAction == null) {
            newProjectAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    NewProjectFileChooser chooser = new NewProjectFileChooser(Settings.getLastDirectory());
                    chooser.setFileFilter(new GenericFileFilter(Ikayaki.FILE_DESCRIPTION, Ikayaki.FILE_TYPE));
                    int returnVal = chooser.showSaveDialog(MainViewPanel.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        Project.Type type = chooser.getProjectType();

                        if (!file.getName().toLowerCase().endsWith(Ikayaki.FILE_TYPE)) {
                            file = new File(file.getAbsolutePath() + Ikayaki.FILE_TYPE);
                        }
                        createProject(file, type);
                    }
                }
            };
            newProjectAction.putValue(Action.NAME, "New...");
            newProjectAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
            newProjectAction.putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        }
        return newProjectAction;
    }

    public Action getOpenProjectAction() {
        if (openProjectAction == null) {
            openProjectAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser(Settings.getLastDirectory());
                    chooser.setFileFilter(new GenericFileFilter(Ikayaki.FILE_DESCRIPTION, Ikayaki.FILE_TYPE));
                    int returnVal = chooser.showOpenDialog(MainViewPanel.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        loadProject(chooser.getSelectedFile());
                    }
                }
            };
            openProjectAction.putValue(Action.NAME, "Open...");
            openProjectAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
            openProjectAction.putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        }
        return openProjectAction;
    }

    public Action getExportProjectToDATAction() {
        if (exportProjectToDATAction == null) {
            exportProjectToDATAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    exportProject(getProject(), "dat", null);
                }
            };
            exportProjectToDATAction.putValue(Action.NAME, "DAT File...");
            exportProjectToDATAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
        }
        return exportProjectToDATAction;
    }

    public Action getExportProjectToTDTAction() {
        if (exportProjectToDTDAction == null) {
            exportProjectToDTDAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    exportProject(getProject(), "tdt", null);
                }
            };
            exportProjectToDTDAction.putValue(Action.NAME, "TDT File...");
            exportProjectToDTDAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
        }
        return exportProjectToDTDAction;
    }

    public Action getExportProjectToSRMAction() {
        if (exportProjectToSRMAction == null) {
            exportProjectToSRMAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    exportProject(getProject(), "srm", null);
                }
            };
            exportProjectToSRMAction.putValue(Action.NAME, "SRM File...");
            exportProjectToSRMAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);

        }
        return exportProjectToSRMAction;
    }

    public Action getPrintAction() {
        if (printAction == null) {
            printAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    ComponentPrinter.printComponent(new PrintPanel(null, getProject()).getPrintedDocument());
                }
            };
            printAction.putValue(Action.NAME, "Print");
            printAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
            printAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        }
        return printAction;
    }

    public Action getPrintPreviewAction() {
        if (printPreviewAction == null) {
            printPreviewAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    SettingsDialog.showPrintPreview(getParentFrame(), "Print Preview", getProject());
                }
            };
            printPreviewAction.putValue(Action.NAME, "Print Preview");
            printPreviewAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
        }
        return printPreviewAction;
    }


    public Action getExitAction() {
        if (exitAction == null) {
            exitAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    exitProgram();
                }
            };
            exitAction.putValue(Action.NAME, "Exit");
            exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
            exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
        }
        return exitAction;
    }

    public Action getProgramSettingsAction() {
        if (programSettingsAction == null) {
            programSettingsAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    SettingsDialog.showProgramSettingsDialog(getParentFrame(), "Options");
                    setProject(getProject());   // sequence columns might have changed
                }
            };
            programSettingsAction.putValue(Action.NAME, "Options");
            programSettingsAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
        }
        return programSettingsAction;
    }

    public Action getDeviceSettingsAction() {
        if (deviceSettingsAction == null) {
            deviceSettingsAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    SettingsDialog.showDeviceSettingsDialog(getParentFrame(), "Device Configuration");
                    getMeasurementControlsPanel().measurementUpdated(null);     // handler positions might have changed
                }
            };
            deviceSettingsAction.putValue(Action.NAME, "Device Configuration");
            deviceSettingsAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        }
        return deviceSettingsAction;
    }

    public Action getHelpAction() {
        if (helpAction == null) {
            helpAction = new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    if (!System.getProperty("os.name").startsWith("Windows")) {
                        JOptionPane.showMessageDialog(getParentFrame(),
                                "Open this file in your web browser to view the help pages:\n" + Ikayaki.HELP_PAGES);
                        return;
                    }

                    // open the help pages in a web browser
                    String[] cmd = {"cmd", "/c", "start", Ikayaki.HELP_PAGES};
                    //String[] cmd = {"cmd", "/c", "start", "http://www.cs.helsinki.fi/group/squid/"};
                    //String[] cmd = {"cmd.exe", "/c", "dir"};
                    try {
                        Process p = Runtime.getRuntime().exec(cmd);
                        BufferedInputStream in = new BufferedInputStream(p.getInputStream());

                        // print what the process writes to stdout
                        int i;
                        while ((i = in.read()) >= 0) {
                            System.err.print((char) i);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            helpAction.putValue(Action.NAME, "Help Topics");
            helpAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
            helpAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        }
        return helpAction;
    }

    public Action getAboutAction() {
        if (aboutAction == null) {
            aboutAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    String message = Ikayaki.APP_NAME + " " + Ikayaki.APP_VERSION + "\n" +
                            Ikayaki.APP_BUILD + "\n\n" +
                            Ikayaki.APP_HOME_PAGE + "\n\n";
                    for (String author : Ikayaki.AUTHORS) {
                        message += author + "\n";
                    }
                    JOptionPane.showMessageDialog(getParentFrame(),
                            message, "About " + Ikayaki.APP_NAME, JOptionPane.INFORMATION_MESSAGE,
                            new ImageIcon(ClassLoader.getSystemResource("resources/ikayaki.png")));
                }
            };
            aboutAction.putValue(Action.NAME, "About");
            aboutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
        }
        return aboutAction;
    }

    /**
     * Customized JFileChooser for the use of getNewProjectAction(). Has controls for selecting the project's type.
     *
     * @author Esko Luontola
     */
    private class NewProjectFileChooser extends JFileChooser {

        private JComboBox projectType;

        public NewProjectFileChooser(File currentDirectory) {
            super(currentDirectory);
            projectType = new JComboBox(Project.Type.values());
            projectType.setSelectedItem(Project.Type.AF);
        }

        protected JDialog createDialog(Component parent) throws HeadlessException {
            JDialog dialog = super.createDialog(parent);
            Container origCP = dialog.getContentPane();
            JPanel newCP = new JPanel(new BorderLayout(0, 0));

            newCP.add(origCP, "Center");
            newCP.add(createExtraButtons(), "South");
            dialog.setContentPane(newCP);

            Dimension d = dialog.getSize();
            dialog.setSize((int) d.getWidth(), (int) d.getHeight() + 70);

            return dialog;
        }

        private Component createExtraButtons() {
            Box b = new Box(BoxLayout.X_AXIS);
            b.setBorder(BorderFactory.createEmptyBorder(0, 12, 11, 11));
            b.add(new JLabel("Type of Project: "));
            b.add(projectType);
            return b;
        }

        public Project.Type getProjectType() {
            return (Project.Type) projectType.getSelectedItem();
        }
    }
}
