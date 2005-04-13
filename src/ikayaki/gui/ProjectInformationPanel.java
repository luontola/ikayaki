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

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import ikayaki.Project;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Allows inserting and editing project information.
 *
 * @author Esko Luontola
 */
public class ProjectInformationPanel extends ProjectComponent {

    /* Property names for saving values to Project */
    private static final String MEASUREMENT_TYPE_PROPERTY = "measurementType";
    private static final String MEASUREMENT_TYPE_AUTO_VALUE = "AUTO";
    private static final String MEASUREMENT_TYPE_MANUAL_VALUE = "MANUAL";
    private static final String OPERATOR_PROPERTY = "operator";
    private static final String DATE_PROPERTY = "date";
    private static final String ROCK_TYPE_PROPERTY = "rockType";
    private static final String SITE_PROPERTY = "site";
    private static final String COMMENT_PROPERTY = "comment";
    private static final String LATITUDE_PROPERTY = "latitude";
    private static final String LONGITUDE_PROPERTY = "longitude";

    /* Radio Button Groups */
    private ButtonGroup measurementType;
    private JRadioButton measurementTypeAuto;
    private JRadioButton measurementTypeManual;

    private ButtonGroup sampleType;
    private JRadioButton sampleTypeHand;
    private JRadioButton sampleTypeCore;

    private ButtonGroup normalization;
    private JRadioButton normalizationVolume;
    private JRadioButton normalizationMass;

    /* Plain Text Fields */
    private JTextField operatorField;
    private JTextField dateField;
    private JTextField rockTypeField;
    private JTextField siteField;
    private JTextField commentField;

    /* Number-only Text Fields */
    private JFormattedTextField latitudeField;
    private JFormattedTextField longitudeField;
    private JFormattedTextField strikeField;
    private JFormattedTextField dipField;
    private JFormattedTextField massField;
    private JFormattedTextField volumeField;
    private JFormattedTextField susceptibilityField;

    private JPanel contentPane;

    private boolean propertiesModified = false;
    private boolean parametersModified = false;

    /* Labels */
    private JLabel operatorLabel;
    private JLabel dateLabel;
    private JLabel measurementTypeLabel;
    private JLabel rockTypeLabel;
    private JLabel siteLabel;
    private JLabel commentLabel;
    private JLabel latitudeLabel;
    private JLabel longitudeLabel;
    private JLabel strikeLabel;
    private JLabel dipLabel;
    private JLabel volumeLabel;
    private JLabel massLabel;
    private JLabel susceptibilityLabel;
    private JLabel sampleTypeLabel;
    private JLabel normalizationLabel;

    /**
     * Creates default ProjectInformationPanel with no current project. Starts an autosaving thread.
     */
    public ProjectInformationPanel() {
        setLayout(new BorderLayout());
        add(contentPane, "Center");
        contentPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 4));

        /* Radio Button Groups */
        measurementType = new ButtonGroup();
        measurementType.add(measurementTypeAuto);
        measurementType.add(measurementTypeManual);

        sampleType = new ButtonGroup();
        sampleType.add(sampleTypeHand);
        sampleType.add(sampleTypeCore);

        normalization = new ButtonGroup();
        normalization.add(normalizationVolume);
        normalization.add(normalizationMass);

        /* Formatted Text Fields */
        MyFormatterFactory factory = new MyFormatterFactory();
        latitudeField.setFormatterFactory(factory);
        longitudeField.setFormatterFactory(factory);
        strikeField.setFormatterFactory(factory);
        dipField.setFormatterFactory(factory);
        massField.setFormatterFactory(factory);
        volumeField.setFormatterFactory(factory);
        susceptibilityField.setFormatterFactory(factory);

        /* Listeners for properties */
        DocumentListener propertiesDocumentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                initSaveProperties();
            }

            public void removeUpdate(DocumentEvent e) {
                initSaveProperties();
            }

            public void changedUpdate(DocumentEvent e) {
                initSaveProperties();
            }
        };
        ActionListener propertiesActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initSaveProperties();
            }
        };

        measurementTypeAuto.addActionListener(propertiesActionListener);
        measurementTypeManual.addActionListener(propertiesActionListener);

        operatorField.getDocument().addDocumentListener(propertiesDocumentListener);
        dateField.getDocument().addDocumentListener(propertiesDocumentListener);
        rockTypeField.getDocument().addDocumentListener(propertiesDocumentListener);
        siteField.getDocument().addDocumentListener(propertiesDocumentListener);
        commentField.getDocument().addDocumentListener(propertiesDocumentListener);

        latitudeField.getDocument().addDocumentListener(propertiesDocumentListener);
        longitudeField.getDocument().addDocumentListener(propertiesDocumentListener);

        /* Listeners for parameters */
        DocumentListener parametersDocumentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                initSaveParameters();
            }

            public void removeUpdate(DocumentEvent e) {
                initSaveParameters();
            }

            public void changedUpdate(DocumentEvent e) {
                initSaveParameters();
            }
        };
        ActionListener parametersActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                initSaveParameters();
            }
        };

        sampleTypeHand.addActionListener(parametersActionListener);
        sampleTypeCore.addActionListener(parametersActionListener);
        normalizationVolume.addActionListener(parametersActionListener);
        normalizationMass.addActionListener(parametersActionListener);

        strikeField.getDocument().addDocumentListener(parametersDocumentListener);
        dipField.getDocument().addDocumentListener(parametersDocumentListener);
        massField.getDocument().addDocumentListener(parametersDocumentListener);
        volumeField.getDocument().addDocumentListener(parametersDocumentListener);
        susceptibilityField.getDocument().addDocumentListener(parametersDocumentListener);

        /* Autosaving at regular intervals */
        Timer autosave = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveProperties();
                saveParameters();
            }
        });
        autosave.start();
        
        // initialize with no project
        setProject(null);
    }

    /**
     * Sets whether or not this component is enabled. Affects all project information form fields.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        /* Radio Button Groups */
        measurementTypeAuto.setEnabled(enabled);
        measurementTypeManual.setEnabled(enabled);
        sampleTypeHand.setEnabled(enabled);
        sampleTypeCore.setEnabled(enabled);
        normalizationVolume.setEnabled(enabled);
        normalizationMass.setEnabled(enabled);

        /* Plain Text Fields */
        operatorField.setEnabled(enabled);
        dateField.setEnabled(enabled);
        rockTypeField.setEnabled(enabled);
        siteField.setEnabled(enabled);
        commentField.setEnabled(enabled);

        /* Number Fields */
        latitudeField.setEnabled(enabled);
        longitudeField.setEnabled(enabled);
        strikeField.setEnabled(enabled);
        dipField.setEnabled(enabled);
        massField.setEnabled(enabled);
        volumeField.setEnabled(enabled);
        susceptibilityField.setEnabled(enabled);

        /* Labels */
        operatorLabel.setEnabled(enabled);
        dateLabel.setEnabled(enabled);
        measurementTypeLabel.setEnabled(enabled);
        rockTypeLabel.setEnabled(enabled);
        siteLabel.setEnabled(enabled);
        commentLabel.setEnabled(enabled);
        latitudeLabel.setEnabled(enabled);
        longitudeLabel.setEnabled(enabled);
        strikeLabel.setEnabled(enabled);
        dipLabel.setEnabled(enabled);
        volumeLabel.setEnabled(enabled);
        massLabel.setEnabled(enabled);
        susceptibilityLabel.setEnabled(enabled);
        sampleTypeLabel.setEnabled(enabled);
        normalizationLabel.setEnabled(enabled);
    }

    /**
     * Calls super.setProject(project) and updates textfield with new projects data.
     */
    public void setProject(Project project) {

        // save the old project's values
        saveProperties();
        saveParameters();

        super.setProject(project);
        setEnabled(project != null);

        if (project != null) {
            // get values from the new project

            /* Radio Button Groups */
            measurementTypeAuto.setSelected(project.getProperty(MEASUREMENT_TYPE_PROPERTY,
                    MEASUREMENT_TYPE_AUTO_VALUE).equals(MEASUREMENT_TYPE_AUTO_VALUE));
            measurementTypeManual.setSelected(project.getProperty(MEASUREMENT_TYPE_PROPERTY,
                    MEASUREMENT_TYPE_AUTO_VALUE).equals(MEASUREMENT_TYPE_MANUAL_VALUE));
            sampleTypeHand.setSelected(project.getSampleType() == Project.SampleType.HAND);
            sampleTypeCore.setSelected(project.getSampleType() == Project.SampleType.CORE);
            normalizationVolume.setSelected(project.getNormalization() == Project.Normalization.VOLUME);
            normalizationMass.setSelected(project.getNormalization() == Project.Normalization.MASS);

            /* Plain Text Fields */
            operatorField.setText(project.getProperty(OPERATOR_PROPERTY, ""));
            dateField.setText(project.getProperty(DATE_PROPERTY, DateFormat.getDateInstance().format(new Date())));
            rockTypeField.setText(project.getProperty(ROCK_TYPE_PROPERTY, ""));
            siteField.setText(project.getProperty(SITE_PROPERTY, ""));
            commentField.setText(project.getProperty(COMMENT_PROPERTY, ""));

            /* Number Fields */
            latitudeField.setText(project.getProperty(LATITUDE_PROPERTY, ""));
            longitudeField.setText(project.getProperty(LONGITUDE_PROPERTY, ""));
            strikeField.setValue(new Double(project.getStrike()));
            dipField.setValue(new Double(project.getDip()));
            massField.setValue(new Double(project.getMass()));
            volumeField.setValue(new Double(project.getVolume()));
            susceptibilityField.setValue(new Double(project.getSusceptibility()));
        } else {
            // no project active, clear the form fields

            /* Radio Button Groups */
            measurementTypeAuto.setSelected(true);
            measurementTypeManual.setSelected(false);
            sampleTypeHand.setSelected(true);
            sampleTypeCore.setSelected(false);
            normalizationVolume.setSelected(true);
            normalizationMass.setSelected(false);

            /* Plain Text Fields */
            operatorField.setText("");
            dateField.setText("");
            rockTypeField.setText("");
            siteField.setText("");
            commentField.setText("");

            /* Number Fields */
            // TODO: should I use setValue? otherwise the field might remember the number.
            latitudeField.setText("");
            longitudeField.setText("");
            strikeField.setText("");
            dipField.setText("");
            massField.setText("");
            volumeField.setText("");
            susceptibilityField.setText("");
        }

        // prevent the saving of unchanged values
        propertiesModified = false;
        parametersModified = false;
    }

    /**
     * Schedules the running of saveProperties().
     */
    private void initSaveProperties() {
        propertiesModified = true;
    }

    /**
     * Schedules the running of saveParameters().
     */
    private void initSaveParameters() {
        parametersModified = true;
    }

    /**
     * Saves to the project file those properties, that do not affect the measurement calculations. Will do nothing if
     * propertiesModified is false.
     *
     * @throws NullPointerException if the current project is null.
     */
    private void saveProperties() {
        if (!propertiesModified) {
            return;
        }
//        System.out.println("Properties saved");

        /* Radio Button Groups */
        if (measurementTypeAuto.isSelected()) {
            getProject().setProperty(MEASUREMENT_TYPE_PROPERTY, MEASUREMENT_TYPE_AUTO_VALUE);
        }
        if (measurementTypeManual.isSelected()) {
            getProject().setProperty(MEASUREMENT_TYPE_PROPERTY, MEASUREMENT_TYPE_MANUAL_VALUE);
        }

        /* Plain Text Fields */
        getProject().setProperty(OPERATOR_PROPERTY, operatorField.getText());
        getProject().setProperty(DATE_PROPERTY, dateField.getText());
        getProject().setProperty(ROCK_TYPE_PROPERTY, rockTypeField.getText());
        getProject().setProperty(SITE_PROPERTY, siteField.getText());
        getProject().setProperty(COMMENT_PROPERTY, commentField.getText());

        /* Number Fields */
        getProject().setProperty(LATITUDE_PROPERTY, latitudeField.getText());
        getProject().setProperty(LONGITUDE_PROPERTY, longitudeField.getText());

        propertiesModified = false;
    }

    /**
     * Saves to the project file those parameters, that affect the measurement calculations. Will do nothing if
     * parametersModified is false.
     *
     * @throws NullPointerException if the current project is null.
     */
    private void saveParameters() {
        if (!parametersModified) {
            return;
        }
//        System.out.println("Parameters saved");

        /* Radio Button Groups */
        if (sampleTypeHand.isSelected()) {
            getProject().setSampleType(Project.SampleType.HAND);
        }
        if (sampleTypeCore.isSelected()) {
            getProject().setSampleType(Project.SampleType.CORE);
        }
        if (normalizationVolume.isSelected()) {
            getProject().setNormalization(Project.Normalization.VOLUME);
        }
        if (normalizationMass.isSelected()) {
            getProject().setNormalization(Project.Normalization.MASS);
        }

        /* Number-only Text Fields */
        Number value;
        value = (Number) strikeField.getValue();
        getProject().setStrike(value.doubleValue());
        value = (Number) dipField.getValue();
        getProject().setDip(value.doubleValue());
        value = (Number) massField.getValue();
        getProject().setMass(value.doubleValue());
        value = (Number) volumeField.getValue();
        getProject().setVolume(value.doubleValue());
        value = (Number) susceptibilityField.getValue();
        getProject().setSusceptibility(value.doubleValue());

        parametersModified = false;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// !!! IMPORTANT !!!
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer !!! IMPORTANT !!! DO NOT edit this method OR call it in your
     * code!
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(15, 2, new Insets(0, 0, 0, 0), -1, 2));
        operatorLabel = new JLabel();
        operatorLabel.setText("Operator");
        contentPane.add(operatorLabel,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        operatorField = new JTextField();
        contentPane.add(operatorField,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        dateField = new JTextField();
        contentPane.add(dateField,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        massLabel = new JLabel();
        massLabel.setText("Mass (grams)");
        contentPane.add(massLabel,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        volumeLabel = new JLabel();
        volumeLabel.setText("Volume (cm³)");
        contentPane.add(volumeLabel,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dipLabel = new JLabel();
        dipLabel.setText("Dip");
        contentPane.add(dipLabel,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        strikeLabel = new JLabel();
        strikeLabel.setText("Strike");
        contentPane.add(strikeLabel,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        longitudeLabel = new JLabel();
        longitudeLabel.setText("Longitude");
        contentPane.add(longitudeLabel,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        latitudeLabel = new JLabel();
        latitudeLabel.setText("Latitude");
        contentPane.add(latitudeLabel,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        commentLabel = new JLabel();
        commentLabel.setText("Comment");
        contentPane.add(commentLabel,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        siteLabel = new JLabel();
        siteLabel.setText("Site");
        contentPane.add(siteLabel,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        rockTypeLabel = new JLabel();
        rockTypeLabel.setText("Rock type");
        contentPane.add(rockTypeLabel,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        commentField = new JTextField();
        contentPane.add(commentField,
                new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        rockTypeField = new JTextField();
        contentPane.add(rockTypeField,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        siteField = new JTextField();
        contentPane.add(siteField,
                new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        dateLabel = new JLabel();
        dateLabel.setText("Date");
        contentPane.add(dateLabel,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        latitudeField = new JFormattedTextField();
        contentPane.add(latitudeField,
                new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        massField = new JFormattedTextField();
        contentPane.add(massField,
                new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        volumeField = new JFormattedTextField();
        contentPane.add(volumeField,
                new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        dipField = new JFormattedTextField();
        contentPane.add(dipField,
                new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        longitudeField = new JFormattedTextField();
        contentPane.add(longitudeField,
                new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        strikeField = new JFormattedTextField();
        contentPane.add(strikeField,
                new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        measurementTypeAuto = new JRadioButton();
        measurementTypeAuto.setText("Auto");
        panel1.add(measurementTypeAuto,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        measurementTypeManual = new JRadioButton();
        measurementTypeManual.setText("Manual");
        panel1.add(measurementTypeManual,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        measurementTypeLabel = new JLabel();
        measurementTypeLabel.setText("Measurement type");
        contentPane.add(measurementTypeLabel,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2,
                new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        sampleTypeCore = new JRadioButton();
        sampleTypeCore.setText("Core");
        panel2.add(sampleTypeCore,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        sampleTypeHand = new JRadioButton();
        sampleTypeHand.setText("Hand");
        panel2.add(sampleTypeHand,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        sampleTypeLabel = new JLabel();
        sampleTypeLabel.setText("Sample type");
        contentPane.add(sampleTypeLabel,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        susceptibilityLabel = new JLabel();
        susceptibilityLabel.setText("Susceptibility");
        contentPane.add(susceptibilityLabel,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        susceptibilityField = new JFormattedTextField();
        contentPane.add(susceptibilityField,
                new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(70, -1), null));
        normalizationLabel = new JLabel();
        normalizationLabel.setText("Normalize by");
        contentPane.add(normalizationLabel,
                new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3,
                new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        normalizationMass = new JRadioButton();
        normalizationMass.setText("Mass");
        panel3.add(normalizationMass,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        normalizationVolume = new JRadioButton();
        normalizationVolume.setText("Volume");
        panel3.add(normalizationVolume,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }

    /**
     * Custom formatter factory for the JFormattedTextFields in this class.
     * 
     * @author Esko Luontola
     */
    private class MyFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
        /**
         * Returns an <code>AbstractFormatter</code> that can handle formatting of the passed in
         * <code>JFormattedTextField</code>.
         *
         * @param tf JFormattedTextField requesting AbstractFormatter
         * @return AbstractFormatter to handle formatting duties, a null return value implies the JFormattedTextField
         *         should behave like a normal JTextField
         */
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
            NumberFormatter formatter;
            DecimalFormat format;

            if (tf == latitudeField || tf == longitudeField) {
                // allow null values
                format = new NullableDecimalFormat();
            } else if (tf == massField || tf == volumeField || tf == susceptibilityField) {
                // show only positive numbers
                format = new PositiveDecimalFormat();
            } else {
                // show all numbers
                format = new DecimalFormat();
            }
            format.setGroupingUsed(true);
            format.setMaximumFractionDigits(6);
            formatter = new NumberFormatter(format);

            // set value ranges
            if (tf == strikeField) {
                formatter.setMinimum(new Double(0));
                formatter.setMaximum(new Double(360));
            } else if (tf == dipField) {
                formatter.setMinimum(new Double(-90));
                formatter.setMaximum(new Double(90));
            }
            return formatter;
        }
    }
}