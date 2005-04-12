package test.ikayaki.squid;

import ikayaki.squid.*;
import junit.framework.Assert;

/**
 * Test class of SerialIO
 *
 * @author Aki Sysmäläinen
 */
public class SerialIOTest implements SerialIOListener {
    private static SerialIO writePort;
    private static SerialIO readPort;


    public static void main(String[] args) {
        try {
            new SerialIOTest();
        } catch (SerialIOException e) {
            System.out.println(e.getMessage());
        }
    }


    public SerialIOTest() throws SerialIOException {
        //super(parameters);
        try {
            writePort = SerialIO.openPort(new SerialParameters("COM4"));
            readPort = SerialIO.openPort(new SerialParameters("COM7"));

        } catch (SerialIOException e) {
            System.out.println(e.getMessage());
        }

        //writePort.addSerialIOListener(this);
        readPort.addSerialIOListener(this);

        try {
            writePort.writeMessage("test");

        } catch (SerialIOException e) {
            System.out.println(e.getMessage());
        }
    }


    public void testSerialIO() {

        //Assert.assertTrue(expected.equals(result));
    }

    public void testWriteMessage() {

    }

    public void testGetLastMessage() {

    }

    public void serialIOEvent(SerialIOEvent event) {
        System.out.println("message recieved:" + event.getMessage());
        Assert.assertEquals("test", event.getMessage());
    }
}
