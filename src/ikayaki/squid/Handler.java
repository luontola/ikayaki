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
/*
Event A: On SerialIOEvent - reads message and puts it in a buffer
*/

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
     * Handlers current status.
     */
    private String status;  // TODO: this field is never used

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
    private String handlerStatus;   // TODO: this field is never used

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
     * Creates a new handler interface. Opens connection to handler COM port and reads settings from the Settings
     * class.
     */
    protected Handler() throws SerialIOException {
        this.serialIO = SerialIO.openPort(new SerialParameters(Settings.getHandlerPort(), 1200, 0, 0, 8, 1, 0));
        this.serialIO.addSerialIOListener(this);
        this.acceleration = Settings.getHandlerAcceleration();
        this.deceleration = Settings.getHandlerDeceleration();
        this.axialAFPosition = Settings.getHandlerAxialAFPosition();
        this.backgroundPosition = Settings.getHandlerBackgroundPosition();
        this.sampleLoadPosition = Settings.getHandlerSampleLoadPosition();
        this.measurementPosition = Settings.getHandlerMeasurementPosition();
        this.measurementVelocity = Settings.getHandlerMeasurementVelocity();
        this.transverseYAFPosition = Settings.getHandlerTransverseYAFPosition();
        this.velocity = Settings.getHandlerVelocity();
        this.rotationSpeed = Settings.getHandlerRotationVelocity();
        this.rotationAcceleration = Settings.getHandlerRotationAcceleration();
        this.rotationDeceleration = Settings.getHandlerRotationDeceleration();
    }

    protected void setUp() {
        //first put system online
        this.setOnline();

        //set all settings
        this.setAcceleration(this.acceleration);
        System.err.println("Acceleration set:" + this.verify('A'));
        this.setVelocity(this.velocity);
        System.err.println("Velocity set:" + this.verify('M'));
        this.setDeceleration(this.deceleration);
        System.err.println("Deceleration set:" + this.verify('D'));

        //must be send to seek home position, so we can know where we are
        this.seekHome();

    }

    /**
     * Checks which settings have changed and updates the handler interface. This method will be called by the Squid
     * class.
     */
    protected void updateSettings() {
        this.acceleration = Settings.getHandlerAcceleration();
        this.deceleration = Settings.getHandlerDeceleration();
        this.axialAFPosition = Settings.getHandlerAxialAFPosition();
        this.backgroundPosition = Settings.getHandlerBackgroundPosition();
        this.sampleLoadPosition = Settings.getHandlerSampleLoadPosition();
        this.measurementPosition = Settings.getHandlerMeasurementPosition();
        this.measurementVelocity = Settings.getHandlerMeasurementVelocity();
        this.transverseYAFPosition = Settings.getHandlerTransverseYAFPosition();
        this.rotationSpeed = Settings.getHandlerRotationVelocity();
        this.rotationAcceleration = Settings.getHandlerRotationAcceleration();
        this.rotationDeceleration = Settings.getHandlerRotationDeceleration();
        this.velocity = Settings.getHandlerVelocity();

        //set all settings.. only three. Let's do it..! Rock'N'Roll
        this.setAcceleration(this.acceleration);
        this.setVelocity(this.velocity);
        this.setDeceleration(this.deceleration);
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
     * Returns current known rotation.
     *
     * @return value between 0 and 360 degrees
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
     * Commands the holder to seek home position and rotation. Only starts the movement and will not wait for it to
     * finish.
     */
    public void seekHome() {
        workQueue.execute(new Runnable() {
            public void run() {
                try {
                    // seek home position
                    setVelocity(velocity);
                    setAcceleration(acceleration);
                    setDeceleration(deceleration);
                    serialIO.writeMessage("O1,0,");
                    serialIO.writeMessage("+S,");
                    waitForMessage();

                    serialIO.writeMessage("-H1,");
                    waitForMessage();
                    currentPosition = 0;

                    // seek home rotation
                    setVelocity(rotationSpeed);
                    setAcceleration(rotationAcceleration);
                    setDeceleration(rotationDeceleration);
                    serialIO.writeMessage("O1,1,");
                    serialIO.writeMessage("+H1,");
                    waitForMessage();
                    currentRotation = 0;

                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });
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

//        setVelocity(velocity);
//        if (currentPosition == Integer.MAX_VALUE) {
//            try {
//                this.serialIO.writeMessage("O1,0,");
//                this.serialIO.writeMessage("-H1,");
//            } catch (SerialIOException e) {
//                e.printStackTrace();
//            }
//        } else if (currentPosition == Integer.MIN_VALUE) {
//            try {
//                this.serialIO.writeMessage("O1,0,");
//                this.serialIO.writeMessage("+H1,");
//            } catch (SerialIOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            int pos = this.sampleLoadPosition - currentPosition;
//            moveSteps(pos);
//            this.currentPosition = this.sampleLoadPosition;
//        }
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

//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        int pos = this.axialAFPosition - currentPosition;
//        moveSteps(pos);
//        this.currentPosition = this.axialAFPosition;
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

//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        int pos = this.transverseYAFPosition - currentPosition;
//        moveSteps(pos);
//        this.currentPosition = this.transverseYAFPosition;
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

//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        int pos = this.measurementPosition - currentPosition;
//        moveSteps(pos);
//        this.currentPosition = this.measurementPosition;
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

//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        int pos = this.backgroundPosition - currentPosition;
//        moveSteps(pos);
//        this.currentPosition = this.backgroundPosition;
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

//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        this.currentPosition = Integer.MIN_VALUE;
//        this.setMotorNegative();
//        this.performSlew();
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

//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        setVelocity(velocity);
//        this.currentPosition = Integer.MAX_VALUE;
//        this.setMotorPositive();
//        this.performSlew();
    }

    /**
     * Moves the handler to an absolute position. Waits for the handler to arrive there. Changes the speed of the
     * handler when necessary.
     *
     * @param position the position to move to, relative to home. If equal to Integer.MIN_VALUE, will go to left limit.
     *                 If equal to Integer.MAX_VALUE, will go to right limit.
     */
    protected void moveToPosition(int position) {
        if (currentPosition == position) {
            // do not move if we are already there
            return;

        } else if (position == Integer.MIN_VALUE) {
            // slew to left limit
            setVelocity(velocity);
            currentPosition = position;
            setMotorNegative();
            performSlew();
            waitForMessage();

        } else if (position == Integer.MAX_VALUE) {
            // slew to right limit
            setVelocity(velocity);
            currentPosition = position;
            setMotorPositive();
            performSlew();
            waitForMessage();

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
                moveSteps(position - currentPosition);
                currentPosition = position;
                waitForMessage();

            } else if (currentPosition < backgroundPosition) {

                // currently in fast speed area
                setVelocity(velocity);

                if (position > backgroundPosition) {
                    // must change the speed at BG position
                    moveSteps(backgroundPosition - currentPosition);
                    currentPosition = backgroundPosition;
                    waitForMessage();
                    moveToPosition(position);
                } else {
                    // keep the same speed all the way
                    moveSteps(position - currentPosition);
                    currentPosition = position;
                    waitForMessage();
                }

            } else if (currentPosition > backgroundPosition) {

                // currently in slow speed area
                setVelocity(measurementVelocity);

                if (position < backgroundPosition) {
                    // must change the speed at BG position
                    moveSteps(backgroundPosition - currentPosition);
                    currentPosition = backgroundPosition;
                    waitForMessage();
                    moveToPosition(position);
                } else {
                    // keep the same speed all the way
                    moveSteps(position - currentPosition);
                    currentPosition = position;
                    waitForMessage();
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

        String direction;
        if (steps >= 0) {
            direction = "+";
        } else {
            direction = "-";
        }
        steps = Math.abs(steps);

        setAcceleration(acceleration);
        setDeceleration(deceleration);

        try {
            serialIO.writeMessage("O1,0");
            serialIO.writeMessage(direction + "N" + steps);
            go();
        } catch (SerialIOException e) {
            e.printStackTrace();
        }


//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        boolean direction = true;
//        int speedChange = 0; // 0: no change, 1: change to measurement , 2: change to normal later
//        int speedChangeTime = 0; // in milliseconds
//
//        // check if we are going to measurement position, change speed on-the-fly if needed
//        if ((currentPosition + pos) == measurementPosition) {
//            int distanceToMeasure = backgroundPosition - currentPosition;
//            speedChange = 1;
//            speedChangeTime = distanceToMeasure / velocity * 1000;
//            if (speedChangeTime < 0) speedChangeTime *= -1;
//        } else if (currentPosition == measurementPosition) {
//            int distanceToBackground = backgroundPosition - currentPosition;
//            speedChange = 2;
//            speedChangeTime = distanceToBackground / velocity * 1000;
//            if (speedChangeTime < 0) speedChangeTime *= -1;
//
//        }
//
//        //if negative value, then we send "-" command and change
//        if (pos < 0) {
//            pos *= -1;
//            direction = false;
//        }
//        if (pos < 0 || pos > 16777215) {
//            return false;
//        }
//
//        final int posT = pos;
//        final boolean directionT = direction;
//        final int speedChangeT = speedChange;
//        final int speedChangeTimeT = speedChangeTime;
//
//        new Thread() {
//            @Override public void run() {
//                try {
//
//                    //select speed
//                    if (speedChangeT == 1 && speedChangeTimeT == 0) {
//                        setVelocity(measurementVelocity);
//                    } else if (speedChangeT == 2) {
//                        setVelocity(measurementVelocity);
//                    } else {
//                        setVelocity(velocity);
//                    }
//                    setAcceleration(acceleration);
//                    setDeceleration(deceleration);
//
//                    //first need to set translate active
//                    serialIO.writeMessage("O1,0");
//                    if (directionT) {
//                        serialIO.writeMessage("+N" + posT);
//                    } else {
//                        serialIO.writeMessage("-N" + posT);
//                    }
//                    go();
//
//                    //on-the-fly speed change
//                    if (speedChangeT == 1 && speedChangeTimeT > 0) {
//                        try {
//                            Thread.sleep(speedChangeTimeT);
//                            setVelocity(measurementVelocity);
//                        } catch (InterruptedException ex1) {
//                        }
//                    } else if (speedChangeT == 2) {
//                        try {
//                            Thread.sleep(speedChangeTimeT);
//                            setVelocity(velocity);
//                        } catch (InterruptedException ex1) {
//                        }
//
//                    }
//                } catch (SerialIOException ex) {
//                    System.err.println(ex);
//                }
//            }
//        }.start();
//        return true;
    }

    /**
     * Rotates the handler to the specified angle. If angle is over than 360 or lower than 0, it is divided by 360 and
     * value is remainder. Only starts the movement and will not wait for it to finish.
     *
     * @param rotationAngle the angle in degrees to rotate the handler to.
     */
    public void rotateTo(final int rotationAngle) {
        // TODO rotation korjattava siten etta - ja + merkit annetaan
        // riippuen nykyisesta positiosta.
        // Handler.java:n tulisi varmaan muistaa rotatio-moottorille asetettu suunta (+/-)?
        //

        workQueue.execute(new Runnable() {
            public void run() {
                int angle = rotationAngle % 360;
                int steps = (int) (((double) angle) / 360.0 * Settings.getHandlerRotation());

                try {
                    // first set rotation speed, acceleration and deceleration
                    setVelocity(rotationSpeed);
                    setAcceleration(rotationAcceleration);
                    setDeceleration(rotationDeceleration);

                    // then set rotation active
                    if (angle == 0) {
                        serialIO.writeMessage("O1,1,");
                        serialIO.writeMessage("+H1,");
                    } else {
                        serialIO.writeMessage("O1,1");
                        serialIO.writeMessage("+N" + (steps - currentRotation) + "G,");
                    }
                    currentRotation = steps;

                } catch (SerialIOException e) {
                    e.printStackTrace();
                }
            }
        });


//        if (this.waitingForMessage) {
//            throw new IllegalStateException("Tried to command handler while waiting for message");
//        }
//        angle = angle % 360;
//        angle = (int) (((double) angle) / 360.0 * Settings.getHandlerRotation());
//        try {
//            //first set rotation speed,acceleration and deceleration
//            setVelocity(rotationSpeed);
//            setAcceleration(rotationAcceleration);
//            setDeceleration(rotationDeceleration);
//            //then set rotation active
//            if (angle == 0) {
//                this.serialIO.writeMessage("O1,1,");
//                this.serialIO.writeMessage("+H1,");
//            } else {
//                int rotation = angle - currentRotation;
//                this.serialIO.writeMessage("O1,1");
//                this.serialIO.writeMessage("N" + rotation + "G,");
//            }
//            this.currentRotation = angle;
//        } catch (SerialIOException ex) {
//            System.err.println(ex);
//        }
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
            this.serialIO.writeMessage("@0" + ",");
            //this.serialIO.writeMessage(","); //execute command
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
                this.serialIO.writeMessage("A" + a + ",");
                //this.serialIO.writeMessage(","); //execute command
                this.acceleration = a;
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
                this.serialIO.writeMessage("D" + d + ",");
                this.deceleration = d;
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
                this.serialIO.writeMessage("M" + v + ",");
                this.velocity = v;
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
            this.serialIO.writeMessage("Q,");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Slew the motor up to maximum speed and continue until reaching a hard limit switch or receiving a quit (Q)
     * command. (S).
     */
    protected void performSlew() {
        try {
            this.serialIO.writeMessage("S,");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the motor direction of movement to positive. (+).
     */
    protected void setMotorPositive() {
        try {
            this.serialIO.writeMessage("+");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the motor direction of movement to negative. (-).
     */
    protected void setMotorNegative() {
        try {
            this.serialIO.writeMessage("-");
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send handler on move (G).
     */
    protected void go() {
        try {
            this.serialIO.writeMessage("G,");
            //this.serialIO.writeMessage(","); //execute command
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
            this.serialIO.writeMessage("F%,");
            //this just polls for messages, we might get old messages waiting there? Use take message.
            //this.serialIO.writeMessage("%,");
            waitingForMessage = true;
            try {
                String answer = (String) answerQueue.take();//poll(pollTimeout, TimeUnit.SECONDS);
                System.err.println("get:" + answer + " from queue(join)");
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
            this.serialIO.writeMessage("V" + v + ",");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        // while(true) {
        try {
            answer = (String) answerQueue.take(); //(pollTimeout, TimeUnit.SECONDS);
            System.err.println("get:" + answer + " from queue(verify)");
            //if(answer != null) break;
            //Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //}

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
            this.serialIO.writeMessage("%,");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException e) {
            e.printStackTrace();
        }
        waitingForMessage = true;
        String answer = null;
        try {
            answer = (String) answerQueue.poll(pollTimeout, TimeUnit.SECONDS);
            System.err.println("get:" + answer + " from queue(take)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitingForMessage = false;
        return answer.charAt(0);
    }

    public void serialIOEvent(SerialIOEvent event) {
        //System.err.println("new event:" + event.getMessage());
        String message = event.getMessage();
        if (message != null) {
            if (waitingForMessage) {
                try {
                    //System.err.println("putted:" + message + " to queue");
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
