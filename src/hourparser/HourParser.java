package hourparser;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Locale;

/**
 * Reads log files that include how many hours each person has made work and creates reports from them. Takes the file
 * names of the log files as commandline parameter.
 */
public class HourParser {

    private static String headerFile = null;
    private static String footerFile = null;
    private static String namePrefix = "hours";
    private static String nameSuffix = ".html";
    private static String dateFormat = "d.M.yyyy";
    private static Locale locale = Locale.getDefault();

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
                System.err.println("Error reading file " + args[arg]);
                e.printStackTrace();
                System.exit(1);
            }
        }

        // print help if there were no input files
        if (persons.size() == 0) {
            printHelp();
            return;
        }

        // make reports
        Report report = new Report(persons);
        
        // TODO
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar HourParser.jar [OPTION]... [FILE]...");
        System.out.println("Reads reported hours from specified files and writes a summary in HTML format.");
        System.out.println();
        System.out.println("  --header-file=FILE      header for the output files");
        System.out.println("  --footer-file=FILE      footer for the output files");
        System.out.println("  --name-prefix=STRING    prefix for the output file names (default: " + getNamePrefix() + ")");
        System.out.println("  --name-suffix=STRING    suffix for the output file names (default: " + getNameSuffix() + ")");
        System.out.println("  --date-format=FORMAT    format of the dates in input files (default: " + getDateFormat() + ")");
        System.out.println("  --locale=LOCALE         sets the system locale (default: " + getLocale() + ")");
        System.out.println("  --help                  display this help and exit");
        System.out.println("  --version               output version information and exit");
    }

    private static void printVersion() {
        System.out.println("HourParser 1.0");
        System.out.println("Written by Esko Luontola.");
    }

    public static String getHeaderFile() {
        return headerFile;
    }

    public static void setHeaderFile(String headerFile) {
        HourParser.headerFile = headerFile;
    }

    public static String getFooterFile() {
        return footerFile;
    }

    public static void setFooterFile(String footerFile) {
        HourParser.footerFile = footerFile;
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

    public static String getDateFormat() {
        return dateFormat;
    }

    public static void setDateFormat(String dateFormat) {
        HourParser.dateFormat = dateFormat;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(String code) {
        String[] s = code.split("_");
        if (s.length == 1) {
            HourParser.locale = new Locale(s[0]);
        } else if (s.length == 2) {
            HourParser.locale = new Locale(s[0], s[1]);
        } else if (s.length == 3) {
            HourParser.locale = new Locale(s[0], s[1], s[2]);
        } else {
            System.err.println("Invalid locale: " + code);
            System.exit(1);
        }
        Locale.setDefault(HourParser.locale);
    }
}
