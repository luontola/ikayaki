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
    public SquidEmulator() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
    private static boolean running;

    /**
     * send message to SerialIO to be sented.
     *
     * @param message any message reply we are sending back
     * @param port    port number to be sent
     */
    public static void writeMessage(String message, SerialIO port) {
        //writes log if indicated to do so
        try {
            logWriter.write("SEND:" + message + "\n");
            logWriter.flush();
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
            System.out.println(
                    "Usage \"java SquidEmulator (0:Magnetometer and Degausser use different ports, 1: Magnetometer and Degausser use same port) HandlerPort MagnetometerPort (optional)DegausserPort Log_filename\"");
            return;
        }

        running = true;

        handler = new HandlerEmu();
        handler.start();
        magnetometer = new MagnetometerEmu();
        magnetometer.start();
        degausser = new DegausserEmu();
        degausser.start();

        System.out.println("System running...");
        try {
            logWriter.write("SESSION STARTED:\n");
            logWriter.flush();
        } catch (IOException ex1) {
            System.out.println("Writing error");
        }

        try {
            //wait for signal to quit 8)
            System.in.read();
        } catch (IOException ex) {
        }

        SerialIO.closeAllPorts();
        try {
            logWriter.close();
        } catch (IOException ex2) {
        }

        running = false;

        return;
    }

    private void jbInit() throws Exception {
    }

    /**
     * Runs handler emulation process. Process incoming messages and sends data back. When message comes, process it
     * (wait if needed for a while), updates own status and sends result back.
     */
    private static class HandlerEmu extends Thread implements SerialIOListener {


        //All recieved commands
        private Stack<String> commandStack;

        //remainder of last command (commands are separated with ',')
        private String lastMessagePart = "";

        public HandlerEmu() {
            //Setlistener to handlePort
            handlerPort.addSerialIOListener(this);
            commandStack = new Stack<String>();
        }

        public void run() {
            while (running) {

                if (!commandStack.empty()) {
                    String command = commandStack.pop();
                    if (command.startsWith("V", 0)) {
                        writeMessage("100" + command, handlerPort);
                    } else if (command.startsWith("F", 0) || command.startsWith("%", 0)) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex1) {
                        }
                        writeMessage("perillä" + command, handlerPort);
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }

        public void serialIOEvent(SerialIOEvent event) {
            int i;
            try {
                logWriter.write("HANDLER_RECIEVE:" + event.getCleanMessage() + "\n");
                logWriter.flush();
            } catch (IOException ex) {
            }
            commandStack.add(event.getCleanMessage());
            /*
            String message = lastMessagePart + event.getCleanMessage();
            String[] commands = message.split(",");
            for (i = 0; i < commands.length - 1; i++) {
                commandStack.add(commands[i]);
            }
            if(commands.length == 1)
              commandStack.add(commands[0]);
            else if(commands.length >= i && commands[i] != null)
              lastMessagePart = commands[i];
             */
        }
    }

    /**
     * Runs magnetometer emulation process. Process incoming messages and sends data back. When message comes, process
     * it (wait if needed for a while), updates own status and sends result back.
     */
    private static class MagnetometerEmu extends Thread implements SerialIOListener {
        //All recieved commands
        private Stack<String> commandStack;

        public MagnetometerEmu() {
            //Setlistener to port
            magnetometerPort.addSerialIOListener(this);
            commandStack = new Stack<String>();
        }


        public void run() {
            while (running) {

                if (!commandStack.empty()) {
                    String command = commandStack.pop();
                    if (command.startsWith("SD", 1)) {
                        writeMessage("+" + Math.random(), magnetometerPort);
                    } else if (command.startsWith("SC", 1)) {
                        writeMessage("+" + (int) (Math.random() * 100), magnetometerPort);
                    } else {
                        writeMessage("1", magnetometerPort);
                    }
                    /*
                    else if(command.startsWith("Y",0)) {
                      writeMessage("+" + Math.random(), magnetometerPort);
                    }
                    else if(command.startsWith("Z",0)) {
                      writeMessage("+" + Math.random(), magnetometerPort);
                    }
      */
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }

        }

        public void serialIOEvent(SerialIOEvent event) {
            try {
                logWriter.write("MAGNETOMETER_RECIEVE:" + event.getCleanMessage() + "\n");
                logWriter.flush();
            } catch (IOException ex) {
            }

            String message = event.getCleanMessage();
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
        private Stack<String> commandStack;

        public DegausserEmu() {
            //Setlistener to port
            degausserPort.addSerialIOListener(this);
            commandStack = new Stack<String>();
        }


        public void run() {
            while (running) {

                if (!commandStack.empty()) {
                    String command = commandStack.pop();
                    if (command.startsWith("D", 0)) {
                        writeMessage("DONE", degausserPort);
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }

        }

        public void serialIOEvent(SerialIOEvent event) {
            try {
                logWriter.write("DEGAUSSER_RECIEVE:" + event.getCleanMessage() + "\n");
                logWriter.flush();
            } catch (IOException ex) {
            }

            String message = event.getCleanMessage();
            String[] commands = message.split("/r");
            //we only accept first "part". And only if it starts with D
            if (commands[0].charAt(0) == 'D') {
                commandStack.add(commands[0]);
            }
        }
    }
}
