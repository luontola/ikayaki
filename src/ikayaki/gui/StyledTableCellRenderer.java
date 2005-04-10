/*
 * StyledTableCellRenderer.java
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
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Class for rendering individual cells in a JTable with customized colors and fonts.  Applies the style to the JLabel
 * returned by another cell renderer. Unless otherwise specified, uses a DefaultTableCellRenderer.
 *
 * @author Esko Luontola
 */
public class StyledTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * Returns the styled table cell renderer.
     *
     * @param table      the JTable
     * @param value      the value to assign to the cell at [row, column]
     * @param isSelected true if cell is selected
     * @param hasFocus   true if cell has focus
     * @param row        the row of the cell to render
     * @param column     the column of the cell to render
     * @return the styled table cell renderer
     */
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
        // reset style before default renderer
        setBorder(null);
        setBackground(null);
        setForeground(null);
        setFont(null);

        // get the component as rendered by the default renderer
        if (!(value instanceof StyledWrapper)) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        StyledWrapper wrapper = (StyledWrapper) value;
        super.getTableCellRendererComponent(table, wrapper.value, isSelected, hasFocus, row, column);

        // apply custom style to the component
        setHorizontalAlignment(wrapper.horizontalAlignment);
        setVerticalAlignment(wrapper.verticalAlignment);
        setOpaque(wrapper.opaque);
        if (isSelected && hasFocus) {
            if (wrapper.selectedFocusBorder != null) setBorder(wrapper.selectedFocusBorder);
            if (wrapper.selectedFocusBackground != null) setBackground(wrapper.selectedFocusBackground);
        } else if (isSelected) {
            if (wrapper.selectedBorder != null) setBorder(wrapper.selectedBorder);
            if (wrapper.selectedBackground != null) setBackground(wrapper.selectedBackground);
        } else if (hasFocus) {
            if (wrapper.focusBorder != null) setBorder(wrapper.focusBorder);
            if (wrapper.focusBackground != null) setBackground(wrapper.focusBackground);
        } else {
            if (wrapper.border != null) setBorder(wrapper.border);
            if (wrapper.background != null) setBackground(wrapper.background);
        }
        if (wrapper.foreground != null) setForeground(wrapper.foreground);
        if (wrapper.font != null) setFont(wrapper.font);
        return this;
    }
}
