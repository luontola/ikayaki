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
    private String delimiter;
    private String delimiterRegexp;

    public FittedComboBoxRenderer(JComponent fitToComponent) {
        this.fitToComponent = fitToComponent;
        this.delimiter = "\\";
        this.delimiterRegexp = "\\\\";
    }

    public FittedComboBoxRenderer(JComponent fitToComponent, String delimiter, String regexp) {
        this.fitToComponent = fitToComponent;
        this.delimiter = delimiter;
        this.delimiterRegexp = regexp;
    }

    /* TODO:
     * Tuo nykyinen algoritmi on melko hyv‰ Project Explorerin kansiohistoriaa katsottaessa. Autocompletessa se ei ole
     * niin hyv‰, koska jos on eri pituisia kansioiden nimi‰, niin polku katkaistaan niill‰ eri kohdasta. Jos
     * autocompleten haluaisi hyv‰ksi, niin pit‰isi ennen valikon avaamista laskea ett‰ kuinka paljon enimmill‰‰n
     * p‰tkit‰‰n ja sitten tallettaa se muistiin t‰m‰n rendererin k‰ytett‰v‰ksi.
     */

    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                            boolean cellHasFocus) {
        JLabel comp = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        int maxWidth = fitToComponent.getWidth();

        // split the text and take out parts of it until it fits
        String[] text = value.toString().split(delimiterRegexp);
        if (maxWidth <= comp.getPreferredSize().width && text.length >= 3) {
            boolean shortenMore = true;
            while (shortenMore) {

                // take out one part of the text
                shortenMore = false;
                for (int i = 1; i < text.length - 1; i++) {
                    if (text[i] != null) {
                        text[i] = null;
                        shortenMore = true;
                        break;
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
                comp.setText(result);
                if (maxWidth > comp.getPreferredSize().width) {
                    shortenMore = false;
                }
            }
        }
        return comp;
    }
}
