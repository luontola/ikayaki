package ikayaki.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PrintStream for directing the output to another PrintStream and a OutputStream. Can be used for printing System.err
 * to screen and to a log file. Writes timestamps for each printed line.
 *
 * @author Esko Luontola
 */
public class LoggerPrintStream extends PrintStream {

    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    private boolean lineStart = true;

    private PrintStream screen;

    /**
     * Creates a new LoggerPrintStream.
     *
     * @param message a message to be printed at the creaton of this print stream, or null to show no message. This will
     *                not be timestamped.
     * @param screen  a PrintStream to direct all output with timestamps.
     * @param file    an OutputStream to direct all output with timestamps.
     */
    public LoggerPrintStream(PrintStream screen, OutputStream file, String message) {
        super(file);
        this.screen = screen;
        if (message != null) {
            screen.print(message);
            screen.println();
            super.print(message);
            super.println();
        }
    }

    private void timestamp() {
        if (lineStart) {
            String timestamp = dateFormat.format(new Date()) + " -- ";
            screen.print(timestamp);
            super.print(timestamp);
        }
        lineStart = false;
    }

    @Override public void print(Object obj) {
        timestamp();
        screen.print(obj);
        super.print(obj);
    }

    @Override public void print(String s) {
        timestamp();
        screen.print(s);
        super.print(s);
    }

    @Override public void println() {
        screen.println();
        super.println();
        lineStart = true;
    }

    @Override public void println(Object obj) {
        this.print(obj);
        this.println();
    }

    @Override public void println(String s) {
        this.print(s);
        this.println();
    }

}
