/*
 * Report.java
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

import java.util.*;

/**
 * Formats the data of persons to HTML format.
 *
 * @author Esko Luontola, http://www.orfjackal.net/
 */
public class Report {

    /**
     * Index of the index page for <code>getPage()</code> and <code>getPageName()</code>.
     */
    public static final int INDEX_PAGE_NUMBER = 0;

    /**
     * The Persons who are included in this report.
     */
    private final Person[] persons;

    /**
     * How many hours each person has made per week. The data format is <code>personHours[personsIndex].get(weeksIndex)</code>.
     */
    private final Vector<Double>[] personHours;

    /**
     * How many hours of each work code has made per week. The data format is <code>codeHours.get(code).get(weeksIndex)</code>.
     */
    private final HashMap<String, Vector<Double>> codeHours;

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
     * @param persons persons to be included in the report; must be at least one.
     */
    public Report(Vector<Person> persons) {
        this(persons.toArray(new Person[persons.size()]));
    }

    /**
     * Creates a report from an array of Persons.
     *
     * @param persons persons to be included in the report; must be at least one.
     */
    public Report(Person[] persons) {
        if (persons.length == 0) {
            throw new IllegalArgumentException("Length of the array persons must be at least 1");
        }
        this.persons = persons;
        weeks = new Vector<Date>();
        personHours = new Vector[persons.length];
        for (int i = 0; i < personHours.length; i++) {
            personHours[i] = new Vector<Double>();
        }
        codeHours = new HashMap<String, Vector<Double>>();
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
        } while (weekEnd.getTime().before(end) || weekEnd.getTime().equals(end));
    }

    /**
     * Collects the data from the persons for one week and saves it for later use in the reports.
     *
     * @param weekStart the beginning of the week, must this week's first day at 00:00:00.
     * @param weekEnd   the end of the week, must be <em>next</em> week's first day at 00:00:00.
     */
    private void processWeek(Date weekStart, Date weekEnd) {
        weeks.add(weekStart);

        for (int i = 0; i < persons.length; i++) {
            Person person = persons[i];

            // count hours per person per week
            double d = person.getHours(weekStart, weekEnd);
            personHours[i].add(d);

            // initialize hours per code per week
            String[] codes = person.getCodes(weekStart, weekEnd);
            for (String code : codes) {
                // make sure that the code exists
                if (!codeHours.containsKey(code)) {
                    codeHours.put(code, new Vector<Double>());
                }
            }
        }

        // add weeks for each code
        for (Vector<Double> codeWeeks : codeHours.values()) {
            while (codeWeeks.size() < weeks.size()) {
                codeWeeks.add(0.0);
            }
        }

        for (int i = 0; i < persons.length; i++) {
            Person person = persons[i];

            // count hours per code per week
            String[] codes = person.getCodes(weekStart, weekEnd);
            for (String code : codes) {
                double d = person.getHours(weekStart, weekEnd, code);
                d = d + codeHours.get(code).get(weeks.size() - 1);
                codeHours.get(code).set(weeks.size() - 1, d);
            }
        }
    }

    /**
     * Returns how many pages this report will generate.
     */
    public int getPages() {
        return persons.length + 1;
    }

    /**
     * Returns the HTML code for the given page.
     *
     * @param page index of the page, from <code>0</code> to <code>getPages()-1</code>.
     * @return HTML code for the page, including the header and footer.
     */
    public String getPage(int page) {
        if (page == INDEX_PAGE_NUMBER) {
            if (HourParser.isPlainIndex()) {
                return getIndexPage() + "\n<br />\n" + getSummaryPage();
            } else {
                return getHeader() + getIndexPage() + "\n<br />\n" + getSummaryPage() + getFooter();
            }
        } else {
            return getHeader() + getPersonPage(page - 1) + getFooter();
        }
    }

    /**
     * Returns the file name for the given page.
     *
     * @param page index of the page, from <code>0</code> to <code>getPages()-1</code>.
     * @return file name for the page.
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
     */
    private String getIndexPageName() {
        return HourParser.getNamePrefix() + HourParser.getNameSuffix();
    }

    /**
     * Returns the HTML code for the page with the hours listed by person and week.
     *
     * @return HTML code for the page. Does not include the header or footer.
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
            html.append("   <th style=\"width: 5ex;\">" + cal.get(Calendar.WEEK_OF_YEAR) + "</th>\n");
        }
        html.append("   <th style=\"padding: 0ex 1ex 0ex 1ex;\">Total</th>\n");
        html.append("</tr>\n");

        // each person's total hours for every week
        int row;
        for (row = 0; row < persons.length; row++) {
            if (row % 2 == 0) {
                html.append("<tr style=\"background-color: #DDDDDD;\">\n");
            } else {
                html.append("<tr style=\"background-color: #EEEEEE;\">\n");
            }
            html.append("   <td><a href=\"" + HourParser.getBaseUrl() + getPageName(row + 1) + "\">"
                    + persons[row].getName() + "</a></td>\n");

            double totalSum = 0.0;
            for (int i = 0; i < personHours[row].size(); i++) {
                html.append("   <td align=\"center\"><a href=\""
                        + HourParser.getBaseUrl() + getPageName(row + 1) + "#" + i + "\">"
                        + HourParser.getNumberFormat().format(personHours[row].get(i)) + "</a></td>\n");
                totalSum += personHours[row].get(i);
            }
            html.append("   <td align=\"center\">" + HourParser.getNumberFormat().format(totalSum) + "&nbsp;h</td>\n");
            html.append("</tr>\n");
        }

        // weekly total hours
        {
            if (row % 2 == 0) {
                html.append("<tr style=\"background-color: #DDDDDD;\">\n");
            } else {
                html.append("<tr style=\"background-color: #EEEEEE;\">\n");
            }
            html.append("   <th align=\"left\">Total</th>\n");

            double totalSum = 0.0;
            for (int i = 0; i < weeks.size(); i++) {
                double weekSum = 0.0;
                for (Vector<Double> h : personHours) {
                    weekSum += h.get(i);
                }
                html.append(
                        "   <td align=\"center\"><b>" + HourParser.getNumberFormat().format(weekSum) + "</b></td>\n");
                totalSum += weekSum;
            }
            html.append(
                    "   <td align=\"center\"><b>" + HourParser.getNumberFormat().format(totalSum) + "&nbsp;h</b></td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        return html.toString();
    }

    /**
     * Returns the HTML code for the page with the hours listed by code and week.
     *
     * @return HTML code for the page. Does not include the header or footer.
     */
    private String getSummaryPage() {
        StringBuffer html = new StringBuffer();
        Calendar cal = Calendar.getInstance();

        // table header
        html.append("<table border=\"0\">\n");
        html.append("<tr>\n");
        html.append("   <th style=\"padding: 0ex 3ex 0ex 3ex;\">Code / Week</th>\n");
        for (Date week : weeks) {
            cal.setTime(week);
            html.append("   <th style=\"width: 5ex;\">" + cal.get(Calendar.WEEK_OF_YEAR) + "</th>\n");
        }
        html.append("   <th style=\"padding: 0ex 1ex 0ex 1ex;\">Total</th>\n");
        html.append("</tr>\n");

        // sort the codes by order of appearance
        String[] codes = codeHours.keySet().toArray(new String[0]);
        Arrays.<String>sort(codes, new Comparator<String>() {
            public int compare(String s, String s1) {
                Vector<Double> hours1 = codeHours.get(s);
                Vector<Double> hours2 = codeHours.get(s1);

                // find out which of the codes started collecting hours first
                for (int i = 0; i < hours1.size() && i < hours2.size(); i++) {
                    Double d1 = hours1.get(i);
                    Double d2 = hours1.get(i);
                    if (d1.compareTo(d2) != 0) {
                        return d1.compareTo(d2);
                    }
                }
                return 0;
            }
        });

        // each code's total hours for every week
        int row;
        for (row = 0; row < codes.length; row++) {
            if (row % 2 == 0) {
                html.append("<tr style=\"background-color: #DDDDDD;\">\n");
            } else {
                html.append("<tr style=\"background-color: #EEEEEE;\">\n");
            }
            html.append("   <td>" + codes[row] + "</td>\n");

            double totalSum = 0.0;
            for (int i = 0; i < codeHours.get(codes[row]).size(); i++) {
                html.append("   <td align=\"center\">"
                        + HourParser.getNumberFormat().format(codeHours.get(codes[row]).get(i)) + "</td>\n");
                totalSum += codeHours.get(codes[row]).get(i);
            }
            html.append("   <td align=\"center\">" + HourParser.getNumberFormat().format(totalSum) + "&nbsp;h</td>\n");
            html.append("</tr>\n");
        }

        // weekly total hours
        {
            if (row % 2 == 0) {
                html.append("<tr style=\"background-color: #DDDDDD;\">\n");
            } else {
                html.append("<tr style=\"background-color: #EEEEEE;\">\n");
            }
            html.append("   <th align=\"left\">Total</th>\n");

            double totalSum = 0.0;
            for (int i = 0; i < weeks.size(); i++) {
                double weekSum = 0.0;
                for (Vector<Double> h : codeHours.values()) {
                    weekSum += h.get(i);
                }
                html.append(
                        "   <td align=\"center\"><b>" + HourParser.getNumberFormat().format(weekSum) + "</b></td>\n");
                totalSum += weekSum;
            }
            html.append(
                    "   <td align=\"center\"><b>" + HourParser.getNumberFormat().format(totalSum) + "&nbsp;h</b></td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        return html.toString();
    }

    /**
     * Returns the HTML code for a person's summary page.
     *
     * @param person index of the person
     * @return HTML code for the page. Does not include the header or footer.
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
                html.append("   <td valign=\"top\">" + HourParser.getDateFormat().format(entry.getDate()) + "</td>\n");
                html.append("   <td valign=\"top\">" + entry.getCode() + "</td>\n");
                html.append(
                        "   <td valign=\"top\">" + HourParser.getNumberFormat().format(entry.getHours()) + "</td>\n");
                html.append("   <td valign=\"top\">" + entry.getComment() + "</td>\n");
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
