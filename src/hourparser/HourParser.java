package hourparser;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

/**
 * Reads log files that include how many hours each person has made work and creates reports from them. Takes the file
 * names of the log files as commandline parameter.
 *
 * @author Esko Luontola, http://www.orfjackal.net/
 */
public class HourParser {

    /* Application information */
    public static final String APP_NAME = "HourParser";
    public static final String APP_VERSION = "1.02 CVS";
    public static final String APP_HOME_PAGE = "http://www.cs.helsinki.fi/group/squid/";
    public static final String COMMAND_LINE = "java -jar HourParser.jar";

    /* Settings */
    private static File headerFile = null;
    private static File footerFile = null;
    private static String outputDir = "." + File.separator;
    private static String baseUrl = "./";
    private static boolean plainIndex = false;
    private static String namePrefix = "hours";
    private static String nameSuffix = ".html";
    private static String dateFormat = "d.M.yyyy";
    private static Locale locale = Locale.getDefault();

    /* Cached format instances */
    private static DateFormat dateFormatInstance = null;
    private static NumberFormat numberFormatInstance = null;

    /**
     * Starts the program from command line.
     *
     * @param args Command line parameters
     */
    public static void main(String[] args) {
        // parse the command line parameters
        int arg;
        for (arg = 0; arg < args.length; arg++) {
            if (args[arg].startsWith("--help")) {
                printHelp();
                return;
            } else if (args[arg].startsWith("--version")) {
                printVersion();
                return;
            } else if (args[arg].startsWith("--header-file=")) {
                String param = args[arg].substring("--header-file=".length());
                setHeaderFile(param);
            } else if (args[arg].startsWith("--footer-file=")) {
                String param = args[arg].substring("--footer-file=".length());
                setFooterFile(param);
            } else if (args[arg].startsWith("--base-url=")) {
                String param = args[arg].substring("--base-url=".length());
                setBaseUrl(param);
            } else if (args[arg].startsWith("--output-dir=")) {
                String param = args[arg].substring("--output-dir=".length());
                setOutputDir(param);
            } else if (args[arg].equals("--plain-index")) {
                setPlainIndex(true);
            } else if (args[arg].startsWith("--name-prefix=")) {
                String param = args[arg].substring("--name-prefix=".length());
                setNamePrefix(param);
            } else if (args[arg].startsWith("--name-suffix=")) {
                String param = args[arg].substring("--name-suffix=".length());
                setNameSuffix(param);
            } else if (args[arg].startsWith("--date-format=")) {
                String param = args[arg].substring("--date-format=".length());
                setDateFormat(param);
            } else if (args[arg].startsWith("--locale=")) {
                String param = args[arg].substring("--locale=".length());
                setLocale(param);
            } else {
                // beginning of input file parameters
                break;
            }
        }

        // read all the input files given as parameters
        Vector<Person> persons = new Vector<Person>();
        for (; arg < args.length; arg++) {
            try {
                persons.add(new Person(new File(args[arg])));
            } catch (IOException e) {
                System.err.println("Unable to read file " + args[arg]);
                System.exit(1);
            }
        }

        // print help if there were no input files
        if (persons.size() == 0) {
            printHelp();
            return;
        }

        // read header and footer from file
        String header = "";
        String footer = "";
        try {
            BufferedReader reader;

            if (getHeaderFile() != null) {
                reader = new BufferedReader(new FileReader(getHeaderFile()));
                StringBuffer sb = new StringBuffer();
                String s;
                while ((s = reader.readLine()) != null) {
                    sb.append(s).append("\n");
                }
                reader.close();
                header = sb.toString();
            }

            if (getFooterFile() != null) {
                reader = new BufferedReader(new FileReader(getFooterFile()));
                StringBuffer sb = new StringBuffer();
                String s;
                while ((s = reader.readLine()) != null) {
                    sb.append(s).append("\n");
                }
                reader.close();
                footer = sb.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // tidy up the base url and output dir before they are used
        if (!getBaseUrl().endsWith("/")) {
            setBaseUrl(getBaseUrl() + "/");
        }
        if (getBaseUrl().equals("./")) {
            setBaseUrl("");
        }
        if (!getOutputDir().endsWith("/") && !getOutputDir().endsWith("\\")) {
            setOutputDir(getOutputDir() + File.separator);
        }

        // make reports
        Report report = new Report(persons);
        report.setHeader(header);
        report.setFooter(footer);
        for (int i = 0; i < report.getPages(); i++) {
            String page = report.getPage(i);
            String pageName = report.getPageName(i);

            // write reports to output files
            File file = new File(getOutputDir() + pageName);
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file, false);
                writer.write(page);
                writer.close();
            } catch (IOException e) {
                System.err.println("Unable to write file " + file);
                System.exit(1);
            }
        }
    }

    private static void printHelp() {
        System.out.println("Usage: " + COMMAND_LINE + " [OPTION]... [FILE]...");
        System.out.println("Reads reported hours from specified files and writes a summary in HTML format.");
        System.out.println();
        System.out.println("  --header-file=FILE      header for the output files");
        System.out.println("  --footer-file=FILE      footer for the output files");
        System.out.println("  --base-url=URL          base url for the html links (default: " + getBaseUrl() + ")");
        System.out.println("  --output-dir=DIR        directory where the output files are saved (default: "
                + getOutputDir() + ")");
        System.out.println("  --plain-index           generate only a plain index for inclusion");
        System.out.println("  --name-prefix=STRING    prefix for the output file names (default: "
                + getNamePrefix() + ")");
        System.out.println("  --name-suffix=STRING    suffix for the output file names (default: "
                + getNameSuffix() + ")");
        System.out.println("  --date-format=FORMAT    format of the dates in input files (default: "
                + dateFormat + ")");
        System.out.println("  --locale=LOCALE         sets the system locale (default: " + getLocale() + ")");
        System.out.println("  --help                  display this help and exit");
        System.out.println("  --version               output version information and exit");
    }

    private static void printVersion() {
        System.out.println(APP_NAME + " " + APP_VERSION + " <" + APP_HOME_PAGE + ">");
        System.out.println("Written by Esko Luontola <http://www.orfjackal.net/>");
    }

    public static File getHeaderFile() {
        return headerFile;
    }

    public static void setHeaderFile(String headerFile) {
        File file = new File(headerFile);
        if (file.exists() && file.canRead()) {
            HourParser.headerFile = file;
        } else {
            System.err.println("Unable to read header file " + file);
            System.exit(1);
        }
    }

    public static File getFooterFile() {
        return footerFile;
    }

    public static void setFooterFile(String footerFile) {
        File file = new File(footerFile);
        if (file.exists() && file.canRead()) {
            HourParser.footerFile = file;
        } else {
            System.err.println("Unable to read footer file " + file);
            System.exit(1);
        }
    }

    public static String getOutputDir() {
        return outputDir;
    }

    public static void setOutputDir(String outputDir) {
        HourParser.outputDir = outputDir;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String baseUrl) {
        HourParser.baseUrl = baseUrl;
    }

    public static boolean isPlainIndex() {
        return plainIndex;
    }

    public static void setPlainIndex(boolean plainIndex) {
        HourParser.plainIndex = plainIndex;
    }

    public static String getNamePrefix() {
        return namePrefix;
    }

    public static void setNamePrefix(String namePrefix) {
        HourParser.namePrefix = namePrefix;
    }

    public static String getNameSuffix() {
        return nameSuffix;
    }

    public static void setNameSuffix(String nameSuffix) {
        HourParser.nameSuffix = nameSuffix;
    }

    public static NumberFormat getNumberFormat() {
        if (numberFormatInstance == null) {
            numberFormatInstance = NumberFormat.getInstance();
            if (numberFormatInstance instanceof DecimalFormat) {
                ((DecimalFormat) numberFormatInstance).setDecimalSeparatorAlwaysShown(false);
            }
            numberFormatInstance.setMaximumFractionDigits(1);
        }
        return numberFormatInstance;
    }

    public static DateFormat getDateFormat() {
        if (dateFormatInstance == null) {
            dateFormatInstance = new SimpleDateFormat(dateFormat);
        }
        return dateFormatInstance;
    }

    public static void setDateFormat(String dateFormat) {
        HourParser.dateFormat = dateFormat;
        dateFormatInstance = null;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(String code) {
        String[] s = code.split("_");
        if (s.length == 1) {
            locale = new Locale(s[0]);
        } else if (s.length == 2) {
            locale = new Locale(s[0], s[1]);
        } else if (s.length == 3) {
            locale = new Locale(s[0], s[1], s[2]);
        } else {
            System.err.println("Invalid locale " + code);
            System.exit(1);
        }
        Locale.setDefault(locale);
    }
}
