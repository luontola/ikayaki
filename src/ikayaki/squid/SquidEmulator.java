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

/**
 * This class tries to emulate behavior of real squid-system. It starts 3 threads (handler,magnetometer,degausser),
 * opens COM-ports for them and adds SerialIO Listeners. Threads generates random data values or loaded values as
 * results and generates random error situations to see that program using real squid system does survive those. Uses
 * 2-3 COM ports. Usage SquidEmulator x z.. filename where x is 0 or 1 and indicates if Magnetometer and Demagnetizer
 * are on same COM port. z... values are COM ports. filename is name of log file we are using or it is existing log
 * file, which is used to generate same sequence used to verify that old and new program behaves same way.
 *
 * @author
 */
public class SquidEmulator {
/*
Event A: On New IO Message - reads message and puts it in Buffer
*/

    /**
     * indicates if system have been started
     */
    private boolean online;

    /**
     * log file we are using read or write
     */
    private File logFile;

    /**
     * indicates have we loaded log file for using or are we writing it
     */
    private boolean usingOldLog;

    /**
     * value between 0 and 127 default 5. Settings in the 20-50 range are usually employed.
     */
    private int acceleration;

    /**
     * value between 0 and 127 default 10. Settings in the 20-50 range are usually employed.
     */
    private int deceleration;

    /**
     * value between 50 and 12 000. The decimal number issued is 10 times the actual pulse rate to the motor. Since the
     * motor requires 200 pulses (full step) or 400 pulses (half step) per revolution, a speed setting of M10000 sets
     * the motor to revolve at 5 revolutions per second in full step or 2.5 revolutions in half step. This rate is
     * one-half the sample rate rotation due to the pulley ratios. The sample handler is set up at the factory for half
     * stepping.
     */
    private int velocity;

    /**
     * 5 end of move, previous G command complete, 7 hard limit stop, G motor is currently indexing
     */
    private String handlerStatus;

    /**
     * value between 1 and 16,777,215
     */
    private int commandedDistance;

    /**
     * value between 1 and 16,777,215
     */
    private int currentPosition;

    /**
     * value between 1 and 16,777,215
     */
    private int homePosition;

    /**
     * angles are between 0 (0) and 2000 (360)
     */
    private int commandedRotation;

    /**
     * angles are between 0 (0) and 2000 (360)
     */
    private int currentRotation;

    /**
     * (X, Y, Z) = (0,1,2) default axis Z
     */
    private int degausserCoil;

    /**
     * 0->3000 default amp 0
     */
    private int degausserAmplitude;

    /**
     * 1-9 seconds default delay 1 second
     */
    private int degausserDelay;

    /**
     * (3, 5, 7, 9) default 3
     */
    private int degausserRamp;

    /**
     * Z=Zero, T=Tracking, ?=Unknown
     */
    private char degausserStatus;

    /**
     * starts Threads which reads messages from selected COM port. Own listener for each. Offers write commads to port
     * too.
     */
    private SerialIO[] serialIO;

    private HandlerEmu handler = new HandlerEmu();
    private MagnetometerEmu magnetometer = new MagnetometerEmu();
    private DegausserEmu degausser = new DegausserEmu();

    /**
     * send message to SerialIO to be sented.
     *
     * @param message any message reply we are sending back
     * @param port    port number to be sent
     */
    public void writeMessage(String message, int port) {
        return; // TODO
    }

    /**
     * First creates or loads log file and sets settings. Runs sequence where read data from buffer and run cheduled
     * actions (move, rotate, demag, measure) and send feedback to COM ports.
     */
    public static void main(String[] args) {
        return; // TODO
    }

    /**
     * Runs handler emulation process. Process incoming messages and sends data back. When message comes, process it
     * (wait if needed for a while), updates own status and sends result back.
     */
    private class HandlerEmu extends Thread {
        public void run() {
            // TODO
        }
    }

    /**
     * Runs magnetometer emulation process. Process incoming messages and sends data back. When message comes, process
     * it (wait if needed for a while), updates own status and sends result back.
     */
    private class MagnetometerEmu extends Thread {
        public void run() {
            // TODO
        }
    }

    /**
     * Runs degausser emulation process. Process incoming messages and sends data back. When message comes, process it
     * (wait if needed for a while), updates own status and sends result back.
     */
    private class DegausserEmu extends Thread {
        public void run() {
            // TODO
        }
    }
}