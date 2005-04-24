/*
 * AbstractPlot.java
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
 * Abstract class that implements general construction of a graphical plot.
 *
 * @author Aki Sysmäläinen
 */
public abstract class AbstractPlot extends JPanel implements Plot {


    /**
     * Painter method
     */
    @Override public void paint(Graphics g) {
        Dimension d = getSize();
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        render(d.width, d.height, g2);
        g2.dispose();
    }

    /**
     * Classes extending this class must implement this
     *
     * @param w
     * @param h
     * @param g2
     */
    public abstract void render(int w, int h, Graphics2D g2);

}