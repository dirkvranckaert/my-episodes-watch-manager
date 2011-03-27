package eu.vranckaert.worktime.service.impl;

import android.content.Context;
import android.util.Log;
import com.google.inject.Inject;
import eu.vranckaert.worktime.dao.ProjectDao;
import eu.vranckaert.worktime.dao.TimeRegistrationDao;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.service.DevelopmentService;
import eu.vranckaert.worktime.service.WidgetService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:54
 */
public class DevelopmentServiceImpl implements DevelopmentService {
    private static final String LOG_TAG = DevelopmentServiceImpl.class.getSimpleName();

    @Inject
    ProjectDao projectDao;

    @Inject
    TimeRegistrationDao timeRegistrationDao;

    @Inject
    WidgetService widgetService;

    List<Project> projects = new ArrayList<Project>();

    public void createSampleData(Context ctx) {
        clearDatabaseData(ctx);
        createProjects();
        createTimeRegistrations();
    }

    private void createProjects() {
        for(int i=0; i<10; i++) {
            Project project = new Project();
            project.setName("projectPost" + i);
            project.setComment("Project number " + i);
            project = projectDao.save(project);
            projects.add(project);
        }
    }

    private void createTimeRegistrations() {
        int counter = 0;
        for (Project project : projects) {
            TimeRegistration tr = new TimeRegistration();

            Calendar startTime = Calendar.getInstance();
            startTime.setTime(new Date());
            startTime.set(Calendar.MONTH, counter);

            Calendar endTime = Calendar.getInstance();
            endTime.setTime(startTime.getTime());
            endTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY) + counter+1);

            tr.setStartTime(startTime.getTime());
            tr.setEndTime(endTime.getTime());

            tr.setProject(project);
            timeRegistrationDao.save(tr);
            counter++;
        }
        Log.d(LOG_TAG, counter + " timeregistrations created");
    }

    public void clearDatabaseData(Context ctx) {
        List<Project> projects = projectDao.findAll();
        Log.d(LOG_TAG, projects.size() + " projects found!");
        for(Project project : projects) {
            projectDao.delete(project);
        }

        List<TimeRegistration> timeRegs = timeRegistrationDao.findAll();
        Log.d(LOG_TAG, timeRegs.size() + " registrations found!");
        for(TimeRegistration timeReg : timeRegs) {
            timeRegistrationDao.delete(timeReg);
        }
        widgetService.updateWidget(ctx);
    }

    public void bootCheck() {
        List<Project> projects = projectDao.findAll();
        List<TimeRegistration> regs = timeRegistrationDao.findAll();

        Log.d(LOG_TAG, "Number of projects found at startup: " + projects.size());
        Log.d(LOG_TAG, "Number of time registrations found at startup: " + regs.size());
    }
}
