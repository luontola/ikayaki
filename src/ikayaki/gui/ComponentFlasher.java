/*
* ComponentFlasher.java
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
import java.awt.event.*;

/**
 * Timer used for flashing a JComponent background light red (or given color), for 100 ms (or given time).
 *
 * @author Samuli Kaipiainen
 */
public class ComponentFlasher extends Timer {

    private final JComponent component;
    private final Color componentBG;
    private final Color flashcolor;
    private static final Color defauldFlashColor = new Color(0xff6060);

    public ComponentFlasher(JComponent component) {
        this(component, defauldFlashColor, 100);
    }

    public ComponentFlasher(JComponent component, Color flashcolor) {
        this(component, flashcolor, 100);
    }

    public ComponentFlasher(JComponent component, int flashtime) {
        this(component, defauldFlashColor, flashtime);
    }

    public ComponentFlasher(JComponent component, Color flashcolor, int flashtime) {
        super(flashtime, null);
        this.component = component;
        this.componentBG = component.getBackground();
        this.flashcolor = flashcolor;
        this.setRepeats(false);

        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ComponentFlasher.this.component.setBackground(componentBG);
            }
        });
    }

    public void flash() {
        component.setBackground(flashcolor);
        super.start();
    }
}
