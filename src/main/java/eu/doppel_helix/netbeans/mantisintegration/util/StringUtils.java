
package eu.doppel_helix.netbeans.mantisintegration.util;

import java.util.Arrays;
import java.util.Collection;

public class StringUtils {
    public static String toCustomFieldList(Collection<String> list) {
        StringBuilder result = new StringBuilder();
        for (String element : list) {
            if (result.length() != 0) {
                result.append("|");
            }
            result.append(element);
        }
        return result.toString();
    }

    public static String toCustomFieldList(String... list) {
        return toCustomFieldList(Arrays.asList(list));
    }
}
