package hourparser;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Reads log files that include how many hours each person has made work and creates reports from them. Takes the file
 * names of the log files as commandline parameter.
 */
public class HourParser {

    private static Vector<Person> persons;

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        // read all the files given as program parameters
        persons = new Vector<Person>();
        for (int i = 0; i < args.length; i++) {
            try {
                persons.add(new Person(new File(args[i])));
            } catch (IOException e) {
                System.err.println("Error reading file " + args[i]);
                e.printStackTrace();
                System.exit(1);
            }
        }

        // build reports
        // TODO
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar HourParser.jar [files...]");
    }

}
