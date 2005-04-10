/*
* SquidEmulator.java
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

/**
 * This class tries to emulate behavior of real squid-system. It starts 3 threads (handler,magnetometer,degausser),
 * opens COM-ports for them and adds SerialIO Listeners. Threads generates random data values or loaded values as
 * results and generates random error situations to see that program using real squid system does survive those. Uses
 * 2-3 COM ports. Usage SquidEmulator x z.. filename where x is 0 or 1 and indicates if Magnetometer and Demagnetizer
 * are on same COM port. z... values are COM ports (Handler,Magnetometer,Degausser). filename is name of log file we are
 * using or it is existing log file, which is used to generate same sequence used to verify that old and new program
 * behaves same way.
 *
 * @author Aki Korpua
 */
public class SquidEmulator {

    /*
  Event A: On New IO Message - reads message and puts it in Buffer
  */

    /**
     * indicates if system have been started
     */
    private static boolean online;

    /**
     * log file we are using read or write
     */
    private static File logFile;

    /**
     * indicates have we loaded log file for using or are we writing it
     */
    private static boolean usingOldLog;

    /**
     * value between 0 and 127 default 5. Settings in the 20-50 range are usually employed.
     */
    private static int acceleration;

    /**
     * value between 0 and 127 default 10. Settings in the 20-50 range are usually employed.
     */
    private static int deceleration;

    /**
     * value between 50 and 12 000. The decimal number issued is 10 times the actual pulse rate to the motor. Since the
     * motor requires 200 pulses (full step) or 400 pulses (half step) per revolution, a speed setting of M10000 sets
     * the motor to revolve at 5 revolutions per second in full step or 2.5 revolutions in half step. This rate is
     * one-half the sample rate rotation due to the pulley ratios. The sample handler is set up at the factory for half
     * stepping.
     */
    private static int velocity;

    /**
     * 5 end of move, previous G command complete, 7 hard limit stop, G motor is currently indexing
     */
    private static String handlerStatus;

    /**
     * value between 1 and 16,777,215
     */
    private static int commandedDistance;

    /**
     * value between 1 and 16,777,215
     */
    private static int currentPosition;

    /**
     * value between 1 and 16,777,215
     */
    private static int homePosition;

    /**
     * angles are between 0 (0) and 2000 (360)
     */
    private static int commandedRotation;

    /**
     * angles are between 0 (0) and 2000 (360)
     */
    private static int currentRotation;

    /**
     * (X, Y, Z) = (0,1,2) default axis Z
     */
    private static int degausserCoil;

    /**
     * 0->3000 default amp 0
     */
    private static int degausserAmplitude;

    /**
     * 1-9 seconds default delay 1 second
     */
    private static int degausserDelay;

    /**
     * (3, 5, 7, 9) default 3
     */
    private static int degausserRamp;

    /**
     * Z=Zero, T=Tracking, ?=Unknown
     */
    private static char degausserStatus;

    /**
     * starts Threads which reads messages from selected COM port. Own listener for each. Offers write commads to port
     * too.
     */
    private static SerialIO handlerPort;
    private static SerialIO magnetometerPort;
    private static SerialIO degausserPort;

    private static FileWriter logWriter = null;

    private static HandlerEmu handler;
    private static MagnetometerEmu magnetometer;
    private static DegausserEmu degausser;

    /**
     * send message to SerialIO to be sented.
     *
     * @param message any message reply we are sending back
     * @param port    port number to be sent
     */
    public static void writeMessage(String message, SerialIO port) {
        //writes log if indicated to do so
        try {
            logWriter.write("SEND:" + message);
        } catch (IOException e) {
            System.err.println("Error on writing log file");
        }
        try {
            port.writeMessage(message);
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
        return;
    }

    /**
     * First creates or loads log file and sets settings. Runs sequence where read data from buffer and run cheduled
     * actions (move, rotate, demag, measure) and send feedback to COM ports.
     */
    public static void main(String[] args) {
        System.out.println("Starting...");
        try {
            int samePort = Integer.parseInt(args[0]);
            handlerPort = SerialIO.openPort(new SerialParameters(args[1]));
            magnetometerPort = SerialIO.openPort(new SerialParameters(args[2]));
            if (samePort == 0) {
                degausserPort = SerialIO.openPort(new SerialParameters(args[3]));
            } else {
                degausserPort = magnetometerPort;
            }
            if (samePort == 0) {
                logFile = new File(args[4]);
            } else {
                logFile = new File(args[5]);
            }
            //Only writing, TODO:switch to reading option
            logWriter = new FileWriter(logFile);
        } catch (SerialIOException e) {
            System.out.println(e);
            return;
        } catch (Exception e) {
            System.out.println("Usage \"java SquidEmulator x z... filename\"");
            return;
        }

        handler = new HandlerEmu();
        handler.start();
        magnetometer = new MagnetometerEmu();
        magnetometer.start();
        degausser = new DegausserEmu();
        degausser.start();

        System.out.println("System running...");

        try {
            //wait for signal to quit 8)
            System.in.read();
        } catch (IOException ex) {
        }

        return;
    }


    /**
     * Runs handler emulation process. Process incoming messages and sends data back. When message comes, process it
     * (wait if needed for a while), updates own status and sends result back.
     */
    private static class HandlerEmu extends Thread implements SerialIOListener {


        //All recieved commands
        private Stack commandStack;

        //remainder of last command (commands are separated with ',')
        private String lastMessagePart;

        public HandlerEmu() {
            //Setlistener to handlePort
            //handlerPort.set
        }

        public void run() {
            // TODO
        }

        public void serialIOEvent(SerialIOEvent event) {
            int i;
            try {
              logWriter.write("HANDLER_RECIEVE:" + event.getMessage());
            }
            catch (IOException ex) {
            }
            String message = lastMessagePart + event.getMessage();
            String[] commands = message.split(",");
            for (i = 0; i < commands.length - 1; i++) {
                commandStack.add(commands[i]);
            }
            lastMessagePart = commands[i + 1];
        }
    }

    /**
     * Runs magnetometer emulation process. Process incoming messages and sends data back. When message comes, process
     * it (wait if needed for a while), updates own status and sends result back.
     */
    private static class MagnetometerEmu extends Thread implements SerialIOListener {
        //All recieved commands
        private Stack commandStack;

        public void run() {
            // TODO
        }

        public void serialIOEvent(SerialIOEvent event) {
          try {
            logWriter.write("MAGNETOMETER_RECIEVE:" + event.getMessage());
          }
          catch (IOException ex) {
          }

            String message = event.getMessage();
            String[] commands = message.split("/r");
            //we only accept first "part". And only if it starts with A,X,Y,Z
            if (commands[0].charAt(0) == 'A' || commands[0].charAt(0) == 'X' || commands[0].charAt(0) == 'Y' || commands[0].charAt(
                    0) == 'Z') {
                commandStack.add(commands[0]);
            }
        }
    }

    /**
     * Runs degausser emulation process. Process incoming messages and sends data back. When message comes, process it
     * (wait if needed for a while), updates own status and sends result back.
     */
    private static class DegausserEmu
            extends Thread implements SerialIOListener {

        //All recieved commands
        private Stack commandStack;

        public void run() {
            // TODO
        }

        public void serialIOEvent(SerialIOEvent event) {
          try {
            logWriter.write("DEGAUSSER_RECIEVE:" + event.getMessage());
          }
          catch (IOException ex) {
          }

            String message = event.getMessage();
            String[] commands = message.split("/r");
            //we only accept first "part". And only if it starts with D
            if (commands[0].charAt(0) == 'D') {
                commandStack.add(commands[0]);
            }
        }
    }
}
