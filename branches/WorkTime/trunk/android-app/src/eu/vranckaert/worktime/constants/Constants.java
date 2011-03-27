package eu.vranckaert.worktime.constants;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 15:22
 */
public class Constants {
    public class Dialog {
        public static final int DELETE_PROJECT_YES_NO = 0;
        public static final int DELETE_TIME_REGISTRATIONS_OF_PROJECT_YES_NO = 1;
        public static final int CHOOSE_EXPORT_FILE_TYPE = 3;
        public static final int CHOOSE_EXPORT_CSV_SEPARATOR = 4;
        public static final int LOADING_TIMEREGISTRATIONS_EXPORT = 5;
        public static final int DELETE_TIME_REGISTRATION_YES_NO = 6;
        public static final int EXPORT_UNAVAILABLE = 7;
        public static final int CHOOSE_SELECTED_PROJECT = 8;
        public static final int LOADING_TIMEREGISTRATION_CHANGE = 9;
    }
    public class IntentRequestCodes {
        public static final int ADD_PROJECT = 0;
    }
    public class Preferences {
        public static final String PREFERENCES_NAME = "WorkTime_0001";

        public static final String EXPORT_TIME_REG_FILE_NAME_DEFAULT_VALUE = "export";
        public static final int SELECTED_PROJECT_ID_DEFAULT_VALUE = -1;

        public class Keys {
            public static final String EXPORT_TIME_REG_FILE_NAME = "exportFileName";
            public static final String EXPROT_TIME_REG_FILE_TYPE = "exportFileType";
            public static final String EXPROT_TIME_REG_CSV_SEPARATOR = "exportCsvSeperator";
            public static final String SELECTED_PROJECT_ID = "selectedProjectId";
        }
    }
    public class Export {
        public static final String EXPORT_DIRECTORY = "worktime";
    }
}
