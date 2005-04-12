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
    private final String testString1 = "0123456789";
    private final String testString2 = "asd";
    private final String testString3 = "This is a long long long test string, and we really hope it's gonna look" +
            "the same when we recieve it again...saasjfda sldfkja sldjf asldj asljfd" +
            " sdfasdf jals dlaskd    asldjfaslfdas ldjfasldkfj \n jslkdjfaslkdjflasj";

    private final String testStringAscii = "";

    private int msgCounter = 0;


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
            writePort.writeMessage(testString1);
            Thread.sleep(2000);
            writePort.writeMessage(testString2);
            Thread.sleep(2000);
            writePort.writeMessage(testString3);

        } catch (SerialIOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void testSerialIO() {

        //Assert.assertTrue(expected.equals(result));
    }


    public void serialIOEvent(SerialIOEvent event) {
        System.out.println("message recieved:" + event.getMessage());
        switch (msgCounter) {
        case 0:
            msgCounter++;
            Assert.assertEquals(testString1, event.getMessage());
            break;
        case 1:
            msgCounter++;
            Assert.assertEquals(testString2, event.getMessage());
            break;
        case 2:
            msgCounter++;
            Assert.assertEquals(testString3, event.getMessage());
            break;
        }

    }
}
