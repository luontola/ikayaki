/*
 * FittedComboBoxRenderer.java
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
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

/**
 * Fits the contents of a ComboBox list to a components width by shortening the text. Especially useful for showing long
 * file paths in a narrow list.
 *
 * @author Esko Luontola
 */
class FittedComboBoxRenderer extends BasicComboBoxRenderer {

    private JComponent fitToComponent;
    private int fitLimit = -1;

    private String delimiter;
    private String delimiterRegexp;

    /**
     * Creates a FittedComboBoxRenderer that will fit the list items to the width of a component. The list items' string
     * values will be split using the "\" character.
     *
     * @param fitToComponent the component to whose width the list items will be fit to.
     * @throws NullPointerException if fitToComponent is null.
     */
    public FittedComboBoxRenderer(JComponent fitToComponent) {
        if (fitToComponent == null) {
            throw new NullPointerException();
        }
        this.fitToComponent = fitToComponent;
        this.delimiter = "\\";
        this.delimiterRegexp = "\\\\";
    }

    /**
     * Creates a FittedComboBoxRenderer that will fit the list items to the width of a component. The list items' string
     * values will be split using the specified pattern.
     *
     * @param fitToComponent the component to whose width the list items will be fit to.
     * @param delimiter      the string with which to join the parts after they have been split.
     * @param regexp         a regular expression of the delimiter with which to split the text into parts.
     * @throws NullPointerException if any of the parameters is null.
     */
    public FittedComboBoxRenderer(JComponent fitToComponent, String delimiter, String regexp) {
        if (fitToComponent == null || delimiter == null || regexp == null) {
            throw new NullPointerException();
        }
        this.fitToComponent = fitToComponent;
        this.delimiter = delimiter;
        this.delimiterRegexp = regexp;
    }

    /**
     * Returns the number of parts that will be chopped of the text, or -1 if it is being detected automatically.
     */
    public int getFitLimit() {
        return fitLimit;
    }

    /**
     * Sets the number of parts that should be chopped of the text.
     *
     * @param fitLimit a fixed number of parts to chop off, or -1 to detected it automatically.
     */
    public void setFitLimit(int fitLimit) {
        this.fitLimit = fitLimit;
    }

    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                            boolean cellHasFocus) {
        JLabel comp = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (fitLimit >= 0) {
            fitValue(value, fitLimit);
        } else {
            fitValue(value);
        }
        return comp;
    }

    /**
     * Fits the specified object to this component. After this method call the possibly shortened string value of the
     * object will be the text in this renderer component. Tells how much had to be removed from the string value before
     * it did fit.
     *
     * @param value the object whose toString() value to fit into this renderer component.
     * @return the number of parts that were chopped off the value.
     */
    public int fitValue(Object value) {
        return fitValue(value, -1);
    }

    /**
     * Fits the specified object to this component. After this method call the possibly shortened string value of the
     * object will be the text in this renderer component. Tells how many parts were removed from the text.
     *
     * @param value    the object whose toString() value to fit into this renderer component.
     * @param fitLimit the fixed number parts to chop off the value, or -1 to detect it automatically.
     * @return the number of parts that were chopped off the value.
     */
    public int fitValue(Object value, int fitLimit) {
        int maxWidth = fitToComponent.getWidth();
        int fitCount = 0;

        // set the text and split it
        this.setText(value.toString());
        String[] text = value.toString().split(delimiterRegexp);
        if (text.length < 3) {
            return 0;               // unable to chop any smaller
        }
        if (fitLimit < 0) {
            // autodetecting enabled
            if (maxWidth > this.getPreferredSize().width) {
                return 0;           // it already fits
            }
        } else if (fitLimit == 0) {
            return 0;               // forbidden to chop any smaller
        }

        // take out parts of the text it until it fits
        boolean shortenMore = true;
        while (shortenMore) {
            shortenMore = false;    // stop the loop, unless somebody tells us to continue

            if (fitLimit >= 0) {
                // take the specified number of parts out of the text
                for (int i = 1; i < text.length - 1 && fitLimit > 0; i++, fitLimit--) {
                    text[i] = null;
                    fitCount++;
                }
            } else {
                // take out one part of the text at a time
                for (int i = 1; i < text.length - 1; i++) {
                    if (text[i] != null) {
                        text[i] = null;
                        shortenMore = true;     // try again if it doesn't fit
                        fitCount++;
                        break;
                    }
                }
            }

            // put the text together
            String result = text[0] + delimiter + "...";
            for (int i = 1; i < text.length; i++) {
                if (text[i] != null) {
                    result += delimiter + text[i];
                }
            }

            // try if it fits
            this.setText(result);
            if (maxWidth > this.getPreferredSize().width) {
                shortenMore = false;
            }
        }
        return fitCount;
    }
}
