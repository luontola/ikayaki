package test.ikayaki.squid;

import ikayaki.squid.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.SynchronousQueue;

/**
 * Test class of SerialIO
 *
 * @author Aki Sysmäläinen
 */
public class SerialIOTest
        extends TestCase implements SerialIOListener {

    private SynchronousQueue<String> queue;

    public static Test suite() {
        return new TestSuite(SerialIOTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    //requires that you have Virtual serial connection from COM2 to COM3
    public void testSerialIO() {
        SerialIO writePort;
        SerialIO readPort;
        try {
            writePort = SerialIO.openPort(new SerialParameters("COM4"));
            readPort = SerialIO.openPort(new SerialParameters("COM7"));
            readPort.addSerialIOListener(this);
            assertTrue(writePort != null);
            assertTrue(readPort != null);
            final String testString = "S124,5//r";
            StringBuffer test = new StringBuffer();
            for (int i = 0; i < 254; i++) {
                test.append((char) i);
            }

            writePort.writeMessage(testString);
            writePort.writeMessage(test.toString());

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            assertTrue(testString.equals((String) queue.poll()));
            assertTrue(test.equals((String) queue.poll()));

        } catch (SerialIOException e) {
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
        } catch (InterruptedException e) {
            System.err.println("Interrupted message event");
        } catch (NullPointerException e) {
            System.err.println("Null from SerialEvent");
        }
    }

}
