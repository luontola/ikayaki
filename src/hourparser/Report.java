package hourparser;

import java.util.Vector;
import java.util.Date;
import java.util.Calendar;

/**
 * Formats the data of persons to HTML format.
 */
public class Report {

    /**
     * The Persons who are included in this report.
     */
    private final Person[] persons;

    /**
     * How many hours each person has made per week. The data format is <code>hours[personsIndex].get(weeksIndex)</code>.
     */
    private final Vector<Double>[] hours;

    /**
     * The beginnings of each week.
     */
    private final Vector<Date> weeks;

    /**
     * Creates a report from a Vector of Persons.
     *
     * @param persons Persons to be included in the report; must be at least one
     */
    public Report(Vector<Person> persons) {
        this(persons.toArray(new Person[persons.size()]));
    }

    /**
     * Creates a report from an array of Persons.
     *
     * @param persons Persons to be included in the report; must be at least one
     */
    public Report(Person[] persons) {
        if (persons.length == 0) {
            throw new IllegalArgumentException("Length of the array persons must be at least 1");
        }
        this.persons = persons;
        weeks = new Vector<Date>();
        hours = new Vector[persons.length];
        for (int i = 0; i < hours.length; i++) {
            hours[i] = new Vector<Double>();
        }
        process();
    }

    /**
     * Collects the data from the persons and saves it for later use in the reports.
     */
    private void process() {
        // find out the beginning and end of the statistics
        Date start = persons[0].getStart();
        Date end = persons[0].getEnd();
        for (Person p : persons) {
            Date date;
            date = p.getStart();
            if (date.before(start)) {
                start = date;
            }
            date = p.getEnd();
            if (date.after(end)) {
                end = date;
            }
        }

        // get the beginning of the first week
        Calendar weekStart = Calendar.getInstance();
        weekStart.setTime(start);
        weekStart.set(Calendar.HOUR, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);
        while (weekStart.get(Calendar.DAY_OF_WEEK) != weekStart.getFirstDayOfWeek()) {
            weekStart.add(Calendar.DATE, -1);
        }

        // process each week
        Calendar weekEnd;
        do {
            weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DATE, 7);
            processWeek(weekStart.getTime(), weekEnd.getTime());
            weekStart.add(Calendar.DATE, 7);
        } while (weekEnd.getTime().before(end));

        System.out.println("persons = " + persons[0]);
        System.out.println("weeks = " + weeks);
        System.out.println("hours = " + hours[0]);
    }

    /**
     * Collects the data from the persons for one week and saves it for later use in the reports.
     *
     * @param weekStart The beginning of the week, must this week's first day at 00:00:00
     * @param weekEnd   The end of the week, must be <i>next</i> week's first day at 00:00:00
     */
    private void processWeek(Date weekStart, Date weekEnd) {
        weeks.add(weekStart);
        for (int i = 0; i < persons.length; i++) {
            Person person = persons[i];
            double d = person.getHours(weekStart, weekEnd);
            hours[i].add(d);
        }
    }

    /**
     * Returns how many pages this report will generate.
     *
     * @return Number of pages
     */
    public int getPages() {
        return persons.length + 1;
    }

    /**
     * Returns the HTML code for the given page.
     *
     * @param page Index of the page, from 0 to getPages()-1
     * @return HTML code for the page. Does not include headers or footers
     */
    public String getPage(int page) {
        if (page == 0) {
            return getIndexPage();
        } else {
            return getPersonPage(page - 1);
        }
    }

    /**
     * Returns the file name for the given page.
     *
     * @param page Index of the page, from 0 to getPages()-1
     * @return File name for the page
     */
    public String getPageName(int page) {
        if (page == 0) {
            return HourParser.getNamePrefix() + HourParser.getNameSuffix();
        } else {
            return HourParser.getNamePrefix() + "-" + page + HourParser.getNameSuffix();
        }
    }

    /**
     * Returns the HTML code for the index page.
     *
     * @return HTML code for the page. Does not include headers or footers
     */
    private String getIndexPage() {
        return null;
    }

    /**
     * Returns the HTML code for a person's summary page.
     *
     * @param person Index of the person
     * @return HTML code for the page. Does not include headers or footers
     */
    private String getPersonPage(int person) {
        return null;
    }


}
