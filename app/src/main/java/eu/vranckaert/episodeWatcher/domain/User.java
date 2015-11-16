package eu.vranckaert.episodeWatcher.domain;

import android.content.Context;
import android.text.TextUtils;
import eu.vranckaert.episodeWatcher.preferences.Preferences;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1161467681474966905L;
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";

    private String username;
    private String password;

    public User(String username, String password) {
        setUsername(username);
        setPassword(password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static User get(Context context) {
        User user = null;
        String username = Preferences.getPreference(context, User.USERNAME);
        String password = Preferences.getPreference(context, User.PASSWORD);
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            user = new User(username, password);
        }
        return user;
    }
}
