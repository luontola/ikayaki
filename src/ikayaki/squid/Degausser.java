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

import java.util.Stack;

/**
 * Offers an interface for controlling the degausser (demagnetizer). Because the data link is implemented in the
 * degausser by a single board computer running a small basic program, the response time of the degausser to commands is
 * slow. This class will make sure that commands are not sent faster than the device can handle.
 *
 * @author
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

    /**
     * Creates a new degausser interface. Opens connection to degausser COM port (if not open yet) and reads settings
     * from the Setting class.
     */
    public Degausser() {
        return; // TODO
    }

    /**
     * Checks which settings have changed and updates the degausser interface. This method will be called by the Squid
     * class.
     */
    public void updateSettings() {
        return; // TODO
    }

    /**
     * Sets coil X,Y,Z.
     *
     * @param coil coil to set on.
     */
    private void setCoil(char coil) {
        return; // TODO
    }

    /**
     * Sets amplitude to ramp, range 0 to 3000.
     *
     * @param amplitude amplitude to demag.
     */
    private void setAmplitude(int amplitude) {
        return; // TODO
    }

    /**
     * Performs Ramp up.
     */
    private void executeRampUp() {
        return; // TODO
    }

    /**
     * Brings Ramp down.
     */
    private void executeRampDown() {
        return; // TODO
    }

    /**
     * Performs Ramp up and down.
     */
    private void executeRampCycle() {
        return; // TODO
    }

    /**
     * Performs full sequence to demagnetize Z coil with the given amplitude. Blocking method.
     *
     * @param amplitude amplitude to demag.
     * @return true if process was sended succesfully, otherwise false.
     */
    public boolean demagnetizeZ(int amplitude) {
        return false; // TODO
    }

    /**
     * Performs full sequence to demagnetize Y (and X) coil with the given amplitude. Blocking method.
     *
     * @param amplitude amplitude to demag.
     * @return true if process was sended succesfully, otherwise false.
     */
    public boolean demagnetizeY(int amplitude) {
        return false; // TODO
    }

    /**
     * Sends status query to degausser and returns answer. Blocking.
     *
     * @return Z=Zero, T=Tracking, ?=Unknown
     */
    public char getRampStatus() {
        return 0; // TODO
    }

    /**
     * Sends ramp query to degausser and returns answer. Blocking.
     *
     * @return 3, 5, 7 or 9
     */
    public int getRamp() {
        return 0; // TODO
    }

    /**
     * Sends delay query to degausser and returns answer. Blocking.
     *
     * @return 1 to 9 as seconds
     */
    public int getDelay() {
        return 0; // TODO
    }

    /**
     * Sends coil query to degausser and returns answer. Blocking.
     *
     * @return X=X Axis, Y=Y Axis, Z=Z Axis, ?=Unknown
     */
    public char getCoil() {
        return 0; // TODO
    }

    /**
     * Sends amplitude query to degausser and returns answer. Blocking.
     *
     * @return 0 to 3000
     */
    public int getAmplitude() {
        return 0; // TODO
    }

    /**
     * Checks if connection is ok.
     *
     * @return true if ok.
     */
    public boolean isOK() {
        return false; // TODO
    }

    public void serialIOEvent(SerialIOEvent event) {
        // TODO
    }
}