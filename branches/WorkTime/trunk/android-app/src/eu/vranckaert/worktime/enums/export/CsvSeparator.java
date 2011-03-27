package eu.vranckaert.worktime.enums.export;

/**
 * User: DIRK VRANCKAERT
 * Date: 19/02/11
 * Time: 17:35
 */
public enum CsvSeparator {
    SEMICOLON(';');

    private char seperator;

    private CsvSeparator(char seperator) {
        this.seperator = seperator;
    }

    public char getSeperator() {
        return seperator;
    }

    public void setSeperator(char seperator) {
        this.seperator = seperator;
    }

    public static CsvSeparator matchFileType(String seperator) {
        for(CsvSeparator s : CsvSeparator.values()) {
            if (String.valueOf(s.getSeperator()).equals(seperator)) {
                return s;
            }
        }
        return null;
    }
}
