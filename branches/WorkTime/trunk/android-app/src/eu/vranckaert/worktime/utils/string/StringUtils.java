package eu.vranckaert.worktime.utils.string;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 19/02/11
 * Time: 18:32
 */
public class StringUtils {
    public static final String SPACE = " ";

    /**
     * Converts a list of {@link String} objects to an array.
     * @param strings The list to convert.
     * @return The concerted array containing the original {@link String} objects in the same order.
     */
    public static String[] convertListToArray(List<String> strings) {
        String[] stringArray = new String[strings.size()];
        for(int i = 0; i<strings.size(); i++) {
            stringArray[i] = strings.get(i);
        }
        return stringArray;
    }

    public static boolean isBlank(String string) {
        if(string == null || string.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }
}
