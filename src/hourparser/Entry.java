package hourparser;

import java.util.Scanner;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Represents one row in a log file. The format of the log file must be this:
 * <pre>dd.mm.yyyy   CODE    hours    comment</pre>
 * <p/>
 * A row that starts with an alphabet will be used as the name of the person. Rows beginning with # will be concidered
 * empty lines.
 */
public class Entry implements Comparable<Entry> {

    private enum State {
        EMPTY, NAME, RECORD
    }

    /* What kind of entry this is */
    private State state;

    /* Set if this is a name entry */
    private String name;

    /* Set if this is a record entry */
    private Date date;
    private String code;
    private double hours;
    private String comment;

    /**
     * Parses a row from a log file.
     *
     * @param row One row from a log file
     */
    public Entry(String row) {
        Scanner sc = new Scanner(row);

        // decide what kind of row we are talking about
        if (!sc.hasNext()) {
            // empty line
            state = State.EMPTY;

        } else if (sc.hasNext("#.*")) {
            // comment
            state = State.EMPTY;

        } else if (sc.hasNext("\\D.*")) {
            // name of the person (starts with non-digit)
            state = State.NAME;
            name = row.trim();

        } else {
            // record
            state = State.RECORD;

            // parse date
            String s = sc.next();
            DateFormat df = new SimpleDateFormat("d.M.yyyy");
            try {
                date = df.parse(s);
            } catch (ParseException e) {
                System.err.println("Error in parsing the date in row: " + row);
                state = null;
                return;
            }

            // parse code
            try {
                code = sc.next();
            } catch (Exception e) {
                System.err.println("Error in reading the code in row: " + row);
                state = null;
                return;
            }

            // parse hours
            try {
                hours = sc.nextDouble();
            } catch (Exception e) {
                System.err.println("Error in parsing the hours in row: " + row);
                state = null;
                return;
            }

            // parse comment
            try {
                comment = sc.nextLine().trim();
            } catch (Exception e) {
                comment = "";
            }
        }
    }

    /**
     * Is this entry an empty line or a comment
     *
     * @return true if is empty or a comment
     */
    public boolean isEmpty() {
        return state == State.EMPTY;
    }

    /**
     * Is this entry the name of the person
     *
     * @return true if is a name
     */
    public boolean isName() {
        return state == State.NAME;
    }

    /**
     * Is this entry a record of work
     *
     * @return true if is a record
     */
    public boolean isRecord() {
        return state == State.RECORD;
    }


    public String getName() {
        if (isName()) {
            return name;
        } else {
            return null;
        }
    }

    public Date getDate() {
        if (isRecord()) {
            return date;
        } else {
            return null;
        }
    }

    public String getCode() {
        if (isRecord()) {
            return code;
        } else {
            return null;
        }
    }

    public double getHours() {
        if (isRecord()) {
            return hours;
        } else {
            return 0.0;
        }
    }

    public String getComment() {
        if (isRecord()) {
            return comment;
        } else {
            return null;
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Entry:");
        if (state == State.RECORD) {
            s.append(" Date=" + getDate().toString());
            s.append(", Code=" + getCode());
            s.append(", Hours=" + getHours());
            s.append(", Comment=" + getComment());
        } else if (state == State.NAME) {
            s.append(" Name=" + getName());
        } else if (state == State.EMPTY) {
            s.append(" Empty");
        }
        return s.toString();
    }

    /**
     * Compares this Entry with the specified Entry for order.
     *
     * @param entry the Entry to be compared.
     * @return the value 0 if the argument's date is equal to this; a value less than 0 if this is before the argument's
     *         date; and a value greater than 0 if this is after the argument's date.
     */
    public int compareTo(Entry entry) {
        return this.getDate().compareTo(entry.getDate());
    }

}
