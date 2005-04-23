/*
 * SettingsDialog.java
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
import java.awt.*;

/**
 * Opens dialog and creates DeviceSettingsPanel
 *
 * @author Aki Korpua
 */
public class SettingsDialog extends JDialog {

    private static final int DEVICE_SETTINGS = 0;
    private static final int PROGRAM_SETTINGS = 1;

    private int dialogType;

    private SettingsDialog(Frame owner, String message, int dialogType) {
        super(owner, message, true);
        if (owner != null) {
            setLocationRelativeTo(owner);
        }
        this.dialogType = dialogType;
    }

    /**
     * Creates all components and puts them in right places. Labels are created only here (no global fields). Creates
     * ActionListeners for buttons.
     */
    protected void dialogInit() {
        super.dialogInit();

        setResizable(false);
        setLayout(new BorderLayout());
        if (dialogType == DEVICE_SETTINGS) {
            add(new DeviceSettingsPanel(this), BorderLayout.CENTER);
        } else if (dialogType == PROGRAM_SETTINGS) {
            add(new ProgramSettingsPanel(this), BorderLayout.CENTER);
        }
        pack();
    }

    public static void showDeviceSettingsDialog(Frame owner, String message) {
        SettingsDialog d = new SettingsDialog(owner, message, DEVICE_SETTINGS);
        d.setVisible(true);
    }

    public static void showProgramSettingsDialog(Frame owner, String message) {
        SettingsDialog d = new SettingsDialog(owner, message, PROGRAM_SETTINGS);
        d.setVisible(true);
    }

    /**
     * Closes window, no changes saved.
     */
    public void closeWindow() {
        setVisible(false);
    }

}

