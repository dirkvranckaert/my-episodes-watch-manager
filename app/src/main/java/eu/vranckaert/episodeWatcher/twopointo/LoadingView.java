package eu.vranckaert.episodeWatcher.twopointo;

import android.content.Context;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.twopointo.view.AbstractViewHolder;

/**
 * Date: 14/04/14
 * Time: 15:28
 *
 * @author Dirk Vranckaert
 */
public class LoadingView extends AbstractViewHolder {
    private final TextView message;

    public LoadingView(Context context) {
        super(context, R.layout.loading);
        message = findViewById(R.id.message);
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }
}
