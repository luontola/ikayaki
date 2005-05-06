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

import ikayaki.Ikayaki;

import javax.comm.*;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.Vector;

/**
 * This class represents hardware layer to serial port communications.
 *
 * @author Aki Sysmäläinen, Aki Korpua
 */
public class SerialIO implements SerialPortEventListener {
/*
Event A: On new SerialPortEvent - generates new SerialMessageArrivedEvent if a data
message from serial port is received.
*/

    private static final boolean DEBUG = false; // Writes log-file
    private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * All opened serial ports
     */
    private static Vector<SerialIO> openPorts = new Vector<SerialIO>();

    /**
     * Listeners for this port.
     */
    private EventListenerList listenerList = new EventListenerList();

    /**
     * This serial port
     */
    private SerialPort sPort;

    /**
     * Outputstream of this port
     */
    private OutputStream os;

    /**
     * Inputstream of this port
     */
    private InputStream is;

    /**
     * Name of this port
     */
    private String portName;

    /**
     * Logwriter buffer
     */
    private BufferedWriter logWriter;

    /**
     * Have we tried to create the log writer? (Don't want to try again if it didn't work.)
     */
    private boolean logWriterTriedCreate = false;

    /**
     * Logwriter event type
     */
    private enum LogEvent {
        SESSION_START, SEND, REVEIVE
    };

    /**
     * Creates an instance of SerialIO which represents one serial port.
     *
     * @param parameters parameters for the serial port being opened.
     * @throws SerialIOException if something goes wrong.
     */
    private SerialIO(SerialParameters parameters) throws SerialIOException {
        CommPortIdentifier portId;
        SerialPort sPort;

        try {
            portId = CommPortIdentifier.getPortIdentifier(parameters.getPortName());
        } catch (NoSuchPortException e) {
            throw new SerialIOException("No such port exists:" + parameters.getPortName());
        }

        // Open the port and give it a timeout of 4 seconds
        try {
            sPort = (SerialPort) portId.open("SerialPort", 4000);
        } catch (PortInUseException e) {
            throw new SerialIOException("The port " + parameters.getPortName() + " is already in use");
        }

        // if debug mode, make own logfile for port
        debug(LogEvent.SESSION_START, sPort.getName());

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

        // open the streams
        try {
            this.os = sPort.getOutputStream();
            this.is = sPort.getInputStream();
        } catch (IOException e) {
            sPort.close();
            throw new SerialIOException("Error opening i/o streams");
        }

        // add this object to be listener for the com port
        try {
            sPort.addEventListener(this);
        } catch (TooManyListenersException ex) {
            throw new SerialIOException("Too many listeners");
        }

        // Set notifyOnDataAvailable to true to allow event driven input.
        sPort.notifyOnDataAvailable(true);

        // Set notifyOnBreakInterrup to allow event driven break handling. //Unneccessary?
        //sPort.notifyOnBreakInterrupt(true);

        // Set receive timeout to allow breaking out of polling loop during
        // input handling.
        try {
            sPort.enableReceiveTimeout(30);
        } catch (UnsupportedCommOperationException e) {
            throw new SerialIOException("Unsupported operation");
        }

        this.sPort = sPort;
        this.portName = sPort.getName();

        return;
    }

    public static SerialIO openPort(SerialParameters parameters) throws SerialIOException {
        //System.out.println("Let's try to open port: " + parameters.getPortName());  //DEBUG

        SerialIO newPort = null;

        // Check if given port is already open
        // if it is then return it instead of creating a new one.
        for (int i = 0; i < openPorts.size(); i++) {
            if (parameters.getPortName().equals(openPorts.elementAt(i).portName)) {
                return openPorts.elementAt(i);
            }
        }
        newPort = new SerialIO(parameters);
        openPorts.add(newPort);

        System.out.println("Port: " + parameters.getPortName() + " opened");
        return newPort;
    }

    /**
     * Writes an ASCII format message to serial port.
     *
     * @param message a message to be send
     * @throws SerialIOException if exception occurs.
     */
    public void writeMessage(String message) throws SerialIOException {

        byte[] asciiMsg;

        // convert a message to ASCII
        try {
            asciiMsg = message.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new SerialIOException("ASCII charset not supported");
        }

        // send a message to outputstream
        try {
            try {
                Thread.sleep(50); // Let's wait a bit so we won't flood the system wtih too many messages
                // 50 msecs seems to work fine with baudrate of 1200..
            } catch (InterruptedException ex) {
            }
            debug(LogEvent.SEND, message);
            os.write(asciiMsg);
            os.flush(); // flush the buffer
        } catch (IOException e) {
            throw new SerialIOException("Couldn't write to outputstream of" + this.portName);
        }

        return;
    }

    /**
     * Closes this serial port and it's streams
     */
    public void closePort() {
        if (sPort != null) {
            this.sPort.close();
            try {
                this.is.close();
            } catch (IOException ex) {
                System.err.println("Could not close inputstream for COM port");
            }
            try {
                this.os.close();
            } catch (IOException ex1) {
                System.err.println("Could not close outputstream for COM port");
            }
        }
    }

    /**
     * Closes all open serialports and their streams
     */
    public static void closeAllPorts() {
        for (int i = 0; i < openPorts.size(); i++) {
            openPorts.elementAt(i).closePort();
        }
    }

    /**
     * This method is run when a serial message is received from serial port. It generates a new SerialIOEvent.
     */
    public void serialEvent(SerialPortEvent event) {
        //System.out.println("New message arrived to port: " + this.portName); //DEBUG
        switch (event.getEventType()) {
        case SerialPortEvent.BI:
        case SerialPortEvent.OE:
        case SerialPortEvent.FE:
        case SerialPortEvent.PE:
        case SerialPortEvent.CD:
        case SerialPortEvent.CTS:
        case SerialPortEvent.DSR:
        case SerialPortEvent.RI:
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;
        case SerialPortEvent.DATA_AVAILABLE:
            StringBuffer inputBuffer = new StringBuffer();
            int newData = 0;
            byte[] newByte = new byte[1];

            while (newData != -1) {
                try {
                    newData = is.read();
                    if (newData == -1) {
                        break;
                    }
//                    if (false && '\r' == (char) newData) {
//                        //inputBuffer.append('\n'); // '\r' chars should be skipped I guess..
//                    } else {
                    newByte[0] = new Integer(newData).byteValue();
                    inputBuffer.append(new String(newByte, "US-ASCII"));
//                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                    return;
                }
            }

            fireSerialIOEvent(new String(inputBuffer));

            //System.out.println("sending: " + new String(inputBuffer)); //debug
            debug(LogEvent.REVEIVE, inputBuffer.toString());

            break;
        }
        return;
    }

    /**
     * Adds a MeasurementListener to the project.
     *
     * @param l the listener to be added.
     */
    public synchronized void addSerialIOListener(SerialIOListener l) {
        listenerList.add(SerialIOListener.class, l);
    }

    /**
     * Removes a MeasurementListener from the project.
     *
     * @param l the listener to be removed
     */
    public synchronized void removeSerialIOListener(SerialIOListener l) {
        listenerList.remove(SerialIOListener.class, l);
    }

    /**
     * Notifies all listeners that have registered for MeasurementEvents.
     *
     * @param message
     */
    private synchronized void fireSerialIOEvent(String message) {
        final SerialIOEvent event = new SerialIOEvent(this, message);
        final SerialIOListener[] listeners = listenerList.getListeners(SerialIOListener.class);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (SerialIOListener l : listeners) {
                    try {
                        l.serialIOEvent(event);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Debug logger.
     *
     * @param e       LogEvent type.
     * @param message String to write; portname if e==SESSION_START.
     */
    private void debug(LogEvent e, String message) {
        // do nothing if debug mode is off
        if (DEBUG == false) return;

        // create new log writer if not yet tried
        if (!logWriterTriedCreate) {
            logWriterTriedCreate = true;

            Calendar now = Calendar.getInstance();
            int y = now.get(Calendar.YEAR), m = now.get(Calendar.MONTH) + 1, d = now.get(Calendar.DAY_OF_MONTH);
            String port = e == LogEvent.SESSION_START ? message : portName;
            File file = new File(Ikayaki.DEBUG_LOG_DIR, y + "-" + padn(m) + "-" + padn(d) + "-" + port + ".log");

            boolean oldFile = file.exists();
            if (!Ikayaki.DEBUG_LOG_DIR.exists()) Ikayaki.DEBUG_LOG_DIR.mkdir();

            try {
                logWriter = new BufferedWriter(new FileWriter(file, true));
                // an empty line after previous session
                if (oldFile) logWriter.newLine();
            } catch (IOException ex1) {
                System.err.println("Error creating log file: " + ex1);
            }
        }

        // no working log writer :(
        if (logWriter == null) return;

        // OK, now write the log message...
        String time = dateFormat.format(new Date());
        try {
            logWriter.write(time + " " + e + ": " + message);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException ex1) {
            System.err.println(ex1);
        }
    }

    /**
     * Zero-paddes a number if it's <10.
     *
     * @param n int to pad.
     * @return padded String.
     */
    private String padn(int n) {
        return (n < 10 ? "0" : "") + n;
    }

    public String getPortName() {
        return portName;
    }
}
