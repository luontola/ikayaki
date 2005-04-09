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
 * User: ORFJackal Date: 9.4.2005 Time: 18:42:46
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
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row); // TODO
    }

    /**
     * Implements the <code>TableCellEditor</code> interface.
     */
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                                                           int column) {
        return super.getTableCellEditorComponent(table, value, isSelected, row, column); // TODO
    }

}
