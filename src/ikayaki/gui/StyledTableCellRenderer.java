package ikayaki.gui;

import javax.swing.*;
import javax.swing.border.Border;
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
        if (!(value instanceof Wrapper)) {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        Wrapper wrapper = (Wrapper) value;
        super.getTableCellRendererComponent(table, wrapper.value, isSelected, hasFocus, row,
                column);

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

    /**
     * Wrapper class for holding the Object to be rendered and the style parameters.
     */
    public static class Wrapper {

        public Object value;

        /**
         * The value of the horizontalAlignment property, one of the following constants defined in SwingConstants:
         * LEFT, CENTER, RIGHT, LEADING or TRAILING.
         */
        public int horizontalAlignment = SwingConstants.LEADING;

        /**
         * The value of the verticalAlignment property, one of the following constants defined in SwingConstants: TOP,
         * CENTER, or BOTTOM.
         */
        public int verticalAlignment = SwingConstants.CENTER;

        /**
         * If true the component paints every pixel within its bounds. Otherwise, the component may not paint some or
         * all of its pixels, allowing the underlying pixels to show through.
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
         * The background color of this component when it is selected and has focus or null to use the parent's
         * background color.
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

        public Wrapper() {
            // DO NOTHING
        }
    }
}
