/*
 * MeasurementGraphsPanel.java
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

import ikayaki.MeasurementEvent;
import ikayaki.Project;
import ikayaki.ProjectEvent;
import ikayaki.ProjectListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * @author Aki Sysmäläinen
 */
public class MeasurementGraphsPanel extends ProjectComponent implements ProjectListener {
    /**
     * All plots in this panel
     */
    private Vector<Plot> plots = new Vector<Plot>();

    /**
     * Creates new panel for plots
     */
    public MeasurementGraphsPanel() {

        IntensityPlot intensityPlot = new IntensityPlot();
        StereoPlot stereoPlot = new StereoPlot();

        plots.add(intensityPlot);
        plots.add(stereoPlot);
        //stereoPlot.setEnabled(true);
        //intensityPlot.setEnabled(true);


        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("1: Intensity", intensityPlot);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("2: Stereo", stereoPlot);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.addTab("3: Zijderweld", new JPanel());
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        setLayout(new BorderLayout());
        add(tabbedPane, "Center");

        // initialize with no project
        setProject(null);

        return; // TODO
    }

    /**
     * Updates plots when additional measurements are done or the data has changed.
     */
    private void updatePlots() {
        // update all plots with the MeasurementSteps from the project
        for (Plot plot : plots) {
            plot.reset();
            if (getProject() != null) {
                for (int i = 0; i < getProject().getSteps(); i++) {
                    plot.add(getProject().getStep(i));
                }
            }
        }
    }

    /**
     * Listener to listen events if projects state is changed.
     */
    public void projectUpdated(ProjectEvent event) {
        if (event.getType() == ProjectEvent.Type.STATE_CHANGED || event.getType() == ProjectEvent.Type.DATA_CHANGED) {
            updatePlots();
        }
    }

    /**
     * @param event MeasurementEvent received.
     */
    public void measurementUpdated(MeasurementEvent event) {
        if (event.getType() == MeasurementEvent.Type.VALUE_MEASURED) {
            updatePlots();
        }
    }

    /**
     * Sets the project for this ProjectComponent. Unregisters MeasurementListener and ProjectListener from the old
     * project, and registers them to the new project.
     *
     * @param project new active project, or null to make no project active.
     */
    @Override public void setProject(Project project) {
        super.setProject(project);
        updatePlots();
    }


    public static void main(String args[]) {
        JFrame f = new JFrame();
        f.setLayout(new BorderLayout());
        f.setContentPane(new MeasurementGraphsPanel());
        f.setLocationByPlatform(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}
