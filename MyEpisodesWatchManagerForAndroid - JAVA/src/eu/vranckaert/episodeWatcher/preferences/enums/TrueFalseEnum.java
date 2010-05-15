package eu.vranckaert.episodeWatcher.preferences.enums;

/**
 * @author Dirk Vranckaert
 *         Date: 15-mei-2010
 *         Time: 18:33:39
 */
public enum TrueFalseEnum {
    TRUE("true"),
    FALSE("false");

    String value;

    TrueFalseEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
