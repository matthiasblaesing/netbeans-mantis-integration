package eu.doppel_helix.netbeans.mantisintegration.swing;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;

public class TimeFormatterFactory extends AbstractFormatterFactory {

    private final WeakHashMap<JFormattedTextField,AbstractFormatter> cache =
            new WeakHashMap<>();

    @Override
    public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
        AbstractFormatter af = cache.get(tf);
        if(af != null) {
            return af;
        } else {
            af = new TimeFormatter();
            cache.put(tf, af);
            return af;
        }
    }

}

class TimeFormatter extends AbstractFormatter {
    Pattern patternCompound = Pattern.compile("(\\d+):(\\d+)");
    Pattern pattern = Pattern.compile("(\\d+)");
    
    @Override
    public Object stringToValue(String text) throws ParseException {
        if (text == null) {
            return BigInteger.ZERO;
        }
        Matcher m = patternCompound.matcher(text);
        Matcher m2 = pattern.matcher(text);
        if (m.find()) {
            String hourPart = m.group(1);
            String minutePart = m.group(2);
            int hours = Integer.valueOf(hourPart);
            int minutes = Integer.valueOf(minutePart);
            return BigInteger.valueOf(hours * 60 + minutes);
        } else if (m2.find()) {
            String minutesString = m2.group(1);
            BigInteger minutes = new BigInteger(minutesString);
            getFormattedTextField().setValue(minutes);
            return minutes;
        } else {
            return BigInteger.ZERO;
        }
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value instanceof BigInteger) {
            BigInteger[] parts = ((BigInteger) value).divideAndRemainder(BigInteger.valueOf(60));
            return String.format("%02d:%02d", parts[0], parts[1]);
        } else {
            return "00:00";
        }
    }
 
}
        