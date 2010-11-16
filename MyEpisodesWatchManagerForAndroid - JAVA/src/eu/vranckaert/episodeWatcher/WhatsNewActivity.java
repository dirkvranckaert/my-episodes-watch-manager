package eu.vranckaert.episodeWatcher;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.utils.ApplicationUtil;

public class WhatsNewActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        init(savedInstanceState);

        LinearLayout text = (LinearLayout) findViewById(R.id.whatsNewTextEntries);

        Resources res = getResources();
        String[] textValues = res.getStringArray(R.array.whatsNewValues);
        String textValuesPrefix = getString(R.string.whatsNewListPrefix);

        for(String textValue : textValues) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(textValuesPrefix + textValue);
            text.addView(textView);
        }

        TextView version = (TextView) findViewById(R.id.whatsNewVersion);
        version.setText(ApplicationUtil.getCurrentApplicationVersion(getApplicationContext()));

        Button closeButton = (Button) findViewById(R.id.whatsnewclose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void init(Bundle savedInstanceState) {
        setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whatnewdialog);
    }
}
