
import java.util.Arrays;
import java.util.Set;

/**
 * Testitiedosto
 */
public class Test {
    public static void main(String[] args) {
        Set<Object> keyset = System.getProperties().keySet();
        Object[] keys = keyset.toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            System.out.println(key + " = " + System.getProperty(key.toString()));
        }
//        System.out.println("\u00b0");
    }
}
