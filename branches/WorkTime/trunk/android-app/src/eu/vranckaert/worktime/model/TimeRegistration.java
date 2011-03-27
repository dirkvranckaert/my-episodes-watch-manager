package eu.vranckaert.worktime.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 16:50
 */
@DatabaseTable
public class TimeRegistration {
    @DatabaseField(generatedId = true, columnName = "id")
    private Integer id;
    @DatabaseField(columnName = "startTime")
    private Date startTime;
    @DatabaseField(columnName = "endTime")
    private Date endTime;
    @DatabaseField(foreign = true, columnName = "projectId")
    private Project project;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isOngoingTimeRegistration() {
        if (endTime == null) {
            return true;
        }
        return false;
    }
}
