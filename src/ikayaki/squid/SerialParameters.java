/*
* SerialParameters.java
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

/**
 * Contains all the serial communication parameters which SerialIO uses when opening the port.
 *
 * @author
 */
public class SerialParameters {

    /**
     * The name of the serial port.
     */
    private String portName;

    /**
     * The baud rate.
     */
    private int baudRate;

    /**
     * Type of flow control for receiving.
     */
    private int flowControlIn;

    /**
     * Type of flow control for sending.
     */
    private int flowControlOut;

    /**
     * The number of data bits.
     */
    private int databits;

    /**
     * The number of stop bits.
     */
    private int stopbits;

    /**
     * The type of parity.
     */
    private int parity;

    /**
     * Creates a SerialParameter object containing settings for serial port communication.
     *
     * @param portName       The name of the serial port.
     * @param baudRate       The baud rate.
     * @param flowControlIn  Type of flow control for receiving.
     * @param flowControlOut Type of flow control for sending.
     * @param databits       The number of data bits.
     * @param stopbits       The number of stop bits.
     * @param parity         The type of parity.
     */
    public SerialParameters(String portName, int baudRate, int flowControlIn, int flowControlOut,
                            int databits, int stopbits, int parity) {
        this.portName = portName;
        this.baudRate = baudRate;
        this.flowControlIn = flowControlIn;
        this.flowControlOut = flowControlOut;
        this.databits = databits;
        this.stopbits = stopbits;
        this.parity = parity;
    }
}