package hourparser;

import java.io.*;
import java.util.Date;
import java.util.Vector;

/**
 * Represents a person who has a personal log file.
 *
 * @author Esko Luontola, http://www.orfjackal.net/
 */
public class Person {

    /**
     * Personal log file of this person.
     */
    private File file;

    /**
     * Name of this person, or <code>null</code> if no name is known.
     */
    private String name;

    /**
     * Work records of this person.
     */
    private Vector<Entry> records;

    /**
     * Constructs a new person by reading the data from a log file.
     *
     * @param file The log file of the person
     * @throws IOException Reading the log file fails
     */
    public Person(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("No such file " + file);
        }
        this.file = file;
        this.name = null;
        this.records = new Vector<Entry>();
        readFile();
    }

    /**
     * Reads all the information from the the log file of this person.
     *
     * @throws IOException Reading the log file fails
     */
    private void readFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            Entry e = new Entry(line);
            if (e.isRecord()) {
                // sort list
                int insert = 0;
                Date thisDate = e.getDate();
                for (int i = records.size() - 1; i >= 0; i--) {
                    Date otherDate = records.get(i).getDate();
                    if (thisDate.after(otherDate) || thisDate.equals(otherDate)) {
                        insert = i + 1;
                        break;
                    }
                }
                records.add(insert, e);
            } else if (e.isName() && name == null) {
                name = e.getName();
            }
        }
        reader.close();
    }

    /**
     * Returns the name of this person as defined in the first row of the log file.
     *
     * @return The name of this person or <code>null</code> if no name is set
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the entries the person has made during a time period.
     *
     * @param start The beginning of the time period. Results are <code>&gt;= start</code>
     * @param end   The end of the time period. Results are <code>&lt; end</code>
     * @return Total hours of work during the time period
     */
    public Entry[] getEntries(Date start, Date end) {
        Vector<Entry> result = new Vector<Entry>();
        for (Entry e : records) {
            if ((e.getDate().after(start) || e.getDate().equals(start)) && e.getDate().before(end)) {
                result.add(e);
            }
        }
        return result.toArray(new Entry[result.size()]);
    }

    /**
     * Returns how many hours in total the person has made during a time period.
     *
     * @param start The beginning of the time period. Results are <code>&gt;= start</code>
     * @param end   The end of the time period. Results are <code>&lt; end</code>
     * @return Total hours of work during the time period
     */
    public double getHours(Date start, Date end) {
        Entry[] entries = getEntries(start, end);
        double sum = 0.0;
        for (Entry e : entries) {
            sum += e.getHours();
        }
        return sum;
    }

    /**
     * Returns how many hours of a specific work the person has made during a time period.
     *
     * @param start The beginning of the time period. Results are <code>&gt;= start</code>
     * @param end   The end of the time period. Results are <code>&lt; end</code>
     * @param code  The code of the work type to be included or <code>null</code> to include all
     * @return Total hours of work during the time period
     */
    public double getHours(Date start, Date end, String code) {
        if (code == null) {
            return getHours(start, end);
        }
        Entry[] entries = getEntries(start, end);
        double sum = 0.0;
        for (Entry e : entries) {
            if (e.getCode().equals(code)) {
                sum += e.getHours();
            }
        }
        return sum;
    }

    /**
     * Returns the time of the first record this person has.
     *
     * @return The time of the first record, or the current time if there are no records
     */
    public Date getStart() {
        if (records.size() == 0) {
            return new Date();
        } else {
            Date first = records.get(0).getDate();
            for (Entry e : records) {
                if (e.getDate().before(first)) {
                    first = e.getDate();
                }
            }
            return first;
        }
    }

    /**
     * Returns the time of the last record this person has.
     *
     * @return The time of the last record, or the current time if there are no records
     */
    public Date getEnd() {
        if (records.size() == 0) {
            return new Date();
        } else {
            Date last = records.get(0).getDate();
            for (Entry e : records) {
                if (e.getDate().after(last)) {
                    last = e.getDate();
                }
            }
            return last;
        }
    }

}
