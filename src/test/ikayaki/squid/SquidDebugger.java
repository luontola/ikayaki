/*
 * SquidDebugger.java
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

package test.ikayaki.squid;

import ikayaki.squid.Squid;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * GUI for using the SQUID Interface's protocol level commands.
 *
 * @author Esko Luontola
 */
public class SquidDebugger extends JFrame {

    private Squid squid;

    public SquidDebugger() throws HeadlessException {
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
        new SquidDebugger();
    }
}
