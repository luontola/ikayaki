package test.ikayaki.squid;

import junit.framework.*;
import ikayaki.squid.SerialIOException;
import ikayaki.squid.SerialParameters;
import ikayaki.squid.SerialIO;
import ikayaki.squid.SerialIOListener;
import ikayaki.squid.SerialIOEvent;
import java.util.concurrent.SynchronousQueue;

/**
 * Test class of SerialIO
 *
 * @author Aki Sysmäläinen
 */
public class SerialIOTest
    extends TestCase implements SerialIOListener {

  private SynchronousQueue queue;

  public static Test suite() {
    return new TestSuite(SerialIOTest.class);
  }

  public static void main(String args[]) {
    junit.textui.TestRunner.run(suite());
  }

  //requires that you have Virtual serial connection from COM2 to COM3
  public void testSerialIO() {
    SerialIO com2;
    SerialIO com3;
    try {
      com2 = SerialIO.openPort(new SerialParameters("COM2"));
      com3 = SerialIO.openPort(new SerialParameters("COM3"));
      com3.addSerialIOListener(this);
      assertTrue(com2 != null);
      assertTrue(com3 != null);
      String testString = "S124,5//r";
      com2.writeMessage(testString);
      assertTrue(testString.equals( (String) queue.poll()));
    }
    catch (SerialIOException e) {
      fail(e.toString());
    }
  }

  public void testWriteMessage() {

  }

  public void testGetLastMessage() {

  }

  public void serialIOEvent(SerialIOEvent event) {
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

}
