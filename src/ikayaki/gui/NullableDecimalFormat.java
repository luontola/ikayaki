package ikayaki.gui;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Decimal format that accepts an empty string. An empty string equals null.
 * <p/>
 * If this class is used, it is not possible to use NumberFormatter's setMinimum and setMaximum values, because it will
 * create a NullPointerException.
 *
 * @author Esko Luontola
 */
public class NullableDecimalFormat extends DecimalFormat {

    @Override public Object parseObject(String source) throws ParseException {
        if (source.equals("")) {
            return null;
        }
        return super.parseObject(source);
    }
}
