package hourparser;

import java.io.File;
import java.io.IOException;

/**
 * Reads log files that include how many hours each person has made work and creates reports from them. Takes the file
 * names of the log files as commandline parameter.
 */
public class HourParser {

    private static Person[] persons;

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }
        
        // read all the files given as program parameters
        persons = new Person[args.length];
        for (int i = 0; i < args.length; i++) {
            try {
                persons[i] = new Person(new File(args[i]));
            } catch (IOException e) {
                System.err.println("Error reading file " + args[i]);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java -jar HourParser.jar [files...]");
    }

}
