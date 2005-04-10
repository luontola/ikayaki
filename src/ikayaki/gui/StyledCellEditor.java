/*
 * StyledCellEditor.java
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
 * CellEditor to compliment StyledTableCellRenderer. Applies the horizontalAlignment, foreground and font styles to the
 * component returned by another cell editor. Unless otherwise specified, uses a DefaultCellEditor.
 *
 * @author Esko Luontola
 */
public class StyledCellEditor extends DefaultCellEditor {

    /**
     * Constructs a <code>StyledCellEditor</code> that uses a text field.
     *
     * @param textField a <code>JTextField</code> object
     */
    public StyledCellEditor(final JTextField textField) {
        super(textField);
    }

    /**
     * Constructs a <code>StyledCellEditor</code> object that uses a check box.
     *
     * @param checkBox a <code>JCheckBox</code> object
     */
    public StyledCellEditor(final JCheckBox checkBox) {
        super(checkBox);
    }

    /**
     * Constructs a <code>StyledCellEditor</code> object that uses a combo box.
     *
     * @param comboBox a <code>JComboBox</code> object
     */
    public StyledCellEditor(final JComboBox comboBox) {
        super(comboBox);
    }

    /**
     * Implements the <code>TreeCellEditor</code> interface.
     */
    @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
                                                          boolean expanded, boolean leaf, int row) {
        // get the component as made by the default editor
        if (!(value instanceof StyledWrapper)) {
            return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        }
        StyledWrapper wrapper = (StyledWrapper) value;
        Component comp = super.getTreeCellEditorComponent(tree, wrapper.value, isSelected, expanded, leaf, row);

        // apply custom style to the component
        if (comp instanceof JTextField) {
            JTextField textField = (JTextField) comp;
            textField.setHorizontalAlignment(wrapper.horizontalAlignment);
            if (wrapper.foreground != null) textField.setForeground(wrapper.foreground);
            if (wrapper.font != null) textField.setFont(wrapper.font);

        } else if (comp instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) comp;
            checkBox.setHorizontalAlignment(wrapper.horizontalAlignment);
            if (wrapper.foreground != null) checkBox.setForeground(wrapper.foreground);
            if (wrapper.font != null) checkBox.setFont(wrapper.font);

        } else if (comp instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) comp;
            if (wrapper.foreground != null) comboBox.setForeground(wrapper.foreground);
            if (wrapper.font != null) comboBox.setFont(wrapper.font);
        }
        return comp;
    }

    /**
     * Implements the <code>TableCellEditor</code> interface.
     */
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                           int column) {
        // get the component as made by the default editor
        if (!(value instanceof StyledWrapper)) {
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }
        StyledWrapper wrapper = (StyledWrapper) value;
        Component comp = super.getTableCellEditorComponent(table, wrapper.value, isSelected, row, column);

        // apply custom style to the component
        if (comp instanceof JTextField) {
            JTextField textField = (JTextField) comp;
            textField.setHorizontalAlignment(wrapper.horizontalAlignment);
            if (wrapper.foreground != null) textField.setForeground(wrapper.foreground);
            if (wrapper.font != null) textField.setFont(wrapper.font);

        } else if (comp instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) comp;
            checkBox.setHorizontalAlignment(wrapper.horizontalAlignment);
            if (wrapper.foreground != null) checkBox.setForeground(wrapper.foreground);
            if (wrapper.font != null) checkBox.setFont(wrapper.font);

        } else if (comp instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) comp;
            if (wrapper.foreground != null) comboBox.setForeground(wrapper.foreground);
            if (wrapper.font != null) comboBox.setFont(wrapper.font);
        }
        return comp;
    }
}
