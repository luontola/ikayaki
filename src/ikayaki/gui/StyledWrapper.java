/*
 * StyledWrapper.java
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
import javax.swing.border.Border;
import java.awt.*;

/**
 * Wrapper class for holding the value to be rendered and its style parameters. Used by StyledTableCellRenderer and
 * StyledCellEditor.
 *
 * @author Esko Luontola
 */
public class StyledWrapper {

    /**
     * The wrapped value.
     */
    public Object value;

    /**
     * The value of the horizontalAlignment property, one of the following constants defined in SwingConstants: LEFT,
     * CENTER, RIGHT, LEADING or TRAILING.
     */
    public int horizontalAlignment = SwingConstants.LEADING;

    /**
     * The value of the verticalAlignment property, one of the following constants defined in SwingConstants: TOP,
     * CENTER, or BOTTOM.
     */
    public int verticalAlignment = SwingConstants.CENTER;

    /**
     * If true the component paints every pixel within its bounds. Otherwise, the component may not paint some or all of
     * its pixels, allowing the underlying pixels to show through.
     */
    public boolean opaque = true;

    /**
     * The border of this component or null if no border is currently set.
     */
    public Border border = null;

    /**
     * The border of this component when it is selected or null to use the default border.
     */
    public Border selectedBorder = null;

    /**
     * The border of this component when it has focus or null to use the default border.
     */
    public Border focusBorder = null;

    /**
     * The border of this component when it is selected and has focus or null to use the default border.
     */
    public Border selectedFocusBorder = null;

//        public Insets insets = null; // TODO: is this also necessary?

    /**
     * The background color of this component or null to use the parent's background color.
     */
    public Color background = null;

    /**
     * The background color of this component when it is selected or null to use the parent's background color.
     */
    public Color selectedBackground = null;

    /**
     * The background color of this component when it has focus or null to use the parent's background color.
     */
    public Color focusBackground = null;

    /**
     * The background color of this component when it is selected and has focus or null to use the parent's background
     * color.
     */
    public Color selectedFocusBackground = null;

    /**
     * The foreground color of this component or null to use the parent's foreground color.
     */
    public Color foreground = null;

    /**
     * The font of this component or null to use the parent's font.
     */
    public Font font = null;
}
