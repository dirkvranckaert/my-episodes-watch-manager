package eu.vranckaert.worktime.activities.projects;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.model.Project;
import eu.vranckaert.worktime.service.ProjectService;
import eu.vranckaert.worktime.utils.IntentUtil;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import roboguice.activity.GuiceActivity;
import roboguice.inject.InjectView;

/**
 * User: DIRK VRANCKAERT
 * Date: 06/02/11
 * Time: 03:51
 */
public class AddProjectActivity extends GuiceActivity {
    private static final String LOG_TAG = AddProjectActivity.class.getSimpleName();

    @InjectView(R.id.projectname) private EditText projectNameInput;
    @InjectView(R.id.projectcomment) private EditText projectCommentInput;
    @InjectView(R.id.makeDefaultProject) private CheckBox projectMakeDefault;
    @InjectView(R.id.projectname_required) private TextView projectnameRequired;
    @InjectView(R.id.projectname_unique) private TextView projectnameUnique;

    @Inject
    private ProjectService projectService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
    }

    /**
     * Navigate home.
     * @param view The view.
     */
    public void onHomeClick(View view) {
        IntentUtil.goHome(this);
    }

    /**
     * Save the project.
     * @param view The view.
     */
    public void onSaveClick(View view) {
        hideValidationErrors();

        String name = projectNameInput.getText().toString();
        String comment = projectCommentInput.getText().toString();
        if (name.length() > 0) {
            if (projectService.isNameAlreadyUsed(name)) {
                Log.d(LOG_TAG, "A project with this name already exists... Choose another name!");
                projectnameUnique.setVisibility(View.VISIBLE);
            } else {
                ContextUtils.hideKeyboard(AddProjectActivity.this, projectNameInput);
                Log.d(LOG_TAG, "Ready to save new project");

                ImageView saveButton = (ImageView) findViewById(R.id.btn_save);
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.title_refresh_progress);
                saveButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                Project project = new Project();
                project.setName(name);
                project.setComment(comment);
                project = projectService.save(project);
                if (projectMakeDefault.isChecked()) {
                    projectService.changeDefaultProject(project);
                }
                Log.d(LOG_TAG, "New project persisted");
                setResult(RESULT_OK);
                finish();
            }
        } else {
            Log.d(LOG_TAG, "Validation error!");
            projectnameRequired.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide all the validation errors.
     */
    private void hideValidationErrors() {
        projectnameRequired.setVisibility(View.GONE);
        projectnameUnique.setVisibility(View.GONE);
    }
}
