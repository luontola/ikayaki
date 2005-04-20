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
    private Stack<String> messageBuffer;

    /**
     * Synchronous queue for waiting result message from handler
     */
    private SynchronousQueue<String> queue;
    private int pollTimeout = 60;

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
    private int currentRotation = 0;

    private boolean waitingForMessage = false;

    private int rotationSpeed;
    private int rotationAcceleration;
    private int rotationDeceleration;

  /**
     * Creates a new handler interface. Opens connection to handler COM port and reads settings from the Settings
     * class.
     */
    public Handler() throws SerialIOException {
      this.serialIO = SerialIO.openPort(new SerialParameters(Settings.
          getHandlerPort(), 1200, 0, 0, 8, 1,
          0));
      serialIO.addSerialIOListener(this);
      messageBuffer = new Stack<String> ();
      queue = new SynchronousQueue<String> ();
      this.acceleration = Settings.getHandlerAcceleration();
      this.deceleration = Settings.getHandlerDeceleration();
      this.axialAFPosition = Settings.getHandlerAxialAFPosition();
      this.backgroundPosition = Settings.getHandlerBackgroundPosition();
      this.homePosition = Settings.getHandlerSampleLoadPosition();
      this.measurementPosition = Settings.
          getHandlerMeasurementPosition();
      this.measurementVelocity = Settings.
          getHandlerMeasurementVelocity();
      this.transverseYAFPosition = Settings.
          getHandlerTransverseYAFPosition();
      this.velocity = Settings.getHandlerVelocity();
      this.rotationSpeed = Settings.getHandlerRotationVelocity();
      this.rotationAcceleration = Settings.
          getHandlerRotationAcceleration();
      this.rotationDeceleration = Settings.
          getHandlerRotationDeceleration();
    }

    public void setUp() {
      //first put system online
      this.setOnline();

      //set all settings TODO: do we need to check values? (original does)
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
    public void updateSettings() {
        this.acceleration = Settings.getHandlerAcceleration();
        this.deceleration = Settings.getHandlerDeceleration();
        this.axialAFPosition = Settings.getHandlerAxialAFPosition();
        this.backgroundPosition = Settings.getHandlerBackgroundPosition();
        this.homePosition = Settings.getHandlerSampleLoadPosition();
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
        try {
            this.serialIO.writeMessage("%");
            this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException ex) {
            System.err.println(ex);
            return 'E';
        }
        waitingForMessage = true;
        String answer = null;
        try {
          answer = (String) queue.poll(pollTimeout,TimeUnit.SECONDS);
          System.err.println("get:" + answer + " from queue(status)");
        }
        catch (InterruptedException ex1) {
        }
        waitingForMessage = false;
        return answer.charAt(0);


    }

    /**
     * Returns current known position.
     *
     * @return Value between 1 and 16,777,215
     */
    public int getPosition() {
        return this.currentPosition;

    }

    /**
     * Returns current known rotation.
     *
     * @return Value between 0 and 360 degrees
     */
    public int getRotation() {
        Double angle = new Integer(currentRotation).doubleValue() / new Integer(Settings.getHandlerRotation()).doubleValue();
        angle *= 360;
        return angle.intValue();
    }

    /**
     * checks if connection is ok.
     *
     * @return True if ok
     */
    public boolean isOK() {
        if (serialIO != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Commands the holder to seek home position. Blocking.
     *
     */
    public void seekHome() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        try {
            setVelocity(velocity);
            this.serialIO.writeMessage("O1,0,");
            this.serialIO.writeMessage("+S,");
            this.join();
            this.serialIO.writeMessage("-H1,");
            this.join();
            //this.serialIO.writeMessage("+H,");
            //this.join();
            this.serialIO.writeMessage("O1,1,");
            this.serialIO.writeMessage("+H1,");
            this.join();
            this.currentPosition = this.homePosition;
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Commands the holder to move to home position.
     *
     */
    public void moveToHome() throws IllegalStateException {
      if (this.waitingForMessage)
        throw new IllegalStateException(
            "Tried to command handler while waiting for message");
      setVelocity(velocity);
      if (currentPosition == Integer.MAX_VALUE) {
        try {
          this.serialIO.writeMessage("O1,0,");
          this.serialIO.writeMessage("-H1,");
        }
        catch (SerialIOException ex) {
        }
      }
      else if (currentPosition == Integer.MIN_VALUE) {
        try {
          this.serialIO.writeMessage("O1,0,");
          this.serialIO.writeMessage("+H1,");
        }
        catch (SerialIOException ex) {
        }
      }
      else {
        int pos = this.homePosition - currentPosition;
        this.currentPosition = this.homePosition;
        moveToPos(pos);
      }
    }

    /**
     * Commands the holder to move to degauss Z position. Only starts movement, needs to take with join() when movement
     * is finished.
     */
    public void moveToDegausserZ() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        setVelocity(velocity);
        int pos = this.axialAFPosition - currentPosition;
        this.currentPosition = this.axialAFPosition;
        moveToPos(pos);
        //moveToPos(this.axialAFPosition);
        //this.go();
    }

    /**
     * Commands the holder to move to degauss Y (and X) position. Only starts movement, needs to take with join() when
     * movement is finished.
     */
    public void moveToDegausserY() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        setVelocity(velocity);
        int pos = this.transverseYAFPosition - currentPosition;
        this.currentPosition = this.transverseYAFPosition;
        moveToPos(pos);
        //moveToPos(this.transverseYAFPosition);
        //this.go();
    }


    /**
     * Commands the holder to move to measure position. Only starts movement, needs to take with join() when movement is
     * finished.
     */
    public void moveToMeasurement() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        // do we use now measurement velocity?
        if(currentPosition == this.backgroundPosition)
          setVelocity(measurementVelocity);
        else
          setVelocity(velocity);
        int pos = this.measurementPosition - currentPosition;
        this.currentPosition = this.measurementPosition;
        moveToPos(pos);
        //moveToPos(this.measurementPosition);
        //this.go();

    }

    /**
     * Commands the holder to move to background position. Only starts movement, needs to take with join() when movement
     * is finished.
     */
    public void moveToBackground() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        setVelocity(velocity);
        int pos = this.backgroundPosition - currentPosition;
        this.currentPosition = this.backgroundPosition;
        moveToPos(pos);
        //moveToPos(this.backgroundPosition);
        //this.go();
    }

    /**
     * Commands the holder to go to left limit. Only starts movement, needs to take with join() when movement
     * is finished.
     */
    public void moveToLeftLimit() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        setVelocity(velocity);
        this.currentPosition = Integer.MIN_VALUE;
        this.setMotorNegative();
        this.performSlew();
    }

    /**
     * Commands the holder to go to right limit. Only starts movement, needs to take with join() when movement
     * is finished.
     */
    public void moveToRightLimit() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        setVelocity(velocity);
        this.currentPosition = Integer.MAX_VALUE;
        this.setMotorPositive();
        this.performSlew();
    }


    /**
     * Commands the holder to move to the specified position. Value must be between 1 and 16,777,215. Return true if
     * good pos-value and moves handler there. Only starts movement, needs to take with join() when movement is
     * finished. Positions is in relation to Home.
     *
     * @param pos the position where the handler will move to.
     * @return true if given position was ok, otherwise false.
     */
    public boolean moveToPos(int pos) {
      try {
        boolean direction = true;
        if (pos < 0) {
          pos *= -1;
          direction = false;
        }
        if (pos < 0 || pos > 16777215) {
          return false;
        }

        //first need to set translate active
        this.serialIO.writeMessage("O1,0");
        if (direction)
          this.serialIO.writeMessage("+N" + pos);
        else
          this.serialIO.writeMessage("-N" + pos);
        //this.serialIO.writeMessage("P" + pos); //absolute
        //no need to execute, Go will do it.
        //this.serialIO.writeMessage(","); //execute command
        this.go();
        //this.currentPosition = pos; we cannot but relative position here
        return true;
      }
      catch (SerialIOException ex) {
        System.err.println(ex);
      }
      return false;
    }

    /**
     * Commands the handler to stop its current job.
     */
    public void stop() {
        try {
            this.serialIO.writeMessage("Q,");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }

    }

    /**
     * Rotates the handler to the specified angle. If angle is over than 360 or lower than 0, it is divided by 360 and
     * value is remainder. Only starts movement, needs to take with join() when movement is finished.
     *
     * @param angle the angle in degrees to rotate the handler to.
     */
    public void rotateTo(int angle) throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        angle = angle % 360;
        angle = (int) (((double) angle) / 360.0 * Settings.getHandlerRotation());
        try {
            //first set rotation speed
            this.serialIO.writeMessage("M" + rotationSpeed);
            //then set rotation active
            if(angle == 0) {
              this.serialIO.writeMessage("O1,1,");
              this.serialIO.writeMessage("+H1,");
            }
            else {
              this.serialIO.writeMessage("O1,1");
              this.serialIO.writeMessage("P" + angle + "G,");
            }
            this.currentRotation = angle;
           // this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Sends message to handler go online (@0).
     */
    protected void setOnline() {
        try {
            this.serialIO.writeMessage("@0" + ",");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }

    }

    /**
     * Sends message to handler to set acceleration (Aa).
     *
     * @param a Acceleration is a number from 0 to 127
     */
    protected void setAcceleration(int a) {
        if (a >= 0 && a < 128) {
            try {
                this.serialIO.writeMessage("A" + a + ",");
                //this.serialIO.writeMessage(","); //execute command
                this.acceleration = a;
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * Sends message to handler to set deceleration (Dd).
     *
     * @param d Deceleration is a number from 0 to 127
     */
    protected void setDeceleration(int d) {
        if (d >= 0 && d < 128) {
            try {
                this.serialIO.writeMessage("D" + d + ",");
                //this.serialIO.writeMessage(","); //execute command
                this.deceleration = d;
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * Sends message to handler to set base speed. Base rate is the speed at which the motor motion starts and stops.
     * (Bb).
     *
     * @param b Base Speed is pulses per second and has a range of 50 to 5000.
     */
    protected void setBaseSpeed(int b) {
        if (b >= 50 && b < 5001) {
            try {
                this.serialIO.writeMessage("B" + b + ",");
                //this.serialIO.writeMessage(","); //execute command
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }

    }

    /**
     * Sends message to handler to set maximum velocity (Mv).
     *
     * @param v Velocity range is 50 to 20,000
     */
    protected void setVelocity(int v) {
        if (v >= 50 && v < 8501) {
            try {
                this.serialIO.writeMessage("M" + v + ",");
                //this.serialIO.writeMessage(","); //execute command
                this.velocity = v;
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }
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
    protected void setHoldTime(int h) {
        try {
            this.serialIO.writeMessage("CH" + h + ",");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }

    }

    /**
     * Set the crystal frequency to the value of rrrrrr. The range of values is 4,000,000 to 8,000.000. This command
     * does not check for out of range numbers. The crystal frequency is used by the chip for setting the base speed and
     * maximum speed and for controlling the time for the wait command. (CX cf).
     *
     * @param cf frequence range is 4,000,000 to 8,000.000
     */
    protected void setCrystalFrequence(int cf) {
        if (cf >= 4000000 && cf <= 8000000) {
            try {
                this.serialIO.writeMessage("CX" + cf + ",");
                //this.serialIO.writeMessage(","); //execute command
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
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
        } catch (SerialIOException ex) {
            System.err.println(ex);
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
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Set the motor direction of movement to positive. (+).
     */
    protected void setMotorPositive() {
        try {
            this.serialIO.writeMessage("+");
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }

    }

    /**
     * Set the motor direction of movement to negative. (-).
     */
    protected void setMotorNegative() {
        try {
            this.serialIO.writeMessage("-");
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }

    }

    /**
     * Set the number of steps to move for the G command. (N s).
     *
     * @param s steps range is 0 to 16,777,215
     */
    protected void setSteps(int s) {
        if (s >= 0 && s <= 16777216) {
            try {
                this.serialIO.writeMessage("N" + s + ",");
                //this.serialIO.writeMessage(","); //execute command
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * Set absolute position to move for the G command. (P p).
     *
     * @param p position range is 0 to 16,777,215
     */
    protected void setPosition(int p) {
        if (p > 0 && p < 16777215) {
            try {
                //first need to set translate active
                this.serialIO.writeMessage("O1,0");
                this.serialIO.writeMessage("P" + p + ",");
                this.currentPosition = p;
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }

    }

    /**
     * Send handler on move (G).
     */
    protected void go() {
        try {
            this.serialIO.writeMessage("G,");
            //this.serialIO.writeMessage(","); //execute command
        } catch (SerialIOException ex) {
            System.err.println(ex);
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
    public void join() {
        try {
            this.serialIO.writeMessage("F%,");
            //this.serialIO.writeMessage(","); //execute command
            waitingForMessage = true;
            try {
              String answer = (String) queue.take();//poll(pollTimeout, TimeUnit.SECONDS);
              System.err.println("get:" + answer + " from queue(join)");
            }
            catch (InterruptedException ex1) {
            }
            waitingForMessage = false;

        } catch (SerialIOException ex) {
            System.err.println(ex);
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
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
        waitingForMessage = true;
        String answer = null;
       // while(true) {
          try {
            answer = (String) queue.take(); //(pollTimeout, TimeUnit.SECONDS);
            System.err.println("get:" + answer + " from queue(verify)");
            //if(answer != null) break;
            //Thread.sleep(1000);
          }
          catch (InterruptedException ex1) {
          }
        //}

        waitingForMessage = false;
        return answer;

    }

    /**
     * Set the position register. This command sets the internal absolute position counter to the value of r. (Z r).
     *
     * @param r position range is 0 to 16,777,215
     */
    protected void setPositionRegister(int r) {
        if (r > 0 && r < 16777215) {
            try {
                this.serialIO.writeMessage("Z" + r + ",");
                //this.serialIO.writeMessage(","); //execute command
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }
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
        } catch (SerialIOException ex) {
            System.err.println(ex);
        }
        waitingForMessage = true;
        String answer = null;
        try {
          answer = (String) queue.poll(pollTimeout,TimeUnit.SECONDS);
          System.err.println("get:" + answer + " from queue(take)");
        }
        catch (InterruptedException ex1) {
        }
        waitingForMessage = false;
        return answer.charAt(0);
    }

    public void serialIOEvent(SerialIOEvent event) {
      //System.err.println("new event:" + event.getMessage());
      String message = event.getMessage();
      if(message != null) {
        if (waitingForMessage) {
          try {
            //System.err.println("putted:" + message + " to queue");
            queue.put(message);
          }
          catch (InterruptedException e) {
            System.err.println("Interrupted Handler message event");
          }
          catch (NullPointerException e) {
            System.err.println("Null from SerialEvent in Handler");
          }
        }
        messageBuffer.add(message);
      }
    }
}
