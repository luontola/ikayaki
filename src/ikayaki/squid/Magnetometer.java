/*
* Magnetometer.java
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
 * Offers an interface for controlling the magnetometer."
 *
 * @author
 */
public class Magnetometer implements SerialIOListener {
/*
Event A: On SerialIOEvent - reads the message and puts it in a buffer
*/

    /**
     * Buffer for incoming messages, readed when needed.
     */
    private Stack messageBuffer;

    /**
     * Magnetometer’s current status.
     */
    private String status;

    /**
     * COM port for communication.
     */
    private SerialIO serialIO;

    /**
     * Creates a new magnetometer interface. Opens connection to Magnetometer COM port (if its not open already) and
     * reads settings from the Setting class.
     */
    public Magnetometer() {
        return; // TODO
    }

    /**
     * Checks which settings have changed and updates the magnetometer interface. This method will be called by the
     * Squid class.
     */
    public void updateSettings() {
        return; // TODO
    }

    /**
     * Reset settings for axis.
     *
     * @param axis x,y,z or a (all). In lower case.
     */
    private String reset(char axis) {
        return null; // TODO
    }

    /**
     * Reset counter for axis.
     *
     * @param axis x,y,z or a (all). In lower case.
     */
    private void resetCounter(char axis) {
        return; // TODO
    }

    /**
     * Used for configuring Magnetometer parameters. See subcommand for usages.
     *
     * @param axis       x,y,z or a (all)
     * @param subcommand The CONFIGURE subcommands follow: <br/>"F" Set filter configuration. The data subfield sets the
     *                   filter to the indicated range. The four possible data values are: "1" One Hertz Filter; 1 Hz
     *                   "T" Ten Hertz Filter; 10 Hz "H" One hundred Hertz Filter; 100 Hz "W" Wide band filter; WB
     *                   <br/>"R" Set DC SQUID electronic range. The data subfield selects the range desired. The four
     *                   possible data values are: "1" One time range; 1x "T" Ten times range; 10x "H" One hundred times
     *                   range; 100x "E" Extended range; 1000x <br/>"S" Set/Reset the fast-slew option. Two data values
     *                   are possible: "E" Enable the fast-slew; turn it on. "D" Disable the fast-slew; turn it off.
     *                   <br/>"L" This subcommand opens or closes the SQUID feedback loop or resets the analog signal to
     *                   +/- 1/2 flux quantum about zero. The three possible data values are: "O" Open the feedback
     *                   loop. (This command also zeros the flux counter) "C" Close the feedback loop. "P" Pulse-reset
     *                   (open then close) the feedback loop. (This command also zeros the flux counter)
     * @param option     see data values from subcommands.
     */
    private void configure(char axis, char subcommand, char option) {
        return; // TODO
    }

    /**
     * axis is x,y,x or a (all).
     *
     * @param axis x,y,z or a (all). In lower case.
     */
    private void latchAnalog(char axis) {
        return; // TODO
    }

    /**
     * axis is x,y,x or a (all).
     *
     * @param axis x,y,z or a (all). In lower case.
     */
    private void latchCounter(char axis) {
        return; // TODO
    }

    /**
     * Generic send message sender, use with caution and knowledge. Checks if commands are good.
     *
     * @param axis       x,y,z. In lower case.
     * @param command    "D" Send back the analog data last captured with the LATCH command. The data field is not
     *                   required. <br/>"C" Send back the counter value last captured with the LATCH command. The data
     *                   field is not required. <br/>"S" Send back status. Various pieces of status can be sent by the
     *                   magnetometer electronics.
     * @param datavalues Datavalues one or more: <br/>"A" Send back all status. <br/>"F" Send back all filter status.
     *                   <br/>"R" Send back all range status. <br/>"S" Send back slew status. <br/>"L" Send back SQUID
     *                   feedback loop status. Return feedback, waiting time?
     * @return Returns data wanted, see command and datavalue
     */
    private String getData(char axis, char command, String datavalues) {
        return null; // TODO
    }

    /**
     * Pulse reset and opens feedback loop for axis. Need to be done to all axes before measuring.
     *
     * @param axis x,y,z or a (all). In lower case.
     */
    public void openLoop(char axis) {
        return; // TODO
    }

    /**
     * Clears flux counter for axis. Need to be done to all axes before measuring.
     *
     * @param axis x,y,z or a (all). In lower case.
     */
    public void clearFlux(char axis) {
        return; // TODO
    }

    /**
     * Waits for magnetometer to settle down. Blocking.
     */
    public void join() {
        return; // TODO
    }

    /**
     * Calls first openLoop(a) and clearFlux(a). Latches axes, reads counters and analog. Calculates data from them and
     * returns them.
     *
     * @return Returns 3 double values in following order: (x,y,z)
     */
    public Double[] readData() {
        return null; // TODO
    }

    /**
     * Returns filter configurations for all axis. Blocking.
     *
     * @return return filter values for all axis in order (x,y,z). <br/>Values: <br/>"1" One Hertz Filter; 1 Hz <br/>"T"
     *         Ten Hertz Filter; 10 Hz <br/>"H" One hundred Hertz Filter; 100 Hz <br/>"W" Wide band filter; WB
     */
    public char[] getFilters() {
        return null; // TODO
    }

    /**
     * Returns range configurations for all axis. Blocking.
     *
     * @return return filter values for all axis in order (x,y,z). <br/>Values: <br/>"1" One time range; 1x <br/>"T" Ten
     *         times range; 10x <br/>"H" One hundred times range; 100x <br/>"E" Extended range; 1000x
     */
    public char[] getRange() {
        return null; // TODO
    }

    /**
     * Returns Fast Slew options value. Blocking.
     *
     * @return true if Fast Slew is on, false if not
     */
    public boolean getSlew() {
        return false; // TODO
    }

    /**
     * Returns if Loops have been opened on axes. Blocking.
     *
     * @return return Loop status for all axis in order (x,y,z). Values true = on, false = off.
     */
    public boolean[] getLoop() {
        return null; // TODO
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