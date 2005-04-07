/*
* SerialIO.java
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

import javax.comm.*;

/**
 * This class represents hardware layer to serial port communications.
 *
 * @author  Aki Sysmäläinen
 */
public class SerialIO implements SerialPortEventListener {
/*
Event A: On new SerialPortEvent - generates new SerialMessageArrivedEvent if a data
message from serial port is received.
*/

    /**
     * contains last received message from the serial port that this SerialIO represents.
     */
    private String lastMessge;

    /**
     * parameters for serial port
     */
    private SerialParameters parameters;

    private CommPortIdentifier portId;
    private SerialPort sPort;
    private boolean open;

    /**
     * Creates an instance of SerialIO which represents one serial port.
     *
     * @param parameters parameters for the serial port being opened.
     * @throws SerialIOException if something goes wrong.
     */
    public SerialIO(SerialParameters parameters) throws SerialIOException {

        // Check if given port exists, may throw NoSuchPortException
        try {
            portId = CommPortIdentifier.getPortIdentifier(parameters.getPortName());
        } catch (NoSuchPortException e) {
            throw new SerialIOException("No such port exists");
        }

        // Open the port and give it a timeout of 4 seconds
        try {
            sPort = (SerialPort)portId.open("SerialPort", 4000);
        } catch (PortInUseException e) {
            throw new SerialIOException("The port is already in use");
        }

        // Set the parameters of the connection
        try {
	    sPort.setSerialPortParams(parameters.getBaudRate(),
				      parameters.getDatabits(),
				      parameters.getStopbits(),
				      parameters.getParity());
	} catch (UnsupportedCommOperationException e) {
	    sPort.close();
            throw new SerialIOException("Unsupported parameter");
	}

	// Set flow control
	try {
	    sPort.setFlowControlMode(parameters.getFlowControlIn()
			           | parameters.getFlowControlOut());
	} catch (UnsupportedCommOperationException e) {
	    sPort.close();
            throw new SerialIOException("Unsupported flow control");
	}

        // TODO check this method..

        return;
    }

    /**
     * Writes an ASCII format message to serial port.
     *
     * @param message message to be send
     * @throws SerialIOException if exception occurs.
     */
    public void writeMessage(String message) throws SerialIOException {

        // byte[] String.getBytes(String charsetName) to convert to ASCII..
        return; // TODO
    }

    /**
     * Writes an ASCII format message to serial port. SerialIO sends and SerialPortEvent if it gets answer to this
     * message.
     *
     * @return last answer received from serial port or null if no last message is available.
     */
    public String getLastAnswer() {
        return null; // TODO
    }

    /**
     * This method is run when a serial message is received from serial port. It generates a new SerialIOEvent.
     */
    public void serialEvent(SerialPortEvent event) {
        return; // TODO
    }
        
}