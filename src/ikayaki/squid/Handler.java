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

import ikayaki.Settings;
import ikayaki.util.LastExecutor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Offers an interface for controlling the sample handler.
 *
 * @author Aki Korpua, Esko Luontola
 */
public class Handler implements SerialIOListener {

    /**
     * Synchronous queue for waiting result message from handler
     */
    private SynchronousQueue<String> answerQueue = new SynchronousQueue<String>();

    /**
     * Executes the commands to the handler one at a time. All public interfaces should send their commands to this
     * queue so that they would not conflict eachother.
     */
    private LastExecutor workQueue = new LastExecutor(0, false);

    /**
     * timeout how long we wait answer from Squid-system, debugging to prevent lock-ups if communication fails.
     */
    private final int POLL_TIMEOUT = 60;

    /**
     * COM port for communication.
     */
    protected SerialIO serialIO;

    /**
     * Value between 0 and 127 default 5. Settings in the 20-50 range are usually employed.
     */
    private int ACCELERATION;

    /**
     * Value between 0 and 127 default 10. Settings in the 20-50 range are usually employed.
     */
    private int DECELERATION;

    /**
     * Value between 50 and 12 000. The decimal number issued is 10 times the actual pulse rate to the motor. Since the
     * motor requires 200 pulses (full step) or 400 pulses (half step) per revolution, a speed setting of M10000 sets
     * the motor to revolve at 5 revolutions per second in full step or 2.5 revolutions in half step. This rate is
     * one-half the sample rate rotation due to the pulley ratios. The sample handler is set up at the factory for half
     * stepping.
     */
    private int VELOCITY;

    /**
     * Speed in measurement, should be small.
     */
    private int MEASUREMENT_VELOCITY;

    /**
     * Value between 1 and 16,777,215. Relative to Home.
     */
    private int SAMPLE_LOAD_POSITION;

    /**
     * AF demag position for transverse. Relative to Home.
     */
    private int TRANSVERSE_YAF_POSITION;

    /**
     * Axial AF demag position in steps, must be divisible by 10. Relative to Home.
     */
    private int AXIAL_AF_POSITION;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private int BACKGROUND_POSITION;

    /**
     * Position in steps, must be divisible by 10. Relative to Home.
     */
    private int MEASUREMENT_POSITION;

    /**
     * Value between 50 and 8500, but should be small
     */
    private int ROTATION_VELOCITY;

    /**
     * Value between 0 and 127.
     */
    private int ROTATION_ACCELERATION;

    /**
     * Value between 0 and 127.
     */
    private int ROTATION_DECELERATION;

    private int HANDLER_ROTATION;

    /**
     * Currently selected motor to send the commands to.
     */
    private int currentMotor = -1;

    /**
     * The position where the handler is currently, or where it is heading right now. Integer.MIN_VALUE means the left
     * limit, Integer.MAX_VALUE is the right limit.
     */
    private int currentPosition = 0;

    /**
     * Angles are between 0 (0) and 2000 (360).
     */
    private int currentRotation = 0;

    /**
     * Last set velocity. Negative for moving to left and positive for moving to right.
     */
    private int currentVelocity = 0;

    /**
     * Starting point at start of movement
     */
    private int estimatedPositionStart = 0;

    /**
     * Time in milliseconds when we started movement
     */
    private long estimatedPositionStartTime = 0;

    private int estimatedPositionEnd = 0;

    private int estimatedRotationStart = 0;
    private long estimatedRotationStartTime = 0;
    private int estimatedRotationEnd = 0;

    /**
     * Only one at time can be waiting for answer, works like semaphore for commanding handler
     */
    private boolean waitingForMessage = false;

    /**
     * Creates a new handler interface. Opens connection to handler COM port and reads settings from the Settings
     * class.
     */
    protected Handler() throws SerialIOException {
        serialIO = SerialIO.openPort(new SerialParameters(Settings.getHandlerPort(), 1200, 0, 0, 8, 1, 0));
        serialIO.addSerialIOListener(this);
        updateSettings();
    }

    /**
     * Starts up the handler and seeks the home position. Will wait until the handler is ready for operation.
     */
    protected void setUp() throws SerialIOException {
        // put the system online
        setOnline();

        // seek the home position, so we can know where we are
        seekHome();
    }

    /**
     * Checks which settings have changed and updates the handler interface. This method will be called by the Squid
     * class.
     */
    protected void updateSettings() {
        ACCELERATION = Settings.getHandlerAcceleration();
        DECELERATION = Settings.getHandlerDeceleration();
        AXIAL_AF_POSITION = Settings.getHandlerAxialAFPosition();
        BACKGROUND_POSITION = Settings.getHandlerBackgroundPosition();
        SAMPLE_LOAD_POSITION = Settings.getHandlerSampleLoadPosition();
        MEASUREMENT_POSITION = Settings.getHandlerMeasurementPosition();
        MEASUREMENT_VELOCITY = Settings.getHandlerMeasurementVelocity();
        TRANSVERSE_YAF_POSITION = Settings.getHandlerTransverseYAFPosition();
        ROTATION_VELOCITY = Settings.getHandlerRotationVelocity();
        ROTATION_ACCELERATION = Settings.getHandlerRotationAcceleration();
        ROTATION_DECELERATION = Settings.getHandlerRotationDeceleration();
        VELOCITY = Settings.getHandlerVelocity();
        HANDLER_ROTATION = Settings.getHandlerRotation();
    }

    /**
     * Tells whether handler is moving right now.
     */
    public boolean isMoving() {
        return estimatedPositionStart != estimatedPositionEnd;
    }

    /**
     * Tells whether handler is rotating right now.
     */
    public boolean isRotating() {
        return estimatedRotationStart != estimatedRotationEnd;
    }

    /**
     * Returns the position where the handler is currently, or where it is heading right now. Integer.MAX_VALUE means
     * the right limit and Integer.MIN_VALUE means the left limit.
     *
     * @return position relative to home.
     */
    public int getPosition() {
        return currentPosition;
    }

    /**
     * Returns the handler's current rotation, or where it is rotating to right now.
     *
     * @return rotation in range of 0 to 359 degrees
     */
    public int getRotation() {
        double angle = (double) (currentRotation) / HANDLER_ROTATION * 360.0;
        return (int) (Math.round(angle)) % 360;
    }

    /**
     * Sets the position that we start heading to. Integer.MAX_VALUE means the right limit and Integer.MIN_VALUE means
     * the left limit.
     */
    protected void setPosition(int position) {
        if (currentPosition == Integer.MAX_VALUE || currentPosition == Integer.MIN_VALUE) {
            estimatedPositionStart = getEstimatedPosition();
        } else {
            estimatedPositionStart = currentPosition;
        }
        estimatedPositionStartTime = System.currentTimeMillis();
        currentPosition = position;
        estimatedPositionEnd = currentPosition;
        System.err.println("Start Move:" +
                " \tstartTime=" + estimatedPositionStartTime +
                " \tstart=" + estimatedPositionStart +
                " \tend=" + estimatedPositionEnd);

    }

    /**
     * Sets the rotation that we start heading to. The value is in rotation steps and relative to the home position (no
     * limit to how high the value can be).
     */
    protected void setRotation(int rotationSteps) {
        estimatedRotationStart = currentRotation;
        estimatedRotationStartTime = System.currentTimeMillis();
        currentRotation = rotationSteps;
        estimatedRotationEnd = currentRotation;

        // rotations are always in the same direction, even when rotating back to 0
        while (estimatedRotationEnd < estimatedRotationStart) {
            estimatedRotationEnd += HANDLER_ROTATION;
        }
        System.err.println("Start Rotate:" +
                " \tstartTime=" + estimatedRotationStartTime +
                " \tstart=" + estimatedRotationStart +
                " \tend=" + estimatedRotationEnd);

    }

    /**
     * Stops calculating estimated current position
     */
    private void fireMovementStopped() {
        System.err.println("Stop Move:" +
                " \ttravel Time=" + (System.currentTimeMillis() - estimatedPositionStartTime) +
                " \tstart=" + estimatedPositionStart +
                " \tend=" + estimatedPositionEnd);
        if (currentPosition == Integer.MAX_VALUE || currentPosition == Integer.MIN_VALUE) {
            int pos = getEstimatedPosition();
            estimatedPositionStart = pos;
            estimatedPositionEnd = pos;
        } else {
            estimatedPositionStart = currentPosition;
            estimatedPositionEnd = currentPosition;
        }


    }

    /**
     * Stops calculating estimated current position
     */
    private void fireRotationStopped() {
        System.err.println("Stop Rotate:" +
                " \ttravel Time=" + (System.currentTimeMillis() - estimatedRotationStartTime) +
                " \tstart=" + estimatedRotationStart +
                " \tend=" + estimatedRotationEnd);
        estimatedRotationStart = currentRotation;
        estimatedRotationEnd = currentRotation;
    }

    /**
     * Returns an estimation that where handler is right now. Used for drawing graphics.
     *
     * @return the estimated position we are at, or current position if it is known.
     */
    public int getEstimatedPosition() {
//        System.err.println("MOVE:" +
//                "\t  isMoving=" + isMoving() +
//                " \tstartTime=" + estimatedPositionStartTime +
//                " \tstart=" + estimatedPositionStart +
//                " \tend=" + estimatedPositionEnd);
        if (!isMoving()) {
            return estimatedPositionEnd;
        }

        // TODO: maybe currentVelocity is not in steps per second?
        double timeSpent = (System.currentTimeMillis() - estimatedPositionStartTime) / 1000.0;    // in seconds
        int pos = estimatedPositionStart + (int) (currentVelocity * timeSpent);

        // TODO: calculate the acceleration and deceleration corrections
        /* acceleration correction */
        /*
        double accTime = (double) currentVelocity / (double) ACCELERATION;
        if (timeSpent > accTime) {
            pos -= (1050422 * (accTime * accTime)) / (ACCELERATION);
        } else {
            pos = estimatedPositionStart + (int) ((1050422 * (timeSpent * timeSpent)) / (double) (ACCELERATION));
        }
*/

        // we started from PositionStart and if we are already on the other side of PositionEnd, stop at PositionEnd
        if ((estimatedPositionStart < estimatedPositionEnd) != (pos < estimatedPositionEnd)) {
            return estimatedPositionEnd;
        } else {
            return pos;
        }
    }

    /**
     * Used for graphics of squid, estimates from speed and starting time where handler is.
     *
     * @return estimated rotation of where we are at in angles.
     */
    public int getEstimatedRotation() {
//        System.err.println("ROTATE:" +
//                "\tisRotating=" + isRotating() +
//                " \tstartTime=" + estimatedRotationStartTime +
//                " \tstart=" + estimatedRotationStart +
//                " \tend=" + estimatedRotationEnd);
        if (!isRotating()) {
            return getRotation();
        }

        // TODO: maybe currentVelocity is not in steps per second?
        double timeSpent = (System.currentTimeMillis() - estimatedRotationStartTime) / 1000.0;    // in seconds
        int rotation = estimatedRotationStart + (int) ((20 * currentVelocity) * timeSpent);

        // prevent going over the end limit
        if (rotation > estimatedRotationEnd) {
            rotation = estimatedRotationEnd;
        }

        double angle = (double) (rotation) / HANDLER_ROTATION * 360.0;
        // no need to calculate acceleration, error minimal
        return (int) (Math.round(angle)) % 360;
    }

    /**
     * Checks if the serial communication channel is open.
     *
     * @return true if ok.
     */
    public boolean isOK() {
        return (serialIO != null);
    }

    /**
     * Commands the holder to seek home position and rotation. Waits for the home to be found and resets the home
     * position and rotation.
     */
    protected void seekHome() throws SerialIOException {

        // seek home position
        selectMovement();
        if (currentPosition != Integer.MAX_VALUE) {
            slewToLimit(true);
        }

        setMotorNegative();
        setPosition(0);
        serialIO.writeMessage("H1,");
        waitForMessage();
        fireMovementStopped();

        // seek home rotation
        selectRotation();
        setMotorPositive();
        setRotation(0);
        serialIO.writeMessage("H1,");
        waitForMessage();
        fireRotationStopped();
    }

    /**
     * Commands the holder to move to sample load position. Only starts the movement and will not wait for it to
     * finish.
     */
    public void moveToSampleLoad() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    moveToPosition(SAMPLE_LOAD_POSITION);
                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Commands the holder to move to degauss Z position. Only starts the movement and will not wait for it to finish.
     */
    public void moveToDegausserZ() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    moveToPosition(AXIAL_AF_POSITION);
                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Commands the holder to move to degauss Y (and X) position. Only starts the movement and will not wait for it to
     * finish.
     */
    public void moveToDegausserY() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    moveToPosition(TRANSVERSE_YAF_POSITION);
                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Commands the holder to move to measure position. Only starts the movement and will not wait for it to finish.
     */
    public void moveToMeasurement() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    moveToPosition(MEASUREMENT_POSITION);
                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Commands the holder to move to background position. Only starts the movement and will not wait for it to finish.
     */
    public void moveToBackground() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    moveToPosition(BACKGROUND_POSITION);
                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Commands the holder to go to left limit. Only starts the movement and will not wait for it to finish.
     */
    public void moveToLeftLimit() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    moveToPosition(Integer.MIN_VALUE);
                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Commands the holder to go to right limit. Only starts the movement and will not wait for it to finish.
     */
    public void moveToRightLimit() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    moveToPosition(Integer.MAX_VALUE);
                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Moves the handler to an absolute position. Waits for the handler to arrive there. Changes the speed of the
     * handler when necessary.
     *
     * @param position the position to move to, relative to home. If equal to Integer.MIN_VALUE, will go to left limit.
     *                 If equal to Integer.MAX_VALUE, will go to right limit.
     */
    protected void moveToPosition(int position) throws SerialIOException {
        selectMovement();

        if (position == getPosition()) {
            return;                 // do not move if we are already there

        } else if (position == Integer.MIN_VALUE) {
            slewToLimit(false);     // slew to left limit

        } else if (position == Integer.MAX_VALUE) {
            slewToLimit(true);      // slew to right limit

        } else {

            // if we are at a limit, we must recalibrate the home position
            if (getPosition() == Integer.MIN_VALUE || getPosition() == Integer.MAX_VALUE) {
                seekHome();
            }

            // obey speed limit and go to the specified position
            if (getPosition() == BACKGROUND_POSITION) {

                // currently in background position - speed depends on where we are going
                // move to the target position and stop there
                if (position > BACKGROUND_POSITION) {
                    moveSteps(position - getPosition(), MEASUREMENT_VELOCITY);
                } else {
                    moveSteps(position - getPosition(), VELOCITY);
                }

            } else if (getPosition() < BACKGROUND_POSITION) {

                // currently in fast speed area
                if (position > BACKGROUND_POSITION) {
                    // must change the speed at BG position
                    moveSteps(BACKGROUND_POSITION - getPosition(), VELOCITY);
                    moveToPosition(position);   // continue movement from BG position
                } else {
                    // keep the same speed all the way
                    moveSteps(position - getPosition(), VELOCITY);
                }

            } else if (getPosition() > BACKGROUND_POSITION) {

                // currently in slow speed area
                if (position < BACKGROUND_POSITION) {
                    // must change the speed at BG position
                    moveSteps(BACKGROUND_POSITION - getPosition(), MEASUREMENT_VELOCITY);
                    moveToPosition(position);   // continue movement from BG position
                } else {
                    // keep the same speed all the way
                    moveSteps(position - getPosition(), MEASUREMENT_VELOCITY);
                }

            } else {
                assert false;
            }
        }
    }

    /**
     * Commands the holder to move to the specified number of steps. Only sends the move commands and will not wait for
     * the handler to arrive.
     *
     * @param steps the number of steps to move to.
     * @throws IllegalArgumentException if steps is not in range -16777215 to 16777215.
     */
    protected void moveSteps(int steps, int velocity) throws SerialIOException {
        if (steps < -16777215 || steps > 16777215) {
            throw new IllegalArgumentException("steps = " + steps);
        }
        selectMovement();
        setVelocity(velocity);
        if (steps >= 0) {
            setMotorPositive();
        } else {
            setMotorNegative();
        }

        setPosition(getPosition() + steps);
        serialIO.writeMessage("N" + Math.abs(steps));
        go();

        waitForMessage();
        fireMovementStopped();
    }

    protected void slewToLimit(boolean toRight) throws SerialIOException {
        setVelocity(VELOCITY);
        if (toRight) {
            setMotorPositive();
            setPosition(Integer.MAX_VALUE);
        } else {
            setMotorNegative();
            setPosition(Integer.MIN_VALUE);
        }
        performSlew();
        waitForMessage();
        fireMovementStopped();
    }

    /**
     * Rotates the handler to the specified angle. If angle is over than 360 or lower than 0, it is divided by 360 and
     * value is remainder.
     *
     * @param rotationAngle the angle in degrees to rotate the handler to.
     */
    public void rotateTo(final int rotationAngle) {
        workQueue.execute(new Runnable() {
            public void run() {
                int angle = rotationAngle % 360;
                int steps = (int) (((double) angle) / 360.0 * HANDLER_ROTATION);

                try {
                    selectRotation();
                    setMotorPositive();

                    // re-seek home always rotating to zero, otherwise use the counter
                    if (angle == 0) {
                        setRotation(0);
                        serialIO.writeMessage("H1,");
                    } else {
                        int relativeSteps = steps - currentRotation;
                        while (relativeSteps < 0) {
                            relativeSteps += HANDLER_ROTATION;
                        }
                        relativeSteps = relativeSteps % HANDLER_ROTATION;

                        setRotation(currentRotation + relativeSteps);
                        serialIO.writeMessage("N" + relativeSteps);
                        go();
                    }
                    waitForMessage();
                    fireRotationStopped();

                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Waits that all commands sent to the Handler have been executed.
     *
     * @throws InterruptedException if another thread has interrupted the current thread. The interrupted status of the
     *                              current thread is cleared when this exception is thrown.
     */
    public void join() throws InterruptedException {
        workQueue.join();
    }

    /**
     * Sends message to handler go online (@0).
     */
    protected void setOnline() throws SerialIOException {
        serialIO.writeMessage("@0,");
    }

    /**
     * Selects the movement motor to recieve all commands. Sets the default velocity, acceleration and deceleration for
     * the motor. If the movement motor is already selected, does nothing.
     */
    protected void selectMovement() throws SerialIOException {
        if (currentMotor == 0) {
            return;
        }
        currentMotor = 0;
        serialIO.writeMessage("O1,0,");
        setVelocity(VELOCITY);
        setAcceleration(ACCELERATION);
        setDeceleration(DECELERATION);
    }

    /**
     * Selects the rotation motor to recieve all commands. Sets the default velocity, acceleration and deceleration for
     * the motor. If the rotation motor is already selected, does nothing.
     */
    protected void selectRotation() throws SerialIOException {
        if (currentMotor == 1) {
            return;
        }
        currentMotor = 1;
        serialIO.writeMessage("O1,1,");
        setVelocity(ROTATION_VELOCITY);
        setAcceleration(ROTATION_ACCELERATION);
        setDeceleration(ROTATION_DECELERATION);
    }

    /**
     * Sends message to handler to set acceleration (Aa).
     *
     * @param acceleration Acceleration is a number from 0 to 127
     * @throws IllegalArgumentException if the parameter is not in range.
     */
    protected void setAcceleration(int acceleration) throws SerialIOException {
        if (acceleration < 0 || acceleration > 127) {
            throw new IllegalArgumentException("acceleration = " + acceleration);
        }
        serialIO.writeMessage("A" + acceleration + ",");
    }

    /**
     * Sends message to handler to set deceleration (Dd).
     *
     * @param deceleration Deceleration is a number from 0 to 127
     * @throws IllegalArgumentException if the parameter is not in range.
     */
    protected void setDeceleration(int deceleration) throws SerialIOException {
        if (deceleration < 0 || deceleration > 127) {
            throw new IllegalArgumentException("deceleration = " + deceleration);
        }
        serialIO.writeMessage("D" + deceleration + ",");
    }

    /**
     * Sends message to handler to set maximum velocity (Mv).
     *
     * @param velocity Velocity range is 50 to 8,500
     * @throws IllegalArgumentException if the parameter is not in range.
     */
    protected void setVelocity(int velocity) throws SerialIOException {
        if (velocity < 50 || velocity > 8500) {
            throw new IllegalArgumentException("velocity = " + velocity);
        }
        serialIO.writeMessage("M" + velocity + ",");
        if (currentVelocity < 0) {
            currentVelocity = -velocity;
        } else {
            currentVelocity = velocity;
        }
    }

    /**
     * This command stops execution of the internal program if it is used in the program. If the motor is indexing it
     * will ramp down and then stop. Use this command to stop the motor after issuing a slew command. (Q).
     */
    protected void stopExecution() throws SerialIOException {
        serialIO.writeMessage("Q,");
    }

    /**
     * Slew the motor up to maximum speed and continue until reaching a hard limit switch or receiving a quit (Q)
     * command. (S). Automatically runs selectMovement() before slewing.
     */
    protected void performSlew() throws SerialIOException {
        selectMovement();
        serialIO.writeMessage("S,");
    }

    /**
     * Set the motor direction of movement to positive. (+).
     */
    protected void setMotorPositive() throws SerialIOException {
        serialIO.writeMessage("+");
        currentVelocity = Math.abs(currentVelocity);
    }

    /**
     * Set the motor direction of movement to negative. (-).
     */
    protected void setMotorNegative() throws SerialIOException {
        serialIO.writeMessage("-");
        currentVelocity = -Math.abs(currentVelocity);
    }

    /**
     * Send handler on move (G).
     */
    protected void go() throws SerialIOException {
        serialIO.writeMessage("G,");
    }

    /**
     * Wait for handler to be idle. Blocking (F). Without the this command the SMC25 (Handler system) will continue to
     * accept commands while the motor is moving. This may be desirable, as when changing speed during a move or working
     * with the inputs or outputs. Or it may be undesirable, such as when you wish to make a series of indexes. Without
     * the this command any subsequent Go commands received while the motor is indexing would set the "Not allowed while
     * moving" message. Caution: If this command is used while the motor is executing a Slew command the only way to
     * stop is with a reset or a hard limit switch input.
     */
    protected void waitForMessage() throws SerialIOException {
        // blocks all messages for handler
        serialIO.writeMessage("F%,");

        // just polls for messages, we might get old messages waiting there? Use take message.
        //this.serialIO.writeMessage("%,");
        waitingForMessage = true;
        try {
            answerQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
    }

    /**
     * Gives result for wanted registery. (V v).
     *
     * @param registry A Acceleration <br/>B Base speed <br/>D Deceleration <br/>E Internal program <br/>G Steps
     *                 remaining in current move. Zero if not indexing. <br/>H Hold time <br/>I Input pins <br/>J Slow
     *                 jog speed <br/>M Maximum speed <br/>N Number of steps to index <br/>O Output pins <br/>P
     *                 Position. If motor is indexing this returns the position at the end of the index. <br/>R Internal
     *                 program pointer used by trace (T) or continue (X) commands. Also updated by enter (E) command.
     *                 <br/>W Ticks remaining on wait counter <br/>X Crystal frequency
     * @return registery as a string
     */
    protected String verify(char registry) throws SerialIOException {
        serialIO.writeMessage("V" + registry + ",");

        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) answerQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return answer;
    }

    /**
     * take the device for any waiting messages such as errors or end of move. (
     *
     * @return 0 Normal, no service required <br/>1 Command error, illegal command sent <br/>2 Range error, an out of
     *         range numeric parameter was sent <br/>3 Command invalid while moving (e.g. G, S, H) <br/>4 Command only
     *         valid in program (e.g. I, U, L) <br/>5 End of move notice, a previous G command is complete <br/>6 End of
     *         wait notice, a previousW command is complete <br/>7 Hard limit stop, the move was stopped by the hard
     *         limit <br/>8 End of program notice, internal program has completed <br/>G Motor is indexing and no other
     *         notice pending
     */
    protected char takeMessage() throws SerialIOException {
        serialIO.writeMessage("%,");

        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) answerQueue.poll(POLL_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return answer.charAt(0);
    }

    public void serialIOEvent(SerialIOEvent event) {
        String message = event.getCleanMessage();
        if (!waitingForMessage) {
            System.err.println("Recieved a message that nobody waited for: " + message);
            return;
        }

        try {
            answerQueue.put(message);
        } catch (InterruptedException e) {
            System.err.println("Interrupted Handler message event");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Null from SerialEvent in Handler");
            e.printStackTrace();
        }
    }
}
