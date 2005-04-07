/*
* Degausser.java
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

import ikayaki.Settings;
import java.util.Stack;
import java.util.concurrent.SynchronousQueue;

/**
 * Offers an interface for controlling the degausser (demagnetizer). Because the data link is implemented in the
 * degausser by a single board computer running a small basic program, the response time of the degausser to commands is
 * slow. This class will make sure that commands are not sent faster than the device can handle.
 *
 * @author Aki Korpua
 */
public class Degausser implements SerialIOListener {
/*
Event A: On SerialIOEvent - reads the message and puts it in a buffer
*/

    /**
     * buffer for incoming messages, readed when needed.
     */
    private Stack messageBuffer;

    /**
     * Synchronous queue for waiting result message from degausser
     */
    private SynchronousQueue queue;

    /**
     * Degaussers current status
     */
    private String status;

    /**
     * COM port for communication.
     */
    private SerialIO serialIO;

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

    private long lastCommandTime;

    private boolean waitingForMessage = false;

    /**
     * Creates a new degausser interface. Opens connection to degausser COM port (if not open yet) and reads settings
     * from the Setting class.
     */
    public Degausser() throws SerialIOException {
        this.serialIO = new SerialIO(new SerialParameters(Settings.instance().
                    getDegausserPort()));
        this.degausserDelay = Settings.instance().getDegausserDelay();
        this.degausserRamp = Settings.instance().getDegausserRamp();
        lastCommandTime = System.currentTimeMillis();
        //needs to call new functions setDelay() and setRamp(). TODO
        waitSecond();
        try {
            this.serialIO.writeMessage("DCD " + this.degausserDelay);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
        waitSecond();
        try {
            this.serialIO.writeMessage("DCR " + this.degausserRamp);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
    }

    /**
     * Checks which settings have changed and updates the degausser interface. This method will be called by the Squid
     * class.
     */
    public void updateSettings() {
        // No check, only two options. Doesnt matter.
        this.degausserDelay = Settings.instance().getDegausserDelay();
        this.degausserRamp = Settings.instance().getDegausserRamp();
        waitSecond();
        try {
            this.serialIO.writeMessage("DCD " + this.degausserDelay);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
        waitSecond();
        try {
            this.serialIO.writeMessage("DCR " + this.degausserRamp);
        } catch (SerialIOException ex1) {
            System.err.println("Error using port in degausser:" + ex1);
        }
    }

    /**
     * Sets coil X,Y,Z.
     *
     * @param coil coil to set on.
     */
    private void setCoil(char coil) {
        waitSecond();
        if(coil == 'X' || coil == 'Y' || coil == 'X') {
            try {
                this.serialIO.writeMessage("DCC " + coil);
            } catch (SerialIOException ex) {
            }
        }
    }

    /**
     * Sets amplitude to ramp, range 0 to 3000.
     *
     * @param amplitude amplitude to demag.
     */
    private void setAmplitude(int amplitude) {
        waitSecond();
        if(amplitude>=0 && amplitude<=3000) {
            try {
                if (amplitude < 10)
                    this.serialIO.writeMessage("DCA 000" + amplitude);
                else if (amplitude < 100)
                    this.serialIO.writeMessage("DCA 00" + amplitude);
                else if (amplitude < 1000)
                    this.serialIO.writeMessage("DCA 0" + amplitude);
            } catch (SerialIOException ex) {
            }
        }
    }

    /**
     * Performs Ramp up. If this is used, make sure you Ramp down in less than 10 seconds because it can damage coil
     */
    private void executeRampUp() {
        waitSecond();
        try {
            this.serialIO.writeMessage("DERU");
        } catch (SerialIOException ex) {
        }
    }

    /**
     * Brings Ramp down.
     */
    private void executeRampDown() {
        waitSecond();
        try {
            this.serialIO.writeMessage("DERD");
        } catch (SerialIOException ex) {
        }
    }

    /**
     * Performs Ramp up and down.
     */
    private void executeRampCycle() {
        waitSecond();
        try {
            this.serialIO.writeMessage("DERC");
        } catch (SerialIOException ex) {
        }
    }

    /**
     *
     * Waits 1 second between command, neccessary because Degausser is slow :)
     *
     */
    private void waitSecond() {
        long waitTime = 1000 - (System.currentTimeMillis() - lastCommandTime);
        if(waitTime>0) {
            try
            {
                Thread.sleep(waitTime);
            }
            catch(InterruptedException e) { }
        }
        lastCommandTime = System.currentTimeMillis();
    }

    /**
     * Performs full sequence to demagnetize Z coil with the given amplitude. Blocking method.
     *
     * @param amplitude amplitude to demag.
     * @return true if process was sended succesfully, otherwise false.
     */
    public boolean demagnetizeZ(int amplitude) {
        this.setCoil('Z');
        this.setAmplitude(amplitude);
        this.executeRampCycle();
        //we need to poll for DONE message or TRACK ERROR message
        waitingForMessage = true;
        String answer = (String)queue.poll();
        waitingForMessage = false;
        if(answer.equals("DONE"))
            return true;
        else
            return false;

    }

    /**
     * Performs full sequence to demagnetize Y (and X) coil with the given amplitude. Blocking method.
     *
     * @param amplitude amplitude to demag.
     * @return true if process was sended succesfully, otherwise false.
     */
    public boolean demagnetizeY(int amplitude) {
        this.setCoil('Y');
        this.setAmplitude(amplitude);
        this.executeRampCycle();
        //we need to poll for DONE message or TRACK ERROR message
        waitingForMessage = true;
        String answer = (String)queue.poll();
        waitingForMessage = false;
        if(answer.equals("DONE"))
            return true;
        else
            return false;
    }

    /**
     * Sends status query to degausser and returns answer. Blocking.
     *
     * @return Z=Zero, T=Tracking, ?=Unknown
     */
    public char getRampStatus() {
        try {
            this.serialIO.writeMessage("DSS");
        } catch (SerialIOException ex) {
        }
        waitingForMessage = true;
        String answer = (String)queue.poll();
        waitingForMessage = false;
        return answer.charAt(1);
    }

    /**
     * Sends ramp query to degausser and returns answer. Blocking.
     *
     * @return 3, 5, 7 or 9
     */
    public int getRamp() {
        try {
            this.serialIO.writeMessage("DSS");
        } catch (SerialIOException ex) {
        }
        waitingForMessage = true;
        String answer = (String)queue.poll();
        waitingForMessage = false;
        return (int)answer.charAt(4);

    }

    /**
     * Sends delay query to degausser and returns answer. Blocking.
     *
     * @return 1 to 9 as seconds
     */
    public int getDelay() {
        try {
            this.serialIO.writeMessage("DSS");
        } catch (SerialIOException ex) {
        }
        waitingForMessage = true;
        String answer = (String)queue.poll();
        waitingForMessage = false;
        return (int)answer.charAt(7);

    }

    /**
     * Sends coil query to degausser and returns answer. Blocking.
     *
     * @return X=X Axis, Y=Y Axis, Z=Z Axis, ?=Unknown
     */
    public char getCoil() {
        try {
            this.serialIO.writeMessage("DSS");
        } catch (SerialIOException ex) {
        }
        waitingForMessage = true;
        String answer = (String)queue.poll();
        waitingForMessage = false;
        return answer.charAt(10);

    }

    /**
     * Sends amplitude query to degausser and returns answer. Blocking.
     *
     * @return 0 to 3000
     */
    public int getAmplitude() {
        try {
             this.serialIO.writeMessage("DSS");
         } catch (SerialIOException ex) {
         }
         waitingForMessage = true;
         String answer = (String)queue.poll();
         waitingForMessage = false;

         return Integer.parseInt(answer.substring(13,17));
    }

    /**
     * Checks if connection is ok.
     *
     * @return true if ok.
     */
    public boolean isOK() {
        if(serialIO != null)
            return true;
        else
            return false;
    }

    public void serialIOEvent(SerialIOEvent event) {
        //TODO: problem when Degausser and Magnetometer uses same port :/
        if(waitingForMessage) {
            try {
                queue.put(event.getMessage());
            }
            catch (InterruptedException e) {
                System.err.println("Interrupted Degausser message event");
            }
            catch (NullPointerException e) {
                System.err.println("Null from SerialEvent in Degausser");
            }
        }
        messageBuffer.add(event.getMessage());
    }
}
