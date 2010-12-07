package eu.vranckaert.episodeWatcher.utils;

/**
 * @author Dirk Vranckaert
 *         Date: 11-mei-2010
 *         Time: 18:29:04
 */
public class StringUtils {
    public static String EMPTY = "";

    /**
     * Search what the start-index of a certain search value in a source string is.
     * @param source The string to search in.
     * @param search The string to search for.
     * @return The index on which the search value has been found first. So for multiple ocurences only the result will
     * be the index of the first occurence in the source. Returns -1 if the search value hasn't been found!
     */
    public static int indexOf(String source, String search) {
        int result = -1;
        if(source.startsWith(search)) {
            result = 0;
        } else {
            String[] split = source.split(search);
            if(split.length > 0) {
                result = split[0].length();
            }
        }
        return result;
    }
}
