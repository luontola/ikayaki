package hourparser;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class HourParser {

    private static Person[] persons;

    public static void main(String[] args) {

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
}
