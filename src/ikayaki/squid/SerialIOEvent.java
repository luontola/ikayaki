/*
 * SerialIOEvent.java
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

package ikayaki.squid;

import java.util.EventObject;

/**
 * An event that is generated when SerialIO receives data from serial port.
 *
 * @author Aki Sysmäläinen
 */
public class SerialIOEvent extends EventObject {

    /**
     * ASCII message recieved from serial port.
     */
    private String message;

    public SerialIOEvent(Object source, String message) {
        super(source);
        this.message = message;
    }

    /**
     * Returns received serial message.
     *
     * @return The message in ASCII form that was received from serial port.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns received serial message with all '\r' characters removed.
     *
     * @return The message in ASCII form that was received from serial port.
     */
    public String getCleanMessage() {
        String result = "";
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c != '\r') {
                result += c;
            }
        }
        return result;
    }

    /**
     * Returns received serial message with all non-space whitespace replaced with their keycodes.
     *
     * @return The message in ASCII form that was received from serial port.
     */
    public String getLogMessage() {
        String result = "";
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c < ' ') {
                result += "&#" + (int) c + ";";
            } else {
                result += c;
            }

        }
        return result;
    }
}