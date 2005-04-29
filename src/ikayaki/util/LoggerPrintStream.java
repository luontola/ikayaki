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
     * Creates a timestamped print stream directed to one output.
     *
     * @param out an OutputStream to direct all output with timestamps.
     */
    public LoggerPrintStream(OutputStream out) {
        this(out, null, null);
    }

    /**
     * Creates a timestamped print stream directed to two outputs.
     *
     * @param out    an OutputStream to direct all output with timestamps.
     * @param screen a PrintStream to direct all output with timestamps. Will be ignored if null.
     */
    public LoggerPrintStream(OutputStream out, PrintStream screen) {
        this(out, screen, null);
    }

    /**
     * Creates a timestamped print stream directed to two outputs with a startup message.
     *
     * @param out     an OutputStream to direct all output with timestamps.
     * @param screen  a PrintStream to direct all output with timestamps. Will be ignored if null.
     * @param message a message to be printed at the creaton of this print stream. This will not be timestamped. Will be
     *                ignored if null.
     */
    public LoggerPrintStream(OutputStream out, PrintStream screen, String message) {
        super(out);
        this.screen = screen;
        if (message != null) {
            if (screen != null) {
                screen.print(message);
                screen.println();
            }
            super.print(message);
            super.println();
        }
    }

    private void timestamp() {
        if (lineStart) {
            String timestamp = dateFormat.format(new Date()) + " -- ";
            if (screen != null) {
                screen.print(timestamp);
            }
            super.print(timestamp);
        }
        lineStart = false;
    }

    @Override public void print(Object obj) {
        timestamp();
        if (screen != null) {
            screen.print(obj);
        }
        super.print(obj);
    }

    @Override public void print(String s) {
        timestamp();
        if (screen != null) {
            screen.print(s);
        }
        super.print(s);
    }

    @Override public void println() {
        if (screen != null) {
            screen.println();
        }
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
