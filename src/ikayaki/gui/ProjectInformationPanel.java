/*
* ProjectInformationPanel.java
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Allows inserting and editing project information.
 *
 * @author Mikko Jormalainen
 */
public class ProjectInformationPanel extends ProjectComponent {
/*
Event A: On change of contest in textfield - Notify project about change in project information.
*/
/*
Event B: On project event - Update textfields to correspond new project information.
*/

    private JLabel operatorLabel;
    private JTextField operatorTextField;

    private JLabel dateLabel;
    private JTextField dateTextField;

    /**
     * Groups autoMeasurement and manualMeasurement radiobuttons.
     */
    private ButtonGroup measurementType;
    private JRadioButton autoMeasurement;
    private JRadioButton manualMeasurement;

    private JLabel rocktypeLabel;
    private JTextField rocktypeTextField;

    private JLabel siteLabel;
    private JTextField siteTextField;

    private JLabel commentLabel;
    private JTextField commentTextField;

    private JLabel latitudeLabel;
    private JTextField latitudeTextField;

    private JLabel longLabel;
    private JTextField longTextField;

    private JLabel strikeLabel;
    private JTextField strikeTextField;

    private JLabel dipLabel;
    private JTextField dipTextField;

    private JLabel volumeLabel;
    private JTextField volumeTextField;

    private JLabel massLabel;
    private JTextField massTextField;

    /**
     * Groups coreSample and handSample radiobuttons.
     */
    private ButtonGroup sampleType;
    private JRadioButton coreSample;
    private JRadioButton handSample;

    /**
     * Creates default ProjectInformationPanel.
     */
    public ProjectInformationPanel() {
        setLayout(new GridLayout(0, 2, 5, 5));
        
        operatorLabel = new JLabel("Operator");
        operatorTextField = new JTextField(20);
        add(operatorLabel);
        add(operatorTextField);
        
        dateLabel = new JLabel("Date");
        dateTextField = new JTextField(20);
        add(dateLabel);
        add(dateTextField);
        
        operatorLabel = new JLabel("Operator");
        operatorTextField = new JTextField(20);
        add(operatorLabel);
        add(operatorTextField);
        
        measurementType = new ButtonGroup();
        autoMeasurement = new JRadioButton("Auto", true);
        manualMeasurement = new JRadioButton("Manual", false);
        measurementType.add(autoMeasurement);
        measurementType.add(manualMeasurement);
        add(autoMeasurement);
        add(manualMeasurement);
        
        rocktypeLabel = new JLabel("Rock type");
        rocktypeTextField = new JTextField(20);
        add(rocktypeLabel);
        add(rocktypeTextField);
        
        siteLabel = new JLabel("Site");
        siteTextField = new JTextField(20);
        add(siteLabel);
        add(siteTextField);
        
        commentLabel = new JLabel("Comment");
        commentTextField = new JTextField(20);
        add(commentLabel);
        add(commentTextField);
        
        latitudeLabel = new JLabel("Latitude");
        latitudeTextField = new JTextField(20);
        add(latitudeLabel);
        add(latitudeTextField);
        
        longLabel = new JLabel("Long");
        longTextField = new JTextField(20);
        add(longLabel);
        add(longTextField);
        
        strikeLabel = new JLabel("Strike");
        strikeTextField = new JTextField(20);
        add(strikeLabel);
        add(strikeTextField);
        
        dipLabel = new JLabel("Dip");
        dipTextField = new JTextField(20);
        add(dipLabel);
        add(dipTextField);
        
        volumeLabel = new JLabel("Volume");
        volumeTextField = new JTextField(20);
        add(volumeLabel);
        add(volumeTextField);
        
        massLabel = new JLabel("Mass");
        massTextField = new JTextField(20);
        add(massLabel);
        add(massTextField);
        
        sampleType = new ButtonGroup();
        coreSample = new JRadioButton("Core", true);
        handSample = new JRadioButton("Hand", false);
        sampleType.add(coreSample);
        sampleType.add(handSample);
        add(coreSample);
        add(handSample);
        
        return; // TODO
    }

    /**
     * Calls super.setProject(project) and updates textfield with new projects data.
     */
    public void setProject(Project project) {
        super.setProject(project);
    }

    public void projectUpdated(ProjectEvent event) {
        // DOES NOTHING
    }
}