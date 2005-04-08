package ikayaki.gui;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;

/**
 * Decimal format for only positive decimal numbers. Will not show negative numbers. An empty string equals -1.
 *
 * @author Esko Luontola
 */
public class PositiveDecimalFormat extends DecimalFormat {

    @Override public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        if (number < 0.0) {
            return result;
        }
        return super.format(number, result, fieldPosition);
    }

    @Override public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
        if (number < 0) {
            return result;
        }
        return super.format(number, result, fieldPosition);
    }

    @Override public Object parseObject(String source) throws ParseException {
        if (source.equals("")) {
            return new Long(-1);
        }
        return super.parseObject(source);
    }
}
