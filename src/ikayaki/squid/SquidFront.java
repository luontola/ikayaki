/*
 * SquidFront.java
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

package ikayaki.squid;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyBlue;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Graphical front-end for using the SQUID Interface's protocol level commands.
 *
 * @author Esko Luontola
 */
public class SquidFront extends JFrame {

    private static boolean DEBUG = false; // debug the program without squid

    private JButton hupdateSettings;
    private JButton hgetStatus;
    private JButton hgetPosition;
    private JButton hgetRotation;
    private JButton hisOK;
    private JButton hmoveToHome;
    private JButton hmoveToDegausserZ;
    private JButton hmoveToDegausserY;
    private JButton hmoveToMeasurement;
    private JButton hmoveToBackground;
    private JButton hmoveToPos;
    private JButton hstop;
    private JButton hrotateTo;
    private JButton hsetOnline;
    private JButton hsetAcceleration;
    private JButton hsetDeceleration;
    private JButton hsetBaseSpeed;
    private JButton hsetVelocity;
    private JButton hsetHoldTime;
    private JButton hsetCrystalFrequence;
    private JButton hstopExecution;
    private JButton hperformSlew;
    private JButton hsetMotorPositive;
    private JButton hsetMotorNegative;
    private JButton hsetSteps;
    private JButton hsetPosition;
    private JButton hgo;
    private JButton hjoin;
    private JButton hverify;
    private JButton hsetPositionRegister;
    private JButton htakeMessage;

    private JButton mupdateSettings;
    private JButton mreset;
    private JButton mresetCounter;
    private JButton mconfigure;
    private JButton mlatchAnalog;
    private JButton mlatchCounter;
    private JButton mgetData;
    private JButton mopenLoop;
    private JButton mclearFlux;
    private JButton mjoin;
    private JButton mreadData;
    private JButton mgetFilters;
    private JButton mgetRange;
    private JButton mgetSlew;
    private JButton mgetLoop;
    private JButton misOK;

    private JButton dupdateSettings;
    private JButton dsetCoil;
    private JButton dsetAmplitude;
    private JButton dexecuteRampUp;
    private JButton dexecuteRampDown;
    private JButton dexecuteRampCycle;
    private JButton ddemagnetizeZ;
    private JButton ddemagnetizeY;
    private JButton dgetRampStatus;
    private JButton dgetRamp;
    private JButton dgetDelay;
    private JButton dgetCoil;
    private JButton dgetAmplitude;
    private JButton disOK;

    private JTextField param1;
    private JTextField param2;
    private JTextField param3;

    private JTextArea handlerLog;
    private JTextArea magnetometerLog;
    private JTextArea degausserLog;

    private JPanel contentPane;

    private Squid squid;

    public SquidFront() throws HeadlessException {
        super("SQUID Front");

        // initialize the squid
        if (!DEBUG) {
            try {
                squid = Squid.instance();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            if (!squid.isOK()) {
                System.err.println("SQUID is not OK!");
                System.exit(2);
            }
        }

        initHandlerActions();
        initMagnetometerActions();
        initDegausserActions();
        initLogging();

        setLayout(new BorderLayout());
        setContentPane(contentPane);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Sets ActionListeners for handler's control buttons.
     */
    private void initHandlerActions() {
        // TODO
    }

    /**
     * Sets ActionListeners for magnetometer's control buttons.
     */
    private void initMagnetometerActions() {
        // TODO
    }

    /**
     * Sets ActionListeners for degausser's control buttons.
     */
    private void initDegausserActions() {
        // TODO
    }

    /**
     * Sets anything that is needed for logging to file and to screen.
     */
    private void initLogging() {
        // TODO
    }

    public static void main(String[] args) {
        PlasticLookAndFeel.setMyCurrentTheme(new SkyBlue());
        try {
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println(e);
        }

        new SquidFront();
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
        contentPane.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), 6, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label1 = new JLabel();
        label1.setText("Handler");
        panel1.add(label1,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(16, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        hupdateSettings = new JButton();
        hupdateSettings.setHorizontalAlignment(2);
        hupdateSettings.setHorizontalTextPosition(11);
        hupdateSettings.setText("updateSettings():void");
        hupdateSettings.setVerticalTextPosition(0);
        panel2.add(hupdateSettings,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgetStatus = new JButton();
        hgetStatus.setHorizontalAlignment(2);
        hgetStatus.setText("getStatus():char");
        panel2.add(hgetStatus,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgetPosition = new JButton();
        hgetPosition.setHorizontalAlignment(2);
        hgetPosition.setText("getPosition():int");
        panel2.add(hgetPosition,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgetRotation = new JButton();
        hgetRotation.setHorizontalAlignment(2);
        hgetRotation.setText("getRotation():int");
        panel2.add(hgetRotation,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hisOK = new JButton();
        hisOK.setHorizontalAlignment(2);
        hisOK.setText("isOK():boolean");
        panel2.add(hisOK,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToHome = new JButton();
        hmoveToHome.setHorizontalAlignment(2);
        hmoveToHome.setText("moveToHome():void");
        panel2.add(hmoveToHome,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToDegausserZ = new JButton();
        hmoveToDegausserZ.setHorizontalAlignment(2);
        hmoveToDegausserZ.setText("moveToDegausserZ():void");
        panel2.add(hmoveToDegausserZ,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToDegausserY = new JButton();
        hmoveToDegausserY.setHorizontalAlignment(2);
        hmoveToDegausserY.setText("moveToDegausserY():void");
        panel2.add(hmoveToDegausserY,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToMeasurement = new JButton();
        hmoveToMeasurement.setHorizontalAlignment(2);
        hmoveToMeasurement.setText("moveToMeasurement():void");
        panel2.add(hmoveToMeasurement,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToBackground = new JButton();
        hmoveToBackground.setHorizontalAlignment(2);
        hmoveToBackground.setText("moveToBackground():void");
        panel2.add(hmoveToBackground,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hmoveToPos = new JButton();
        hmoveToPos.setHorizontalAlignment(2);
        hmoveToPos.setText("moveToPos(int pos):boolean");
        panel2.add(hmoveToPos,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hstop = new JButton();
        hstop.setHorizontalAlignment(2);
        hstop.setText("stop():void");
        panel2.add(hstop,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hrotateTo = new JButton();
        hrotateTo.setHorizontalAlignment(2);
        hrotateTo.setText("rotateTo(int angle):void");
        panel2.add(hrotateTo,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetOnline = new JButton();
        hsetOnline.setHorizontalAlignment(2);
        hsetOnline.setText("setOnline():void");
        panel2.add(hsetOnline,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetAcceleration = new JButton();
        hsetAcceleration.setHorizontalAlignment(2);
        hsetAcceleration.setText("setAcceleration(int a):void");
        panel2.add(hsetAcceleration,
                new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetDeceleration = new JButton();
        hsetDeceleration.setHorizontalAlignment(2);
        hsetDeceleration.setText("setDeceleration(int d):void");
        panel2.add(hsetDeceleration,
                new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetBaseSpeed = new JButton();
        hsetBaseSpeed.setHorizontalAlignment(2);
        hsetBaseSpeed.setText("setBaseSpeed(int b):void");
        panel2.add(hsetBaseSpeed,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetVelocity = new JButton();
        hsetVelocity.setHorizontalAlignment(2);
        hsetVelocity.setText("setVelocity(int v):void");
        panel2.add(hsetVelocity,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetHoldTime = new JButton();
        hsetHoldTime.setHorizontalAlignment(2);
        hsetHoldTime.setText("setHoldTime(int h):void");
        panel2.add(hsetHoldTime,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetCrystalFrequence = new JButton();
        hsetCrystalFrequence.setHorizontalAlignment(2);
        hsetCrystalFrequence.setText("setCrystalFrequence(int cf):void");
        panel2.add(hsetCrystalFrequence,
                new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hstopExecution = new JButton();
        hstopExecution.setHorizontalAlignment(2);
        hstopExecution.setText("stopExecution():void");
        panel2.add(hstopExecution,
                new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hperformSlew = new JButton();
        hperformSlew.setHorizontalAlignment(2);
        hperformSlew.setText("performSlew():void");
        panel2.add(hperformSlew,
                new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetMotorPositive = new JButton();
        hsetMotorPositive.setHorizontalAlignment(2);
        hsetMotorPositive.setText("setMotorPositive():void");
        panel2.add(hsetMotorPositive,
                new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetMotorNegative = new JButton();
        hsetMotorNegative.setHorizontalAlignment(2);
        hsetMotorNegative.setText("setMotorNegative():void");
        panel2.add(hsetMotorNegative,
                new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetSteps = new JButton();
        hsetSteps.setHorizontalAlignment(2);
        hsetSteps.setText("setSteps(int s):void");
        panel2.add(hsetSteps,
                new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetPosition = new JButton();
        hsetPosition.setHorizontalAlignment(2);
        hsetPosition.setText("setPosition(int p):void");
        panel2.add(hsetPosition,
                new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hgo = new JButton();
        hgo.setHorizontalAlignment(2);
        hgo.setText("go():void");
        panel2.add(hgo,
                new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hjoin = new JButton();
        hjoin.setHorizontalAlignment(2);
        hjoin.setText("join():void");
        panel2.add(hjoin,
                new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hverify = new JButton();
        hverify.setHorizontalAlignment(2);
        hverify.setText("verify(char v):String");
        panel2.add(hverify,
                new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        hsetPositionRegister = new JButton();
        hsetPositionRegister.setHorizontalAlignment(2);
        hsetPositionRegister.setText("setPositionRegister(int r):void");
        panel2.add(hsetPositionRegister,
                new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        htakeMessage = new JButton();
        htakeMessage.setHorizontalAlignment(2);
        htakeMessage.setText("takeMessage():char");
        panel2.add(htakeMessage,
                new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label2 = new JLabel();
        label2.setText("Degausser");
        panel3.add(label2,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(14, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        dupdateSettings = new JButton();
        dupdateSettings.setHorizontalAlignment(2);
        dupdateSettings.setText("updateSettings():void");
        panel4.add(dupdateSettings,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dsetCoil = new JButton();
        dsetCoil.setHorizontalAlignment(2);
        dsetCoil.setText("setCoil(char coil):void");
        panel4.add(dsetCoil,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dsetAmplitude = new JButton();
        dsetAmplitude.setHorizontalAlignment(2);
        dsetAmplitude.setText("setAmplitude(int amplitude):void");
        panel4.add(dsetAmplitude,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dexecuteRampUp = new JButton();
        dexecuteRampUp.setHorizontalAlignment(2);
        dexecuteRampUp.setText("executeRampUp():void");
        panel4.add(dexecuteRampUp,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dexecuteRampDown = new JButton();
        dexecuteRampDown.setHorizontalAlignment(2);
        dexecuteRampDown.setText("executeRampDown():void");
        panel4.add(dexecuteRampDown,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dexecuteRampCycle = new JButton();
        dexecuteRampCycle.setHorizontalAlignment(2);
        dexecuteRampCycle.setText("executeRampCycle():void");
        panel4.add(dexecuteRampCycle,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        ddemagnetizeZ = new JButton();
        ddemagnetizeZ.setHorizontalAlignment(2);
        ddemagnetizeZ.setText("demagnetizeZ(int amplitude):boolean");
        panel4.add(ddemagnetizeZ,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        ddemagnetizeY = new JButton();
        ddemagnetizeY.setHorizontalAlignment(2);
        ddemagnetizeY.setText("demagnetizeY(int amplitude):boolean");
        panel4.add(ddemagnetizeY,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetRampStatus = new JButton();
        dgetRampStatus.setHorizontalAlignment(2);
        dgetRampStatus.setText("getRampStatus():char");
        panel4.add(dgetRampStatus,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetRamp = new JButton();
        dgetRamp.setHorizontalAlignment(2);
        dgetRamp.setText("getRamp():int");
        panel4.add(dgetRamp,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetDelay = new JButton();
        dgetDelay.setHorizontalAlignment(2);
        dgetDelay.setText("getDelay():int");
        panel4.add(dgetDelay,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetCoil = new JButton();
        dgetCoil.setEnabled(true);
        dgetCoil.setHorizontalAlignment(2);
        dgetCoil.setText("getCoil():char");
        panel4.add(dgetCoil,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        dgetAmplitude = new JButton();
        dgetAmplitude.setHorizontalAlignment(2);
        dgetAmplitude.setText("getAmplitude():int");
        panel4.add(dgetAmplitude,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        disOK = new JButton();
        disOK.setHorizontalAlignment(2);
        disOK.setText("isOK():boolean");
        panel4.add(disOK,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel5,
                new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        param1 = new JTextField();
        panel5.add(param1,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        param2 = new JTextField();
        panel5.add(param2,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        final JLabel label3 = new JLabel();
        label3.setText("param2");
        panel5.add(label3,
                new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("param1");
        panel5.add(label4,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        param3 = new JTextField();
        panel5.add(param3,
                new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(100, -1), null));
        final JLabel label5 = new JLabel();
        label5.setText("param3");
        panel5.add(label5,
                new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3,
                new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(22);
        contentPane.add(scrollPane1,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        handlerLog = new JTextArea();
        handlerLog.setRows(6);
        scrollPane1.setViewportView(handlerLog);
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setHorizontalScrollBarPolicy(30);
        scrollPane2.setVerticalScrollBarPolicy(22);
        contentPane.add(scrollPane2,
                new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        degausserLog = new JTextArea();
        degausserLog.setRows(6);
        scrollPane2.setViewportView(degausserLog);
        final JScrollPane scrollPane3 = new JScrollPane();
        scrollPane3.setHorizontalScrollBarPolicy(30);
        scrollPane3.setVerticalScrollBarPolicy(22);
        contentPane.add(scrollPane3,
                new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        magnetometerLog = new JTextArea();
        magnetometerLog.setRows(6);
        scrollPane3.setViewportView(magnetometerLog);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel6,
                new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        final JLabel label6 = new JLabel();
        label6.setText("Magnetometer");
        panel6.add(label6,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        final Spacer spacer4 = new Spacer();
        panel6.add(spacer4,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(16, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null));
        mupdateSettings = new JButton();
        mupdateSettings.setHorizontalAlignment(2);
        mupdateSettings.setText("updateSettings():void");
        panel7.add(mupdateSettings,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mreset = new JButton();
        mreset.setHorizontalAlignment(2);
        mreset.setText("reset(char axis):void");
        panel7.add(mreset,
                new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mresetCounter = new JButton();
        mresetCounter.setHorizontalAlignment(2);
        mresetCounter.setText("resetCounter(char axis):void");
        panel7.add(mresetCounter,
                new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mconfigure = new JButton();
        mconfigure.setHorizontalAlignment(2);
        mconfigure.setText("configure(char axis, char subcommand, char option):void");
        panel7.add(mconfigure,
                new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mlatchAnalog = new JButton();
        mlatchAnalog.setHorizontalAlignment(2);
        mlatchAnalog.setText("latchAnalog(char axis):void");
        panel7.add(mlatchAnalog,
                new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mlatchCounter = new JButton();
        mlatchCounter.setHorizontalAlignment(2);
        mlatchCounter.setText("latchCounter(char axis):void");
        panel7.add(mlatchCounter,
                new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetData = new JButton();
        mgetData.setHorizontalAlignment(2);
        mgetData.setText("getData(char axis, char command, String datavalues):String");
        panel7.add(mgetData,
                new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mopenLoop = new JButton();
        mopenLoop.setHorizontalAlignment(2);
        mopenLoop.setText("openLoop(char axis):void");
        panel7.add(mopenLoop,
                new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mclearFlux = new JButton();
        mclearFlux.setHorizontalAlignment(2);
        mclearFlux.setText("clearFlux(char axis):void");
        panel7.add(mclearFlux,
                new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mjoin = new JButton();
        mjoin.setHorizontalAlignment(2);
        mjoin.setText("join():void");
        panel7.add(mjoin,
                new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mreadData = new JButton();
        mreadData.setHorizontalAlignment(2);
        mreadData.setText("readData():Double[3]");
        panel7.add(mreadData,
                new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetFilters = new JButton();
        mgetFilters.setHorizontalAlignment(2);
        mgetFilters.setText("getFilters():char[3]");
        panel7.add(mgetFilters,
                new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetRange = new JButton();
        mgetRange.setHorizontalAlignment(2);
        mgetRange.setText("getRange():char[3]");
        panel7.add(mgetRange,
                new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetSlew = new JButton();
        mgetSlew.setHorizontalAlignment(2);
        mgetSlew.setText("getSlew():boolean[3]");
        panel7.add(mgetSlew,
                new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mgetLoop = new JButton();
        mgetLoop.setHorizontalAlignment(2);
        mgetLoop.setText("getLoop():boolean[3]");
        panel7.add(mgetLoop,
                new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        misOK = new JButton();
        misOK.setHorizontalAlignment(2);
        misOK.setText("isOK():boolean");
        panel7.add(misOK,
                new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }
}
