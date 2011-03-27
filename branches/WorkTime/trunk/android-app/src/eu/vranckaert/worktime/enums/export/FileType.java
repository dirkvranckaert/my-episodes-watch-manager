package eu.vranckaert.worktime.enums.export;

/**
 * All possible file types with their extensions.
 * User: DIRK VRANCKAERT
 * Date: 19/02/11
 * Time: 14:42
 */
public enum FileType {
    TEXT("TXT"),
    COMMA_SERPERATED_VALUES("CSV");

    private String extension;

    /**
     * Constructor.
     * @param extension The extension.
     */
    private FileType(String extension) {
        this.extension = extension;
    }

    /*
     * Getters and setters
     */

    public String getExtension() {
        return this.extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Match an extension on one of the possible export formats!
     * @param extension The extensions to match.
     * @return The type of file to export to.
     */
    public static FileType matchFileType(String extension) {
        for (FileType ft : FileType.values()) {
            if (ft.getExtension().equals(extension)) {
                return ft;
            }
        }
        return null;
    }
}
