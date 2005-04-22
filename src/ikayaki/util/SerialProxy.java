/*
 * SerialProxy.java
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

package ikayaki.util;

import ikayaki.squid.*;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Aki Korpua
 */
public class SerialProxy {

    public static void main(String[] args) {
        SerialIO portOne;
        SerialIO portTwo;

        try {
            portOne = SerialIO.openPort(new SerialParameters(args[0]));
            portTwo = SerialIO.openPort(new SerialParameters(args[1]));
            new Forwarder(portOne, portTwo);
            new Forwarder(portTwo, portOne);

            //wait for signal to quit 8)
            System.in.read();

        } catch (SerialIOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Forwarder implements SerialIOListener {

        private SerialIO out;
        private PrintStream log;
        private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        public Forwarder(SerialIO in, SerialIO out) {
            this(in, out, null);
        }

        public Forwarder(SerialIO in, SerialIO out, PrintStream log) {
            in.addSerialIOListener(this);
            this.out = out;
            this.log = log;
        }

        public void serialIOEvent(SerialIOEvent event) {
            try {
                if (log != null) {
                    log.println(dateFormat.format(new Date()) + "\t" + event.getMessage());
                }
                out.writeMessage(event.getMessage());
            } catch (SerialIOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
