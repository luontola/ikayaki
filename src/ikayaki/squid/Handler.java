/*
* Handler.java
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
 * Offers an interface for controlling the sample handler.
 *
 * @author
 */
public class Handler implements SerialIOListener {
/*
Event A: On SerialIOEvent - reads message and puts it in a buffer
*/

    /**
     * Buffer for incoming messages, readed when needed.
     */
    private Stack messageBuffer;

    /**
     * Handlers current status.
     */
    private String status;

    /**
     * COM port for communication.
     */
    private SerialIO serialIO;

    /**
     * Value between 0 and 127 default 5. Settings in the 20-50 range are usually employed.
     */
    private int acceleration;

    /**
     * Value between 0 and 127 default 10. Settings in the 20-50 range are usually employed.
     */
    private int deceleration;

    /**
     * Value between 50 and 12 000. The decimal number issued is 10 times the actual pulse rate to the motor. Since the
     * motor requires 200 pulses (full step) or 400 pulses (half step) per revolution, a speed setting of M10000 sets
     * the motor to revolve at 5 revolutions per second in full step or 2.5 revolutions in half step. This rate is
     * one-half the sample rate rotation due to the pulley ratios. The sample handler is set up at the factory for half
     * stepping.
     */
    private int velocity;

    /**
     * Speed in measurement, should be small.
     */
    private int measurementVelocity;

    /**
     * 5 end of move, previous G command complete, 7 hard limit stop, G motor is currently indexing.
     */
    private String handlerStatus;

    /**
     * Value between 1 and 16,777,215.
     */
    private int currentPosition;

    /**
     * Value between 1 and 16,777,215.
     */
    private int homePosition;

    /**
     * AF demag position for transverse.
     */
    private int transverseYAFPosition;

    /**
     * Axial AF demag position in steps, must be divisible by 10. Relative to Home.
     */
    private int axialAFPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private int backgroundPosition;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private int measurementPosition;

    /**
     * Angles are between 0 (0) and 2000 (360).
     */
    private int currentRotation;

    /**
     * Creates a new handler interface. Opens connection to handler COM port and reads settings from the Settings
     * class.
     */
    public Handler() {
        return; // TODO
    }

    /**
     * Checks which settings have changed and updates the handler interface. This method will be called by the Squid
     * class.
     */
    public void updateSettings() {
        return; // TODO
    }

    /**
     * Returns current status on Sample Handler.
     *
     * @return 0 Normal, no service required <br/>1 Command error, illegal command sent <br/>2 Range error, an out of
     *         range numeric parameter was sent <br/>3 Command invalid while moving (e.g. G, S, H) <br/>4 Command only
     *         valid in program (e.g. I, U, L) <br/>5 End of move notice, a previous G command is complete <br/>6 End of
     *         wait notice, a previousW command is complete <br/>7 Hard limit stop, the move was stopped by the hard
     *         limit <br/>8 End of program notice, internal program has completed <br/>G Motor is indexing and no other
     *         notice pending.
     */
    public char getStatus() {
        return 0; // TODO
    }

    /**
     * Returns current known position.
     *
     * @return Value between 1 and 16,777,215
     */
    public int getPosition() {
        return 0; // TODO
    }

    /**
     * Returns current known rotation.
     *
     * @return Value between 0 and 2000
     */
    public int getRotation() {
        return 0; // TODO
    }

    /**
     * checks if connection is ok.
     *
     * @return True if ok
     */
    public boolean isOK() {
        return false; // TODO
    }

    /**
     * Commands the holder to move to home position. Only starts movement, needs to poll with join() when movement is
     * finished.
     */
    public void moveToHome() {
        return; // TODO
    }

    /**
     * Commands the holder to move to degauss position. Only starts movement, needs to poll with join() when movement is
     * finished.
     */
    public void moveToDegausser() {
        return; // TODO
    }

    /**
     * Commands the holder to move to measure position. Only starts movement, needs to poll with join() when movement is
     * finished.
     */
    public void moveToMeasurement() {
        return; // TODO
    }

    /**
     * Commands the holder to move to background position. Only starts movement, needs to poll with join() when movement
     * is finished.
     */
    public void moveToBackground() {
        return; // TODO
    }

    /**
     * Commands the holder to move to the specified position. Value must be between 1 and 16,777,215. Return true if
     * good pos-value and moves handler there. Only starts movement, needs to poll with join() when movement is
     * finished.
     *
     * @param pos the position where the handler will move to.
     * @return true if given position was ok, otherwise false.
     */
    public boolean moveToPos(int pos) {
        return false; // TODO
    }

    /**
     * Commands the handler to stop its current job.
     */
    public void stop() {
        return; // TODO
    }

    /**
     * Rotates the handler to the specified angle. If angle is over than 360 or lower than 0, it is divided by 360 and
     * value is remainder. Only starts movement, needs to poll with join() when movement is finished.
     *
     * @param angle the angle in degrees to rotate the handler to.
     */
    public void rotateTo(int angle) {
        return; // TODO
    }

    /**
     * Sends message to handler go online (@0).
     */
    private void setOnline() {
        return; // TODO
    }

    /**
     * Sends message to handler to set acceleration (Aa).
     *
     * @param a Acceleration is a number from 0 to 127
     */
    private void setAcceleration(int a) {
        return; // TODO
    }

    /**
     * Sends message to handler to set deceleration (Dd).
     *
     * @param d Deceleration is a number from 0 to 127
     */
    private void setDeceleration(int d) {
        return; // TODO
    }

    /**
     * Sends message to handler to set base speed. Base rate is the speed at which the motor motion starts and stops.
     * (Bb).
     *
     * @param b Base Speed is pulses per second and has a range of 50 to 5000.
     */
    private void setBaseSpeed(int b) {
        return; // TODO
    }

    /**
     * Sends message to handler to set maximum velocity (Mv).
     *
     * @param v Velocity range is 50 to 20,000
     */
    private void setVelocity(int v) {
        return; // TODO
    }

    /**
     * This command causes the POWER pin to be pulled low within a specified number of ticks after a move of the motor
     * is completed and it will stay low until just prior to the start of the next move command. This command allows the
     * holding torque of the motor to be turned off automatically after a delay for the mechanical system stabilize thus
     * reducing power consumption and allowing the motor to be turned by hand if this feature is required. If the value
     * is zero then the power is left on forever. (CH h).
     *
     * @param h value from 0 to 127 representing the number of clock ticks to leave power on the motor after a move.
     */
    private void setHoldTime(int h) {
        return; // TODO
    }

    /**
     * numbers. The crystal frequency is used by the chip for setting the base speed and maximum speed and for
     * controlling the time for the wait command. (CX cf).
     *
     * @param cf frequence range is 4,000,000 to 8,000.000
     */
    private void setCrystalFrequence(int cf) {
        return; // TODO
    }

    /**
     * This command stops execution of the internal program if it is used in the program. If the motor is indexing it
     * will ramp down and then stop. Use this command to stop the motor after issuing a slew command. (Q).
     */
    private void stopExecution() {
        return; // TODO
    }

    /**
     * Slew the motor up to maximum speed and continue until reaching a hard limit switch or receiving a quit (Q)
     * command. (S).
     */
    private void performSlew() {
        return; // TODO
    }

    /**
     * Set the motor direction of movement to positive. (+).
     */
    private void setMotorPositive() {
        return; // TODO
    }

    /**
     * Set the motor direction of movement to negative. (-).
     */
    private void setMotorNegative() {
        return; // TODO
    }

    /**
     * Set the number of steps to move for the G command. (N s).
     *
     * @param s steps range is 0 to 16,777,215
     */
    private void setSteps(int s) {
        return; // TODO
    }

    /**
     * Set absolute position to move for the G command. (P p).
     *
     * @param p position range is 0 to 16,777,215
     */
    private void setPosition(int p) {
        return; // TODO
    }

    /**
     * Send handler on move (G).
     */
    private void go() {
        return; // TODO
    }

    /**
     * Wait for handler to be idle. Blocking (F). Without the this command the SMC25 (Handler system) will continue to
     * accept commands while the motor is moving. This may be desirable, as when changing speed during a move or working
     * with the inputs or outputs. Or it may be undesirable, such as when you wish to make a series of indexes. Without
     * the this command any subsequent Go commands received while the motor is indexing would set the "Not allowed while
     * moving" message. Caution: If this command is used while the motor is executing a Slew command the only way to
     * stop is with a reset or a hard limit switch input.
     */
    private void join() {
        return; // TODO
    }

    /**
     * Gives result for wanted registery. (V v).
     *
     * @param v A Acceleration <br/>B Base speed <br/>D Deceleration <br/>E Internal program <br/>G Steps remaining in
     *          current move. Zero if not indexing. <br/>H Hold time <br/>I Input pins <br/>J Slow jog speed <br/>M
     *          Maximum speed <br/>N Number of steps to index <br/>O Output pins <br/>P Position. If motor is indexing
     *          this returns the position at the end of the index. <br/>R Internal program pointer used by trace (T) or
     *          continue (X) commands. Also updated by enter (E) command. <br/>W Ticks remaining on wait counter <br/>X
     *          Crystal frequency
     * @return returns registery as string
     */
    private String verify(char v) {
        return null; // TODO
    }

    /**
     * Set the position register. This command sets the internal absolute position counter to the value of r. (Z r).
     *
     * @param r position range is 0 to 16,777,215
     */
    private void setPositionRegister(int r) {
        return; // TODO
    }

    /**
     * Poll the device for any waiting messages such as errors or end of move. (
     *
     * @return 0 Normal, no service required <br/>1 Command error, illegal command sent <br/>2 Range error, an out of
     *         range numeric parameter was sent <br/>3 Command invalid while moving (e.g. G, S, H) <br/>4 Command only
     *         valid in program (e.g. I, U, L) <br/>5 End of move notice, a previous G command is complete <br/>6 End of
     *         wait notice, a previousW command is complete <br/>7 Hard limit stop, the move was stopped by the hard
     *         limit <br/>8 End of program notice, internal program has completed <br/>G Motor is indexing and no other
     *         notice pending
     */
    private char pollMessage() {
        return 0; // TODO
    }

    public void serialIOEvent(SerialIOEvent event) {
        // TODO
    }
}