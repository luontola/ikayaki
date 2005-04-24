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

import java.util.Stack;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Offers an interface for controlling the sample handler.
 *
 * @author Aki Korpua
 */
public class Handler implements SerialIOListener {

    /**
     * Buffer for incoming messages, readed when needed.
     */
    private Stack<String> messageBuffer = new Stack<String>();

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
     * timeout how long we wait answer from Squid-system
     */
    private int pollTimeout = 60;

    /**
     * COM port for communication.
     */
    protected SerialIO serialIO;

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
     * The position where the handler is currently, or where it is heading right now.
     */
    private int currentPosition;

    /**
     * Value between 1 and 16,777,215. Relative to Home.
     */
    private int sampleLoadPosition;

    /**
     * AF demag position for transverse. Relative to Home.
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
    private int currentRotation = 0;

    /**
     * Only one at time can be waiting for answer, works like semaphore for commanding handler
     */
    private boolean waitingForMessage = false;

    /**
     * Value between 50 and 8500, but should be small
     */
    private int rotationSpeed;

    /**
     * Value between 0 and 127.
     */
    private int rotationAcceleration;

    /**
     * Value between 0 and 127.
     */
    private int rotationDeceleration;

    /**
     * Starting point at start of movement
     */
    private int currentStartingPoint;

    /**
     * True if we are moving
     */
    private boolean moving;

    /**
     * Time in nanoseconds when we started movement
     */
    private long startingTime;

    /**
     * Velocity we are moving (negative and positive values accepted)
     */
    private int currentVelocity;

  /**
     * Creates a new handler interface. Opens connection to handler COM port and reads settings from the Settings
     * class.
     */
    protected Handler() throws SerialIOException {
        serialIO = SerialIO.openPort(new SerialParameters(Settings.getHandlerPort(), 1200, 0, 0, 8, 1, 0));
        serialIO.addSerialIOListener(this);
        acceleration = Settings.getHandlerAcceleration();
        deceleration = Settings.getHandlerDeceleration();
        axialAFPosition = Settings.getHandlerAxialAFPosition();
        backgroundPosition = Settings.getHandlerBackgroundPosition();
        sampleLoadPosition = Settings.getHandlerSampleLoadPosition();
        measurementPosition = Settings.getHandlerMeasurementPosition();
        measurementVelocity = Settings.getHandlerMeasurementVelocity();
        transverseYAFPosition = Settings.getHandlerTransverseYAFPosition();
        velocity = Settings.getHandlerVelocity();
        rotationSpeed = Settings.getHandlerRotationVelocity();
        rotationAcceleration = Settings.getHandlerRotationAcceleration();
        rotationDeceleration = Settings.getHandlerRotationDeceleration();
    }

    /**
     * Starts up the handler and seeks the home position. Will wait until the handler is ready for operation.
     */
    protected void setUp() {
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
        acceleration = Settings.getHandlerAcceleration();
        deceleration = Settings.getHandlerDeceleration();
        axialAFPosition = Settings.getHandlerAxialAFPosition();
        backgroundPosition = Settings.getHandlerBackgroundPosition();
        sampleLoadPosition = Settings.getHandlerSampleLoadPosition();
        measurementPosition = Settings.getHandlerMeasurementPosition();
        measurementVelocity = Settings.getHandlerMeasurementVelocity();
        transverseYAFPosition = Settings.getHandlerTransverseYAFPosition();
        rotationSpeed = Settings.getHandlerRotationVelocity();
        rotationAcceleration = Settings.getHandlerRotationAcceleration();
        rotationDeceleration = Settings.getHandlerRotationDeceleration();
        velocity = Settings.getHandlerVelocity();

        //set all settings.. only three. Let's do it..! Rock'N'Roll
        setAcceleration(acceleration);
        setVelocity(velocity);
        setDeceleration(deceleration);
    }

    /**
     * Returns the position where the handler is currently, or where it is heading right now.
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
        double angle = (double) (currentRotation) / Settings.getHandlerRotation() * 360.0;
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
    protected void seekHome() {
        try {
            // seek home position
            selectMovement();
            setVelocity(velocity);
            setAcceleration(acceleration);
            setDeceleration(deceleration);
            setEstimatedMovement(currentPosition);
            if (currentPosition != Integer.MAX_VALUE) {
                currentPosition = Integer.MAX_VALUE;
                serialIO.writeMessage("+S,");
                fireEstimatedMovement();
                waitForMessage();
                stopEstimatedMovement();
                setEstimatedMovement(currentPosition);
            }

            currentVelocity *= -1;
            serialIO.writeMessage("-H1,");
            fireEstimatedMovement();
            waitForMessage();
            stopEstimatedMovement();
            currentPosition = 0;

            // seek home rotation
            selectRotation();
            setEstimatedMovement(currentRotation);
            setVelocity(rotationSpeed);
            setAcceleration(rotationAcceleration);
            setDeceleration(rotationDeceleration);
            serialIO.writeMessage("+H1,");
            fireEstimatedMovement();
            waitForMessage();
            stopEstimatedMovement();
            currentRotation = 0;

        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Commands the holder to move to sample load position. Only starts the movement and will not wait for it to
     * finish.
     */
    public void moveToSampleLoad() {
        workQueue.execute(new Runnable() {
            public void run() {
                moveToPosition(sampleLoadPosition);
            }
        });
    }

    /**
     * Commands the holder to move to degauss Z position. Only starts the movement and will not wait for it to finish.
     */
    public void moveToDegausserZ() {
        workQueue.execute(new Runnable() {
            public void run() {
                moveToPosition(axialAFPosition);
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
                moveToPosition(transverseYAFPosition);
            }
        });
    }


    /**
     * Commands the holder to move to measure position. Only starts the movement and will not wait for it to finish.
     */
    public void moveToMeasurement() {
        workQueue.execute(new Runnable() {
            public void run() {
                moveToPosition(measurementPosition);
            }
        });
    }

    /**
     * Commands the holder to move to background position. Only starts the movement and will not wait for it to finish.
     */
    public void moveToBackground() {
        workQueue.execute(new Runnable() {
            public void run() {
                moveToPosition(backgroundPosition);
            }
        });
    }

    /**
     * Commands the holder to go to left limit. Only starts the movement and will not wait for it to finish.
     */
    public void moveToLeftLimit() {
        workQueue.execute(new Runnable() {
            public void run() {
                moveToPosition(Integer.MIN_VALUE);
            }
        });
    }

    /**
     * Commands the holder to go to right limit. Only starts the movement and will not wait for it to finish.
     */
    public void moveToRightLimit() {
        workQueue.execute(new Runnable() {
            public void run() {
                moveToPosition(Integer.MAX_VALUE);
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
    protected void moveToPosition(int position) {
        selectMovement();
        setEstimatedMovement(currentPosition);

        if (currentPosition == position) {
            // do not move if we are already there
            return;
        } else if (position == Integer.MIN_VALUE) {
            // slew to left limit
            setVelocity(velocity);
            currentPosition = position;
            setMotorNegative();
            performSlew();
            fireEstimatedMovement();
            waitForMessage();
            stopEstimatedMovement();

        } else if (position == Integer.MAX_VALUE) {
            // slew to right limit
            setVelocity(velocity);
            currentPosition = position;
            setMotorPositive();
            performSlew();
            fireEstimatedMovement();
            waitForMessage();
            stopEstimatedMovement();

        } else {

            // if we are at a limit, we must recalibrate the home position
            if (currentPosition == Integer.MIN_VALUE || currentPosition == Integer.MAX_VALUE) {
                seekHome();
            }

            // obey speed limit and go to the specified position
            if (currentPosition == backgroundPosition) {

                // currently in background position - speed depends on where we are going
                if (position > backgroundPosition) {
                    setVelocity(measurementVelocity);
                } else {
                    setVelocity(velocity);
                }

                // move to the target position and stop there
                moveSteps(position - currentPosition);
                currentPosition = position;
                fireEstimatedMovement();
                waitForMessage();
                stopEstimatedMovement();

            } else if (currentPosition < backgroundPosition) {

                // currently in fast speed area
                setVelocity(velocity);

                if (position > backgroundPosition) {
                    // must change the speed at BG position
                    moveSteps(backgroundPosition - currentPosition);
                    currentPosition = backgroundPosition;
                    fireEstimatedMovement();
                    waitForMessage();
                    stopEstimatedMovement();
                    moveToPosition(position);   // continue movement from BG position
                } else {
                    // keep the same speed all the way
                    moveSteps(position - currentPosition);
                    currentPosition = position;
                    fireEstimatedMovement();
                    waitForMessage();
                    stopEstimatedMovement();
                }

            } else if (currentPosition > backgroundPosition) {

                // currently in slow speed area
                setVelocity(measurementVelocity);

                if (position < backgroundPosition) {
                    // must change the speed at BG position
                    moveSteps(backgroundPosition - currentPosition);
                    currentPosition = backgroundPosition;
                    fireEstimatedMovement();
                    waitForMessage();
                    stopEstimatedMovement();
                    moveToPosition(position);   // continue movement from BG position
                } else {
                    // keep the same speed all the way
                    moveSteps(position - currentPosition);
                    currentPosition = position;
                    fireEstimatedMovement();
                    waitForMessage();
                    stopEstimatedMovement();
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
    protected void moveSteps(int steps) {
        if (steps < -16777215 || steps > 16777215) {
            throw new IllegalArgumentException("steps is: " + steps);
        }
        selectMovement();

        String direction;
        if (steps >= 0) {
            direction = "+";
        } else {
            currentVelocity *= -1;
            direction = "-";
        }
        steps = Math.abs(steps);

        setAcceleration(acceleration);
        setDeceleration(deceleration);

        try {
            serialIO.writeMessage(direction + "N" + steps);
            go();
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rotates the handler to the specified angle. If angle is over than 360 or lower than 0, it is divided by 360 and
     * value is remainder. Blocking.
     *
     * @param rotationAngle the angle in degrees to rotate the handler to.
     */
    public void rotateTo(final int rotationAngle) {
        workQueue.execute(new Runnable() {
            public void run() {
                int angle = rotationAngle % 360;
                int steps = (int) (((double) angle) / 360.0 * Settings.getHandlerRotation());
                selectRotation();

                try {
                    setVelocity(rotationSpeed);
                    setAcceleration(rotationAcceleration);
                    setDeceleration(rotationDeceleration);
                    setEstimatedMovement(currentRotation);

                    // re-seek home always rotating to zero, otherwise use the counter
                    if (angle == 0) {
                        fireEstimatedMovement();
                        serialIO.writeMessage("+H1,");
                    } else {
                        int relativeSteps = steps - currentRotation;
                        while (relativeSteps < 0) {
                            currentVelocity *= -1;
                            relativeSteps += Settings.getHandlerRotation();
                        }
                        fireEstimatedMovement();
                        relativeSteps = relativeSteps % Settings.getHandlerRotation();
                        serialIO.writeMessage("+N" + relativeSteps + "G,");
                    }
                    currentRotation = steps;
                    waitForMessage();
                    stopEstimatedMovement();

                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setEstimatedMovement(int from) {
      currentStartingPoint = from;
      //currentFinishPoint = to;
    }

    public void fireEstimatedMovement() {
      startingTime = System.nanoTime();
      moving = true;
    }

    public void stopEstimatedMovement() {
          moving = false;
    }

    public int getEstimatedPosition() {
      if(!moving) return currentPosition;
      //in seconds
      Double estimatedTime = new Long(System.nanoTime() - startingTime).doubleValue()/1000000.0;
      int pos = currentStartingPoint + (int)(currentVelocity*estimatedTime);
      return pos;
    }

    public int getEstimatedRotation() {
      return getEstimatedPosition() % Settings.getHandlerRotation();
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
    protected void setOnline() {
        try {
            serialIO.writeMessage("@0,");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects the movement motor to recieve all commands.
     */
    protected void selectMovement() {
        try {
            serialIO.writeMessage("O1,0,");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects the rotation motor to recieve all commands.
     */
    protected void selectRotation() {
        try {
            serialIO.writeMessage("O1,1,");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends message to handler to set acceleration (Aa).
     *
     * @param a Acceleration is a number from 0 to 127
     * @throws IllegalArgumentException if the parameter is not in range.
     */
    protected void setAcceleration(int a) {
        if (a >= 0 && a < 128) {
            try {
                serialIO.writeMessage("A" + a + ",");
            } catch (SerialIOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("a is : " + a);
        }
    }

    /**
     * Sends message to handler to set deceleration (Dd).
     *
     * @param d Deceleration is a number from 0 to 127
     * @throws IllegalArgumentException if the parameter is not in range.
     */
    protected void setDeceleration(int d) {
        if (d >= 0 && d < 128) {
            try {
                serialIO.writeMessage("D" + d + ",");
            } catch (SerialIOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("d is : " + d);
        }
    }

    /**
     * Sends message to handler to set maximum velocity (Mv).
     *
     * @param v Velocity range is 50 to 8,500
     * @throws IllegalArgumentException if the parameter is not in range.
     */
    protected void setVelocity(int v) {
        if (v >= 50 && v < 8501) {
            try {
                serialIO.writeMessage("M" + v + ",");
                currentVelocity = v;
            } catch (SerialIOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("v is : " + v);
        }
    }

    /**
     * This command stops execution of the internal program if it is used in the program. If the motor is indexing it
     * will ramp down and then stop. Use this command to stop the motor after issuing a slew command. (Q).
     */
    protected void stopExecution() {
        try {
            serialIO.writeMessage("Q,");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Slew the motor up to maximum speed and continue until reaching a hard limit switch or receiving a quit (Q)
     * command. (S). Automatically runs selectMovement() before slewing.
     */
    protected void performSlew() {
        selectMovement();
        try {
            serialIO.writeMessage("S,");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the motor direction of movement to positive. (+).
     */
    protected void setMotorPositive() {
        try {
            serialIO.writeMessage("+");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the motor direction of movement to negative. (-).
     */
    protected void setMotorNegative() {
        try {
            serialIO.writeMessage("-");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send handler on move (G).
     */
    protected void go() {
        try {
            serialIO.writeMessage("G,");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wait for handler to be idle. Blocking (F). Without the this command the SMC25 (Handler system) will continue to
     * accept commands while the motor is moving. This may be desirable, as when changing speed during a move or working
     * with the inputs or outputs. Or it may be undesirable, such as when you wish to make a series of indexes. Without
     * the this command any subsequent Go commands received while the motor is indexing would set the "Not allowed while
     * moving" message. Caution: If this command is used while the motor is executing a Slew command the only way to
     * stop is with a reset or a hard limit switch input.
     */
    protected void waitForMessage() {
        try {
            //This blocks all messages for handler
            serialIO.writeMessage("F%,");
            //this just polls for messages, we might get old messages waiting there? Use take message.
            //this.serialIO.writeMessage("%,");
            waitingForMessage = true;
            try {
                answerQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitingForMessage = false;

        } catch (SerialIOException e) {
            e.printStackTrace();
        }
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
    protected String verify(char v) {
        try {
            serialIO.writeMessage("V" + v + ",");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
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
    protected char takeMessage() {
        try {
            serialIO.writeMessage("%,");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) answerQueue.poll(pollTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return answer.charAt(0);
    }

    public void serialIOEvent(SerialIOEvent event) {
        String message = event.getCleanMessage();
        if (message != null) {
            if (waitingForMessage) {
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
            messageBuffer.add(message);
        }
    }
}
