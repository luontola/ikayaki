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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.TooManyListenersException;
import java.util.Vector;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Calendar;

/**
 * This class represents hardware layer to serial port communications.
 *
 * @author Aki Sysmäläinen, Aki Korpua (co)
 */
public class SerialIO implements SerialPortEventListener {
/*
Event A: On new SerialPortEvent - generates new SerialMessageArrivedEvent if a data
message from serial port is received.
*/

  private static final boolean DEBUG = true; // Writes log-file

  private static Vector<SerialIO> openPorts = new Vector<SerialIO> ();

  /**
   * Listeners for this port.
   */
  private EventListenerList listenerList = new EventListenerList();

  private SerialPort sPort;

  private OutputStream os;
  private InputStream is;

  private String portName;
  private BufferedWriter logWriter;

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
        if(DEBUG) {
          Calendar now = Calendar.getInstance();
          int y = now.get(Calendar.YEAR), m = now.get(Calendar.MONTH) + 1, d = now.get(Calendar.DAY_OF_MONTH);
          File file = new File(Ikayaki.DEBUG_LOG_DIR, y + "-" + (m < 10 ? "0" : "") + m + "-" +
                               (d < 10 ? "0" : "") + d + "-" + parameters.getPortName() + ".log");
          try {
            if (!Ikayaki.DEBUG_LOG_DIR.exists()) Ikayaki.DEBUG_LOG_DIR.mkdir();
            //if (!file.exists()) file.createNewFile(); // not needed
            logWriter = new BufferedWriter(new FileWriter(file, true));
          }
          catch (IOException ex1) {
            System.err.println("Error creating log file '" + file + "': " + ex1);
          }
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

        // open the stream
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

        // Set notifyOnBreakInterrup to allow event driven break handling. Unneccessary?
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
        //System.out.println("Let's try to open port: " + parameters.getPortName());  //TODO debug

        SerialIO newPort = null;

        // Check if given port is already open
        // if it is then return it
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
     * @param message message to be send
     * @throws SerialIOException if exception occurs.
     */
    public void writeMessage(String message) throws SerialIOException {

        byte[] asciiMsg;

        // convert message to ASCII
        try {
            asciiMsg = message.getBytes("US-ASCII"); // TODO is this right??
        } catch (UnsupportedEncodingException e) {
            throw new SerialIOException("ASCII charset not supported");
        }

        // send message to outputstream
        try {
            //System.out.println("Sending data to port: " + this.portName); // TODO debug
            //Shall we wait? No need to shoot them too quick to system
            try {
              Thread.sleep(50);
            }
            catch (InterruptedException ex) {
            }
            os.write(asciiMsg);
             if(DEBUG) {
               logWriter.write("SEND: " + message);
               logWriter.newLine();
               logWriter.flush();
             }
            os.flush(); // TODO is this needed ??
        } catch (IOException e) {
            throw new SerialIOException("Couldn't write to outputstream of" + this.portName);
        }

        return;
    }

    public void closePort() {
        if (sPort != null) {
            this.sPort.close();
            try {
                this.is.close();
                this.os.close();
            } catch (IOException ex) {
                System.err.println("Could not close stream for COM port");
            }
            try {
                this.os.close();
            } catch (IOException ex1) {
                System.err.println("Could not close stream for COM port");
            }
        }
    }


    public static void closeAllPorts() {
        for (int i = 0; i < openPorts.size(); i++) {
            openPorts.elementAt(i).closePort();
        }
    }

    /**
     * This method is run when a serial message is received from serial port. It generates a new SerialIOEvent.
     */
    public void serialEvent(SerialPortEvent event) {
        //System.out.println("New message arrived to port: " + this.portName);

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
                    if ('\r' == (char) newData) {
                        inputBuffer.append('\n');
                    } else {
                        newByte[0] = new Integer(newData).byteValue();
                        inputBuffer.append(new String(newByte, "US-ASCII"));
                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                    return;
                }
            }
            // TODO convert from ASCII to unicode
            //System.out.println("sending: " + new String(inputBuffer)); //debug
            if(DEBUG) {
              try {
                logWriter.write("RECEIVE: " + inputBuffer);
                logWriter.newLine();
                logWriter.flush();
              }
              catch (IOException ex1) {
                System.err.println(ex1);
              }
             }

            fireSerialIOEvent(new String(inputBuffer));
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

}
