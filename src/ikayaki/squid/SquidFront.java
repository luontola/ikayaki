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

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Graphical front-end for using the SQUID Interface's protocol level commands.
 *
 * @author Esko Luontola
 */
public class SquidFront extends JFrame {

    private Squid squid;
    private JTextField param1;
    private JTextField param2;
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


    public SquidFront() throws HeadlessException {
        super("SQUID Debugger");

        // initialize the squid
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


        setLocationByPlatform(true);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        new SquidFront();
    }
}
