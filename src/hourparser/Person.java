package hourparser;

import java.io.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA. User: ORFJackal Date: 8.2.2005 Time: 23:02:57 To change this template use File | Settings |
 * File Templates.
 */
public class Person {

    private File file;
    private String name;
    private Vector<Entry> records;

    public Person(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("No such file " + file);
        }
        this.file = file;
        this.name = null;
        this.records = new Vector<Entry>();
        readFile();
    }

    private void readFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            Entry e = new Entry(line);
            if (e.isRecord()) {
                records.add(e);
            } else if (e.isName() && name == null) {
                name = e.getName();
            }
        }
//        System.out.println(name);
//        for (Entry r : records) {
//            System.out.println(r);
//        }
    }

}
