/*
 * PrintPanel.java
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
import ikayaki.Project;
import java.util.Vector;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;

/**
 * Creates layout from MeasurementSequence and Plots to be printed
 *
 * @author Aki Korpua
 */
public class PrintPanel
    extends JPanel {

    Project project;

    /**
     * All plots in this panel
     */
    private Vector<AbstractPlot> plots = new Vector<AbstractPlot>();


    public PrintPanel(MainViewPanel mother) {

        //TODO: how we get current project?
        project = mother.getProject();

        //TODO: print project information

        //TODO: draw table of sequences

        IntensityPlot intensityPlot = new IntensityPlot();
        StereoPlot stereoPlot = new StereoPlot();

        plots.add(intensityPlot);
        plots.add(stereoPlot);

        for (Plot plot : plots) {
            plot.reset();
            if (project != null) {
                for (int i = 0; i < project.getSteps(); i++) {
                    plot.add(project.getStep(i));
                }
            }
        }

        setLayout(new GridBagLayout());
        for (AbstractPlot plot : plots) {
            add(plot);
        }

    }
}
