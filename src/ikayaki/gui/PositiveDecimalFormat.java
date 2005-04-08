/*
* PositiveDecimalFormat.java
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

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;

/**
 * Decimal format for only positive decimal numbers. Will not show negative numbers. An empty string equals -1.
 *
 * @author Esko Luontola
 */
public class PositiveDecimalFormat extends DecimalFormat {

    @Override public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        if (number < 0.0) {
            return result;
        }
        return super.format(number, result, fieldPosition);
    }

    @Override public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        if (number < 0) {
            return result;
        }
        return super.format(number, result, fieldPosition);
    }

    @Override public Object parseObject(String source) throws ParseException {
        if (source.equals("")) {
            return new Long(-1);
        }
        return super.parseObject(source);
    }
}
