package hourparser;

import java.util.Vector;
import java.util.Date;
import java.util.Calendar;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Formats the data of persons to HTML format.
 */
public class Report {

    /**
     * Index of the index page for getPage() and getPageName()
     */
    public static final int INDEX_PAGE = 0;

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
        if (page == INDEX_PAGE) {
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
        if (page == INDEX_PAGE) {
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
        StringBuffer html = new StringBuffer();
        Calendar cal = Calendar.getInstance();

        // formatters
        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            ((DecimalFormat) nf).setDecimalSeparatorAlwaysShown(false);
        }

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
            html.append("<tr>\n");
            html.append("   <td>" + persons[i].getName() + "</td>\n");
            double sum = 0.0;
            for (int j = 0; j < hours[i].size(); j++) {
                html.append("   <td align=\"center\"><a href=\"" + getPageName(i + 1) + "#" + j + "\">" + nf.format(hours[i].get(j)) + "</a></td>\n");
                sum += hours[i].get(j);
            }
            html.append("   <td align=\"center\">" + nf.format(sum) + "</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        return html.toString();
    }

    /**
     * Returns the HTML code for a person's summary page.
     *
     * @param person Index of the person
     * @return HTML code for the page. Does not include headers or footers
     */
    private String getPersonPage(int person) {
        StringBuffer html = new StringBuffer();
        Calendar cal = Calendar.getInstance();

        // formatters
        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            ((DecimalFormat) nf).setDecimalSeparatorAlwaysShown(false);
        }
        DateFormat df = new SimpleDateFormat(HourParser.getDateFormat());

        html.append("<h1>" + persons[person].getName() + "</h1>\n\n");
        html.append("<p><a href=\"" + getPageName(INDEX_PAGE) + "\">Return to index</a></p>\n\n");

        // print entries for each week
        for (int i = 0; i < weeks.size(); i++) {
            cal.setTime(weeks.get(i));
            html.append("<h2><a name=\"" + i + "\"></a>Week " + cal.get(Calendar.WEEK_OF_YEAR) + "</h2>\n\n");

            cal.add(Calendar.DATE, 7);
            Date start = weeks.get(i);
            Date end = cal.getTime();
            Entry[] entries = persons[person].getEntries(start, end);

            html.append("<table border=\"0\">\n");
            html.append("<tr>\n");
            html.append("   <th align=\"left\" style=\"padding-right: 7ex;\">Date</th>\n");
            html.append("   <th align=\"left\" style=\"padding-right: 2ex;\">Code</th>\n");
            html.append("   <th align=\"left\" style=\"padding-right: 2ex;\">Hours</th>\n");
            html.append("   <th align=\"left\">Comment</th>\n");
            html.append("</tr>\n");
            for (Entry entry : entries) {
                html.append("<tr>\n");
                html.append("   <td>" + df.format(entry.getDate()) + "</td>\n");
                html.append("   <td>" + entry.getCode() + "</td>\n");
                html.append("   <td>" + nf.format(entry.getHours()) + "</td>\n");
                html.append("   <td>" + entry.getComment() + "</td>\n");
                html.append("</tr>\n");
            }
            html.append("</table>\n\n");
        }
        html.append("<p><a href=\"" + getPageName(INDEX_PAGE) + "\">Return to index</a></p>\n");

        return html.toString();
    }


}
