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

import javax.comm.SerialPort;

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

    /**
     * Creates a SerialParameter object with default Serial settings for serial port communication.
     * Default settings are:
     *  Baudrate: 9600
     *  Flowcontrol in: None
     *  Flowcontrol out: None
     *  Databits: 8
     *  Stopbits: 1
     *  Parity: None
     *
     * @param portName       The name of the serial port.
     */
    public SerialParameters(String portName) {
        this.portName = portName;
        this.baudRate = 9600;
        this.flowControlIn = SerialPort.FLOWCONTROL_NONE;
        this.flowControlOut = SerialPort.FLOWCONTROL_NONE;
        this.databits = SerialPort.DATABITS_8;
        this.stopbits = SerialPort.STOPBITS_1;
        this.parity = SerialPort.PARITY_NONE;
    }

    public String getPortName() {
        return portName;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getFlowControlIn() {
        return flowControlIn;
    }

    public int getFlowControlOut() {
        return flowControlOut;
    }

    public int getDatabits() {
        return databits;
    }

    public int getStopbits() {
        return stopbits;
    }

    public int getParity() {
        return parity;
    }
}