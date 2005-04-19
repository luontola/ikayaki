/*
 * Entry.java
 *
 * Copyright (C) 2005 Project SQUID, http://www.cs.helsinki.fi/group/squid/
 *
 * This file is part of HourParser.
 *
 * HourParser is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * HourParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HourParser; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package hourparser;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/**
 * Represents one row in a log file. The format of the log file is like this:
 * <pre>dd.mm.yyyy   code    hours    comment</pre>
 * <p/>
 * A row that starts with an alphabet will be used as the name of the person. Rows beginning with # will be concidered
 * empty lines.
 *
 * @author Esko Luontola, http://www.orfjackal.net/
 */
public class Entry {

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
            // EMPTY LINE
            state = State.EMPTY;

        } else if (sc.hasNext("#.*")) {
            // COMMENT
            state = State.EMPTY;

        } else if (sc.hasNext("\\D.*")) {
            // NAME OF THE PERSON (starts with non-digit)
            state = State.NAME;
            name = row.trim();

        } else {
            // RECORD
            state = State.RECORD;

            // parse date
            String s = sc.next();
            try {
                date = HourParser.getDateFormat().parse(s);

                // if the date format includes no year, then assume the current year
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(0)); // beginning of UNIX time or something
                int historicalYear = cal.get(Calendar.YEAR); // should be 1970
                cal.setTime(date);
                if (cal.get(Calendar.YEAR) == historicalYear) {
                    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR)); // set to this year
                    date = cal.getTime();
                }
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
     * @return <code>true</code> if is empty or a comment, <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return state == State.EMPTY;
    }

    /**
     * Is this entry the name of the person
     *
     * @return <code>true</code> if is a name, <code>false</code> otherwise
     */
    public boolean isName() {
        return state == State.NAME;
    }

    /**
     * Is this entry a record of work
     *
     * @return <code>true</code> if is a record, <code>false</code> otherwise
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
}
