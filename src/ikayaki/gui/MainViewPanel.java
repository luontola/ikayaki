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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
     * Currently opened project.
     */
    private Project project;

    /**
     * Project which has an ongoing measurement, or null if no measurement is running.
     */
    private Project measuringProject;

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
    private Action exitAction;
    private Action configurationAction;
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
            File[] projectHistory = Settings.instance().getProjectHistory();
            if (projectHistory.length > 0) {
                project = Project.loadProject(projectHistory[0]);
            }
        }
//        setProject(project);    // the project must be set before doing the layout

        // TODO: testataan...
        setProject(null);    // the project must be set before doing the layout
        final Project p = project;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setProject(p); // TODO: causes the program flash at the start, but otherwise is good. sequence table scrolls nicely.
            }
        });


        /* Init SQUID interface */
        try {
            squid = Squid.instance();
        } catch (IOException e) {
            // TODO: what should be done now? give error message?
            //e.printStackTrace();
            System.err.println("Unable to initialize the SQUID interface.");
        }

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
        // TODO: make this as an Action
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

//        /* Finalize */
//        setProject(project);
    }

    /**
     * Loads a new project to all GUI components. This method will be called by the Project Explorer and Calibration
     * panels.
     *
     * @param project the project to be opened, or null to close the previous one.
     */
    @Override public void setProject(Project project) {
        if (project != null) {
            // update history logs
            if (project.getType() != Project.Type.CALIBRATION) {
                Settings.instance().updateProjectHistory(project.getFile());
                Settings.instance().updateDirectoryHistory(project.getFile().getAbsoluteFile().getParentFile());
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
        } else {
            // update GUI components
            Frame parent = getParentFrame();
            if (parent != null) {
                getParentFrame().setTitle(null);
            }
            getProjectInformationPanel().setBorder(BorderFactory.createTitledBorder("Project Information"));
            getExportProjectToDATAction().setEnabled(false);
            getExportProjectToTDTAction().setEnabled(false);
            getExportProjectToSRMAction().setEnabled(false);
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

        // close the previous project if it has no measurements running
        // (it will be closed by projectUpdated() if there is a measurement running)
        if (oldProject != null && oldProject != measuringProject && oldProject != project) {
            if (!Project.closeProject(oldProject)) {
                JOptionPane.showMessageDialog(this,
                        "Unable to close the project " + oldProject.getName(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Returns the active project, or null if no project is active.
     */
    @Override public Project getProject() {
        return project;
    }

    /**
     * Keeps track of which project has a measurement running.
     */
    @Override public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.STATE_CHANGED) {

            // check if the last measurement has stopped
            if (measuringProject != null && measuringProject.getState() == Project.State.IDLE) {

                /* TODO:
                 * It might be better that if the measuring ends when measuringProject is not active,
                 * it would not be closed. Otherwise it won't be possible for the user to see which of
                 * the steps were just completed (green color).
                 */
                // close the project if it is not anymore open
                if (measuringProject != project) {
                    if (Project.closeProject(measuringProject)) {
                        measuringProject = null;
                        project.setSquid(squid);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Unable to close the project " + measuringProject.getName(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    // measuringProject is no more measuring, but it is still the active project
                    measuringProject = null;
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
            JOptionPane.showMessageDialog(this,
                    "Can not exit. A measurement is running.",
                    "Error", JOptionPane.ERROR_MESSAGE);
//            System.err.println("Can not exit: a measurement is running");
            return;
        }
        if (!Settings.instance().saveNow()) {
            JOptionPane.showMessageDialog(this,
                    "Can not exit. Unable to save the settings.",
                    "Error", JOptionPane.ERROR_MESSAGE);
//            System.err.println("Can not exit: unable to save settings");
            return;
        }
        if (project != null) {
            if (!Project.closeProject(project)) {
                JOptionPane.showMessageDialog(this,
                        "Can not exit. Unable to close the project " + project.getName() + ".",
                        "Error", JOptionPane.ERROR_MESSAGE);
//                System.err.println("Can not exit: unable to close project " + project.getName());
                return;
            }
        }
        SerialIO.closeAllPorts();
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
            JOptionPane.showMessageDialog(this,
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
            JOptionPane.showMessageDialog(this,
                    "Unable to create the file " + file,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a file chooser and exports the active project to a different file format.
     *
     * @param type the type of file to export from the current project.
     * @throws NullPointerException     if type or the current project is null.
     * @throws IllegalArgumentException if type is not "dat", "tdt" or "srm".
     */
    public void exportProject(String type) {
        type = type.toLowerCase();
        JFileChooser chooser = new JFileChooser(Settings.instance().getLastDirectory());
        chooser.setFileFilter(new GenericFileFilter(type.toUpperCase() + " File", type));

        do {
            int returnVal = chooser.showSaveDialog(MainViewPanel.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                if (!file.getName().toLowerCase().endsWith("." + type)) {
                    file = new File(file.getAbsolutePath() + "." + type);
                }

                // overwrite old file?
                if (file.exists()) {
                    returnVal = JOptionPane.showConfirmDialog(MainViewPanel.this,
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
                    ok = getProject().exportToDAT(chooser.getSelectedFile());
                } else if (type.equals("tdt")) {
                    ok = getProject().exportToTDT(chooser.getSelectedFile());
                } else if (type.equals("srm")) {
                    ok = getProject().exportToSRM(chooser.getSelectedFile());
                } else {
                    throw new IllegalArgumentException("Unkown export type: " + type);
                }
                if (!ok) {
                    JOptionPane.showMessageDialog(MainViewPanel.this,
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
            measurementDetailsPanel = new MeasurementDetailsPanel();
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
                    NewProjectFileChooser chooser = new NewProjectFileChooser(Settings.instance().getLastDirectory());
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
                    JFileChooser chooser = new JFileChooser(Settings.instance().getLastDirectory());
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
                    exportProject("dat");
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
                    exportProject("tdt");
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
                    exportProject("srm");
                }
            };
            exportProjectToSRMAction.putValue(Action.NAME, "SRM File...");
            exportProjectToSRMAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);

        }
        return exportProjectToSRMAction;
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

    public Action getConfigurationAction() {
        if (configurationAction == null) {
            configurationAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    SettingsDialog.showSettingsDialog(getParentFrame(), "Configuration");
                }
            };
            configurationAction.putValue(Action.NAME, "Configuration");
            configurationAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        }
        return configurationAction;
    }

    public Action getHelpAction() {
        if (helpAction == null) {
            helpAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    // TODO
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
                    String message = Ikayaki.APP_NAME + " " + Ikayaki.APP_VERSION +
                            " / " + Ikayaki.APP_VERSION_NAME + "\n" +
                            Ikayaki.APP_BUILD + "\n\n" +
                            Ikayaki.APP_HOME_PAGE + "\n\n";
                    for (int i = 0; i < Ikayaki.AUTHORS.length; i++) {
                        message += Ikayaki.AUTHORS[i] + "\n";
                    }
                    JOptionPane.showMessageDialog(MainViewPanel.this,
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
