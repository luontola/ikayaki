package hourparser;

import java.util.Vector;
import java.util.Date;
import java.util.Calendar;

/**
 * Formats the data of persons to HTML format.
 *
 * @author Esko Luontola, http://www.orfjackal.net/
 */
public class Report {

    /**
     * Index of the index page for <code>getPage()</code> and <code>getPageName()</code>
     */
    public static final int INDEX_PAGE_NUMBER = 0;

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

    /* Header and footer for the reports */
    private String header = "";
    private String footer = "";

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
    }

    /**
     * Collects the data from the persons for one week and saves it for later use in the reports.
     *
     * @param weekStart The beginning of the week, must this week's first day at 00:00:00
     * @param weekEnd   The end of the week, must be <em>next</em> week's first day at 00:00:00
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
     * @param page Index of the page, from <code>0</code> to <code>getPages()-1</code>
     * @return HTML code for the page, including the header and footer
     */
    public String getPage(int page) {
        if (page == INDEX_PAGE_NUMBER) {
            if (HourParser.isPlainIndex()) {
                return getIndexPage();
            } else {
                return getHeader() + getIndexPage() + getFooter();
            }
        } else {
            return getHeader() + getPersonPage(page - 1) + getFooter();
        }
    }

    /**
     * Returns the file name for the given page.
     *
     * @param page Index of the page, from <code>0</code> to <code>getPages()-1</code>
     * @return File name for the page
     */
    public String getPageName(int page) {
        if (page == INDEX_PAGE_NUMBER) {
            if (HourParser.isPlainIndex()) {
                return HourParser.getNamePrefix() + "-plain" + HourParser.getNameSuffix();
            } else {
                return getIndexPageName();
            }
        } else {
            return HourParser.getNamePrefix() + "-" + page + HourParser.getNameSuffix();
        }
    }

    /**
     * Returns the file name for the index page.
     *
     * @return File name for the page
     */
    private String getIndexPageName() {
        return HourParser.getNamePrefix() + HourParser.getNameSuffix();
    }

    /**
     * Returns the HTML code for the index page.
     *
     * @return HTML code for the page. Does not include the header or footer
     */
    private String getIndexPage() {
        StringBuffer html = new StringBuffer();
        Calendar cal = Calendar.getInstance();

        // table header
        html.append("<table border=\"0\">\n");
        html.append("<tr>\n");
        html.append("   <th style=\"padding: 0ex 3ex 0ex 3ex;\">Name / Week</th>\n");
        for (Date week : weeks) {
            cal.setTime(week);
            html.append("   <th style=\"width: 4.5ex;\">" + cal.get(Calendar.WEEK_OF_YEAR) + "</th>\n");
        }
        html.append("   <th style=\"padding: 0ex 1ex 0ex 1ex;\">Total</th>\n");
        html.append("</tr>\n");

        // each person's total hours for every week
        for (int i = 0; i < persons.length; i++) {
            if (i % 2 == 0) {
                html.append("<tr style=\"background-color: #DDDDDD;\">\n");
            } else {
                html.append("<tr style=\"background-color: #EEEEEE;\">\n");
            }
            html.append("   <td><a href=\"" + HourParser.getBaseUrl() + getPageName(i + 1) + "\">"
                    + persons[i].getName() + "</a></td>\n");

            double totalSum = 0.0;
            for (int j = 0; j < hours[i].size(); j++) {
                html.append("   <td align=\"center\"><a href=\""
                        + HourParser.getBaseUrl() + getPageName(i + 1) + "#" + j + "\">"
                        + HourParser.getNumberFormat().format(hours[i].get(j)) + "</a></td>\n");
                totalSum += hours[i].get(j);
            }
            html.append("   <td align=\"center\">" + HourParser.getNumberFormat().format(totalSum) + " h</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        return html.toString();
    }

    /**
     * Returns the HTML code for a person's summary page.
     *
     * @param person Index of the person
     * @return HTML code for the page. Does not include the header or footer
     */
    private String getPersonPage(int person) {
        StringBuffer html = new StringBuffer();
        Calendar cal = Calendar.getInstance();
        double totalSum = 0.0;

        html.append("<h2>" + persons[person].getName() + "</h2>\n\n");
        html.append("<p><a href=\"" + HourParser.getBaseUrl() + getIndexPageName() + "\">Return to index</a></p>\n\n");

        // print entries for each week
        for (int i = 0; i < weeks.size(); i++) {
            cal.setTime(weeks.get(i));
            html.append("<h3><a name=\"" + i + "\"></a>Week " + cal.get(Calendar.WEEK_OF_YEAR)
                    + ", " + cal.get(Calendar.YEAR) + "</h3>\n\n");

            cal.add(Calendar.DATE, 7);
            Date start = weeks.get(i);
            Date end = cal.getTime();
            Entry[] entries = persons[person].getEntries(start, end);
            double weekSum = 0.0;

            html.append("<table border=\"0\">\n");
            html.append("<tr>\n");
            html.append("   <th align=\"left\" style=\"padding-right: 7ex;\">Date</th>\n");
            html.append("   <th align=\"left\" style=\"padding-right: 2ex;\">Code</th>\n");
            html.append("   <th align=\"left\" style=\"padding-right: 2ex;\">Hours</th>\n");
            html.append("   <th align=\"left\">Comment</th>\n");
            html.append("</tr>\n");
            for (Entry entry : entries) {
                html.append("<tr>\n");
                html.append("   <td>" + HourParser.getDateFormat().format(entry.getDate()) + "</td>\n");
                html.append("   <td>" + entry.getCode() + "</td>\n");
                html.append("   <td>" + HourParser.getNumberFormat().format(entry.getHours()) + "</td>\n");
                html.append("   <td>" + entry.getComment() + "</td>\n");
                html.append("</tr>\n");
                weekSum += entry.getHours();
            }
            html.append("</table>\n\n");

            html.append("<p><i>Week total: " + HourParser.getNumberFormat().format(weekSum) + " h</i></p>\n\n");
            totalSum += weekSum;
        }

        html.append("<p><b>Total: " + HourParser.getNumberFormat().format(totalSum) + " h</b></p>\n\n");
        html.append("<p><a href=\"" + HourParser.getBaseUrl() + getIndexPageName() + "\">Return to index</a></p>\n");
        html.append("<p><i>Generated with <a href=\"" + HourParser.APP_HOME_PAGE + "\">"
                + HourParser.APP_NAME + " " + HourParser.APP_VERSION + "</a></i></p>\n");

        return html.toString();
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }
}
