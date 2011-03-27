package eu.vranckaert.worktime.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.activities.projects.AddProjectActivity;
import eu.vranckaert.worktime.comparators.ProjectByNameComparator;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.exceptions.AtLeastOneProjectRequiredException;
import eu.vranckaert.worktime.exceptions.ProjectStillInUseException;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.service.WidgetService;
import eu.vranckaert.worktime.utils.IntentUtil;
import eu.vranckaert.worktime.utils.string.StringUtils;
import roboguice.activity.GuiceListActivity;

import java.util.Collections;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 05/02/11
 * Time: 18:19
 */
public class ManageProjectsActivity extends GuiceListActivity {
    private static final String LOG_TAG = ManageProjectsActivity.class.getSimpleName();

    private List<Project> projects;

    private Project projectToRemove = null;

    @Inject
    private ProjectService projectService;

    @Inject
    private WidgetService widgetService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_projects);

        loadProjects();
    }

    /**
     * Load the all the projects to display and attach the listAdapater.
     */
    private void loadProjects() {
        this.projects = projectService.findAll();
        Collections.sort(this.projects, new ProjectByNameComparator());
        Log.d(LOG_TAG, projects.size() + " projects loaded!");
        ManageProjectsListAdapter adapter = new ManageProjectsListAdapter(projects);
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }

    /**
     * Delete a project.
     * @param project The project to delete.
     * @param askConfirmation If a confirmation should be requested to the user. If so the delete will no be executed
     * but a show dialog is called form where you have to call this method again with the askConfirmation parameter set
     * @param force If set to {@link Boolean#TRUE} all {@link eu.vranckaert.worktime.model.TimeRegistration} instances
     * linked to the project will be deleted first, then the project. If set to {@link Boolean#FALSE} nothing will
     * happen.
     */
    private void deleteProject(Project project, boolean askConfirmation, boolean force) {
        if (askConfirmation) {
            Log.d(LOG_TAG, "Asking confirmation to remove a project");
            projectToRemove = project;
            showDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
        } else {
            Log.d(LOG_TAG, "Removing a project... Are we forcing the removal? " + force);
            projectToRemove = null;
            try {
                Log.d(LOG_TAG, "Ready to actually remove the project!");
                projectService.remove(project, force);
                Log.d(LOG_TAG, "Project removed, ready to reload projects");
                loadProjects();
                widgetService.updateWidget(ManageProjectsActivity.this);
            } catch (ProjectStillInUseException e) {
                if (force) {
                    Log.d(LOG_TAG, "Something is wrong. Forcing the time registrations to be deleted should not result"
                        + "in this exception!");
                } else {
                    projectToRemove = project;
                    showDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_PROJECT_YES_NO);
                }
            } catch (AtLeastOneProjectRequiredException e) {
                Toast.makeText(ManageProjectsActivity.this, R.string.msg_delete_project_at_least_one_required,  Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch(dialogId) {
            case Constants.Dialog.DELETE_PROJECT_YES_NO: {
                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
				alertRemoveAllRegs.setTitle(projectToRemove.getName())
						   .setMessage(R.string.msg_delete_project_confirmation)
						   .setCancelable(false)
						   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteProject(projectToRemove, false, false);
                                    removeDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
								}
							})
						   .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    projectToRemove = null;
									removeDialog(Constants.Dialog.DELETE_PROJECT_YES_NO);
								}
							});
				dialog = alertRemoveAllRegs.create();
                break;
            }
            case Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_PROJECT_YES_NO: {
                AlertDialog.Builder alertRemoveAllRegs = new AlertDialog.Builder(this);
				alertRemoveAllRegs.setTitle(projectToRemove.getName())
						   .setMessage(R.string.msg_delete_project_and_linked_time_registrations_confirmation)
						   .setCancelable(false)
						   .setPositiveButton(R.string.btn_projects_delete_all, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									deleteProject(projectToRemove, false, true);
                                    removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_PROJECT_YES_NO);
								}
							})
						   .setNegativeButton(R.string.btn_projects_do_nothing, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
                                    projectToRemove = null;
									removeDialog(Constants.Dialog.DELETE_TIME_REGISTRATIONS_OF_PROJECT_YES_NO);
								}
							});
				dialog = alertRemoveAllRegs.create();
                break;
            }
        };
        return dialog;
    }

    /**
     * Navigate home.
     * @param view The view.
     */
    public void onHomeClick(View view) {
        IntentUtil.goHome(this);
    }

    /**
     * Navigate to the add projects activity.
     * @param view The view.
     */
    public void onAddClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AddProjectActivity.class);
        startActivityForResult(intent, Constants.IntentRequestCodes.ADD_PROJECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.IntentRequestCodes.ADD_PROJECT && resultCode == RESULT_OK) {
            Log.d(LOG_TAG, "A new project is added which requires a reload of the project list!");
            loadProjects();
        }
    }

    /**
     * The list adapater private inner-class used to display the manage projects list.
     */
    private class ManageProjectsListAdapter extends ArrayAdapter<Project> {
        private final String LOG_TAG = ManageProjectsListAdapter.class.getSimpleName();
        /**
         * {@inheritDoc}
         */
        public ManageProjectsListAdapter(List<Project> projects) {
            super(ManageProjectsActivity.this, R.layout.list_item_projects, projects);
            Log.d(LOG_TAG, "Creating the manage projects list adapater");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(LOG_TAG, "Start rendering/recycling row " + position);
            View row;
            final Project project = projects.get(position);
            Log.d(LOG_TAG, "Got project with name " + project.getName());
            Log.d(LOG_TAG, "Is this project the default project?" + project.isDefaultValue());

            if (convertView == null) {
                Log.d(LOG_TAG, "Render a new line in the list");
                row = getLayoutInflater().inflate(R.layout.list_item_projects, parent, false);
            } else {
                Log.d(LOG_TAG, "Recycling an existing line in the list");
                row = convertView;
            }

            Log.d(LOG_TAG, "Ready to update the name of the project of the listitem...");
            TextView projectname = (TextView) row.findViewById(R.id.projectname_listitem);
            projectname.setText(project.getName());

            Log.d(LOG_TAG, "Ready to update the default value of the project of the listitem...");
            TextView projectDefault = (TextView) row.findViewById(R.id.project_default);
            if (project.isDefaultValue()) {
                projectDefault.setVisibility(View.VISIBLE);
                projectDefault.setText(StringUtils.SPACE + projectDefault.getText());
            } else {
                projectDefault.setVisibility(View.INVISIBLE);
            }

            Log.d(LOG_TAG, "Ready to bind the deleteButton to the deleteProject method...");
            ImageView deleteButton = (ImageView) row.findViewById(R.id.btn_delete);
            if (projects.size() > 1) {
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        deleteProject(project, true, false);
                    }
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }

            Log.d(LOG_TAG, "Done rendering row " + position);
            return row;
        }
    }
}
