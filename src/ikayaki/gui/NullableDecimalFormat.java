/*
 * NullableDecimalFormat.java
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
import java.text.ParseException;

/**
 * Decimal format that accepts an empty string. An empty string equals null.
 * <p/>
 * If this class is used, it is not possible to use NumberFormatter's setMinimum and setMaximum values, because it will
 * create a NullPointerException.
 *
 * @author Esko Luontola
 */
public class NullableDecimalFormat extends DecimalFormat {

    @Override public Object parseObject(String source) throws ParseException {
        if (source.equals("")) {
            return null;
        }
        return super.parseObject(source);
    }
}
