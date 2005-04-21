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

    /**
     * timeout how leng we wait answer from Squid-system
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
     * Value between 1 and 16,777,215. Relative to Home.
     */
    private int currentPosition;

    /**
     * Value between 1 and 16,777,215. Relative to Home.
     */
    private int homePosition;

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
    private int rotationAcceleration;   // TODO: this field is assigned but never used

    /**
     * Value between 0 and 127.
     */
    private int rotationDeceleration;   // TODO: this field is assigned but never used

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
        double angle = (double) (currentRotation) / Settings.getHandlerRotation();
        angle *= 360;
        return (int) angle;
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
            setAcceleration(acceleration);
            setDeceleration(deceleration);
            this.serialIO.writeMessage("O1,0,");
            this.serialIO.writeMessage("+S,");
            this.join();
            this.serialIO.writeMessage("-H1,");
            this.join();
            setVelocity(rotationSpeed);
            setAcceleration(rotationAcceleration);
            setDeceleration(rotationDeceleration);
            this.serialIO.writeMessage("O1,1,");
            this.serialIO.writeMessage("+H1,");
            this.join();
            this.currentPosition = this.homePosition;
            this.currentRotation = 0;
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
        moveSteps(pos);
        this.currentPosition = this.homePosition;
      }
    }

    /**
     * Commands the holder to move to degauss Z position. Only starts movement, needs to take with join() when movement
     * is finished.
     */
    public void moveToDegausserZ() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        int pos = this.axialAFPosition - currentPosition;
        moveSteps(pos);
        this.currentPosition = this.axialAFPosition;
    }

    /**
     * Commands the holder to move to degauss Y (and X) position. Only starts movement, needs to take with join() when
     * movement is finished.
     */
    public void moveToDegausserY() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        int pos = this.transverseYAFPosition - currentPosition;
        moveSteps(pos);
        this.currentPosition = this.transverseYAFPosition;
    }


    /**
     * Commands the holder to move to measure position. Only starts movement, needs to take with join() when movement is
     * finished.
     */
    public void moveToMeasurement() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        int pos = this.measurementPosition - currentPosition;
        moveSteps(pos);
        this.currentPosition = this.measurementPosition;

    }

    /**
     * Commands the holder to move to background position. Only starts movement, needs to take with join() when movement
     * is finished.
     */
    public void moveToBackground() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        int pos = this.backgroundPosition - currentPosition;
        moveSteps(pos);
        this.currentPosition = this.backgroundPosition;
    }

    /**
     * Commands the holder to go to left limit. Only starts movement, needs to take with join() when movement
     * is finished.
     */
    public void moveToLeftLimit() throws IllegalStateException {
        if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
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
     * Commands the holder to move to the relative position. Return true if
     * good pos-value and moves handler there. Only starts movement, needs to take with join() when movement is
     * finished. Positions is in relation to Home. Changes speed if needed.
     *
     * @param pos the position where the handler will move to
     * @return true if given position was ok, otherwise false.
     */
    public boolean moveSteps(int pos) {
      if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
      boolean direction = true;
      int speedChange = 0; // 0: no change, 1: change to measurement , 2: change to normal later
      int speedChangeTime = 0; // in milliseconds

      // check if we are going to measurement position, change speed on-the-fly if needed
      if((currentPosition + pos) == measurementPosition) {
        int distanceToMeasure = backgroundPosition - currentPosition;
        speedChange = 1;
        speedChangeTime = distanceToMeasure/velocity*1000;
        if(speedChangeTime < 0) speedChangeTime *= -1;
      }
      else if(currentPosition == measurementPosition) {
        int distanceToBackground = backgroundPosition - currentPosition;
        speedChange = 2;
        speedChangeTime = distanceToBackground/velocity*1000;
        if(speedChangeTime < 0) speedChangeTime *= -1;

      }

      //if negative value, then we send "-" command and change
      if (pos < 0) {
        pos *= -1;
        direction = false;
      }
      if (pos < 0 || pos > 16777215) {
        return false;
      }

      final int posT = pos;
      final boolean directionT = direction;
      final int speedChangeT = speedChange;
      final int speedChangeTimeT = speedChangeTime;

      new Thread() {
        @Override public void run() {
          try {

            //select speed
            if(speedChangeT == 1 && speedChangeTimeT == 0)
              setVelocity(measurementVelocity);
            else if(speedChangeT == 2)
              setVelocity(measurementVelocity);
            else
              setVelocity(velocity);
            setAcceleration(acceleration);
            setDeceleration(deceleration);

            //first need to set translate active
            serialIO.writeMessage("O1,0");
            if (directionT)
              serialIO.writeMessage("+N" + posT);
            else
              serialIO.writeMessage("-N" + posT);
            go();

            //on-the-fly speed change
            if(speedChangeT == 1 && speedChangeTimeT > 0) {
              try {
                Thread.sleep(speedChangeTimeT);
                setVelocity(measurementVelocity);
              }
              catch (InterruptedException ex1) {
              }
            }
            else if (speedChangeT == 2) {
              try {
                Thread.sleep(speedChangeTimeT);
                setVelocity(velocity);
              }
              catch (InterruptedException ex1) {
              }

            }
          }
          catch (SerialIOException ex) {
            System.err.println(ex);
          }
        }
      }.start();
      return true;
  }

    /**
     * Rotates the handler to the specified angle. If angle is over than 360 or lower than 0, it is divided by 360 and
     * value is remainder. Only starts movement, needs to take with join() when movement is finished.
     *
     * @param angle the angle in degrees to rotate the handler to.
     */
    public void rotateTo(int angle) throws IllegalStateException {
       // TODO rotation korjattava siten etta - ja + merkit annetaan
       // riippuen nykyisesta positiosta.
       // Handler.java:n tulisi varmaan muistaa rotatio-moottorille asetettu suunta (+/-)?
       //

       if(this.waitingForMessage)
          throw new IllegalStateException("Tried to command handler while waiting for message");
        angle = angle % 360;
        angle = (int) (((double) angle) / 360.0 * Settings.getHandlerRotation());
        try {
            //first set rotation speed,acceleration and deceleration
            setVelocity(rotationSpeed);
            setAcceleration(rotationAcceleration);
            setDeceleration(rotationDeceleration);
            //then set rotation active
            if(angle == 0) {
              this.serialIO.writeMessage("O1,1,");
              this.serialIO.writeMessage("+H1,");
            }
            else {
              int rotation = angle - currentRotation;
              this.serialIO.writeMessage("O1,1");
              this.serialIO.writeMessage("N" + rotation + "G,");
            }
            this.currentRotation = angle;
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
                this.deceleration = d;
            } catch (SerialIOException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * Sends message to handler to set maximum velocity (Mv).
     *
     * @param v Velocity range is 50 to 8,500
     */
    protected void setVelocity(int v) {
        if (v >= 50 && v < 8501) {
            try {
                this.serialIO.writeMessage("M" + v + ",");
                this.velocity = v;
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
            //This blocks all messages for handler
            this.serialIO.writeMessage("F%,");
            //this just polls for messages, we might get old messages waiting there? Use take message.
            //this.serialIO.writeMessage("%,");
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
