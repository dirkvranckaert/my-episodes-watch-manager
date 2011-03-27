package eu.vranckaert.worktime.service.impl;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.comparators.TimeRegistrationDescendingByStartdate;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.constants.TextConstants;
import eu.vranckaert.worktime.dao.ProjectDao;
import eu.vranckaert.worktime.dao.TimeRegistrationDao;
import eu.vranckaert.worktime.enums.export.CsvSeparator;
import eu.vranckaert.worktime.enums.export.ExportType;
import eu.vranckaert.worktime.enums.export.FileType;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.TimeRegistrationService;
import eu.vranckaert.worktime.utils.date.DateTimeFormats;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 07/02/11
 * Time: 00:14
 */
public class TimeRegistrationServiceImpl implements TimeRegistrationService {
    private static final String LOG_TAG = TimeRegistrationServiceImpl.class.getSimpleName();

    @Inject
    TimeRegistrationDao dao;

    @Inject
    ProjectDao projectDao;

    /**
     * {@inheritDoc}
     */
    public List<TimeRegistration> findAll() {
        List<TimeRegistration> timeRegistrations = dao.findAll();
        for(TimeRegistration timeRegistration : timeRegistrations) {
            Log.d(LOG_TAG, "Found timeregistration with ID: " + timeRegistration.getId() + " and according project with ID: " + timeRegistration.getProject().getId());
            projectDao.refresh(timeRegistration.getProject());
        }
        return timeRegistrations;
    }

    /**
     * {@inheritDoc}
     */
    public void create(TimeRegistration timeRegistration) {
        dao.save(timeRegistration);
    }

    public void update(TimeRegistration timeRegistration) {
        dao.update(timeRegistration);
    }

    /**
     * {@inheritDoc}
     */
    public TimeRegistration getLatestTimeRegistration() {
        return dao.getLatestTimeRegistration();
    }

    /**
     * {@inheritDoc}
     */
    public File export(final ExportType exportType, final Context ctx) {
        String fileName = Preferences.getTimeRegistrationExportFileName(ctx);
        FileType fileType = Preferences.getTimeRegistrationExportFileType(ctx);
        CsvSeparator csvSeperator = Preferences.getTrimeRegistrationCsvSeparator(ctx);

        List<TimeRegistration> timeRegistrations = findAll();
        Collections.sort(timeRegistrations, new TimeRegistrationDescendingByStartdate());

        StringBuilder export = new StringBuilder();

        boolean isFirstLine = true;
        for (TimeRegistration timeRegistration : timeRegistrations) {
            String startDate = DateUtils.convertDateToString(timeRegistration.getStartTime(), DateTimeFormats.SHORT, ctx);
            String startTime = DateUtils.convertTimeToString(timeRegistration.getStartTime(), DateTimeFormats.SHORT, ctx);
            String endDate = null;
            String endTime = null;
            if(timeRegistration.getEndTime() != null) {
                endDate = DateUtils.convertDateToString(timeRegistration.getEndTime(), DateTimeFormats.SHORT, ctx);
                endTime = DateUtils.convertTimeToString(timeRegistration.getEndTime(), DateTimeFormats.SHORT, ctx);
            } else {
                endDate = ctx.getString(R.string.now);
                endTime = "";
            }

            if (!isFirstLine) {
                export.append(TextConstants.NEW_LINE);
            }

            switch (fileType) {
                case TEXT: {
                    /*START*/
                    export.append(ctx.getText(R.string.lbl_registrations_export_file_txt_start));
                    export.append(TextConstants.NEW_LINE);
                    /*START DATE*/
                    export.append(startDate);
                    export.append(TextConstants.NEW_LINE);
                    /*START TIME*/
                    export.append(startTime);
                    export.append(TextConstants.NEW_LINE);
                    /*END*/
                    export.append(ctx.getText(R.string.lbl_registrations_export_file_txt_end));
                    export.append(TextConstants.NEW_LINE);
                    /*END DATE*/
                    export.append(endDate);
                    export.append(TextConstants.NEW_LINE);
                    /*END TIME*/
                    export.append(endTime);
                    export.append(TextConstants.NEW_LINE);
                    /*PROJECT*/
                    export.append(ctx.getText(R.string.lbl_registrations_export_file_txt_project));
                    export.append(TextConstants.NEW_LINE);
                    /*PROJECT NAME*/
                    export.append(timeRegistration.getProject().getName());
                    export.append(TextConstants.NEW_LINE);
                    /*PROJECT COMMENT*/
                    if(StringUtils.isNotBlank(timeRegistration.getProject().getComment())) {
                        export.append(TextConstants.SPACE +
                                "(" + export.append(timeRegistration.getProject().getName()) + ")");
                    }
                    break;
                }
                case COMMA_SERPERATED_VALUES: {
                    char separator = csvSeperator.getSeperator();

                    if (isFirstLine) {
                        /*Add the column headers*/
                        export.append(ctx.getString(R.string.lbl_registrations_export_file_csv_startdate));
                        export.append(separator);
                        export.append(ctx.getString(R.string.lbl_registrations_export_file_csv_starttime));
                        export.append(separator);
                        export.append(ctx.getString(R.string.lbl_registrations_export_file_csv_enddate));
                        export.append(separator);
                        export.append(ctx.getString(R.string.lbl_registrations_export_file_csv_endtime));
                        export.append(separator);
                        export.append(ctx.getString(R.string.lbl_registrations_export_file_csv_project));
                        export.append(separator);
                        export.append(ctx.getString(R.string.lbl_registrations_export_file_csv_projectcomment));
                        export.append(separator);
                        export.append(TextConstants.NEW_LINE);
                    }

                    export.append(startDate);
                    export.append(separator);
                    export.append(startTime);
                    export.append(separator);
                    export.append(endDate);
                    export.append(separator);
                    export.append(endTime);
                    export.append(separator);
                    export.append(timeRegistration.getProject().getName());
                    export.append(separator);
                    export.append(timeRegistration.getProject().getComment());

                    break;
                }
            }

            if (isFirstLine) {
                isFirstLine = false;
            }
        }

        File defaultStorageDirectory = Environment.getExternalStorageDirectory();
        File file = new File(defaultStorageDirectory.getAbsolutePath() +
                File.separator +
                Constants.Export.EXPORT_DIRECTORY +
                File.separator +
                fileName +
                "." +
                fileType.getExtension().toLowerCase()
        );

        try {
            boolean fileAlreadyExists = file.createNewFile();
            if(fileAlreadyExists) {
                file.delete();
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(export.toString());
            bw.close();
            fw.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception occured during export...", e);
            //TODO handle exception
        }

        return file;
    }

    public void remove(TimeRegistration timeRegistration) {
        dao.delete(timeRegistration);
    }
}
