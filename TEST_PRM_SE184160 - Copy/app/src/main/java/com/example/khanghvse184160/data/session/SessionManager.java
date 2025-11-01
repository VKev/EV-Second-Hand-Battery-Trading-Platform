package com.example.khanghvse184160.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public class SessionManager {

    private static final String PREFS_NAME = "auth_session_prefs";
    private static final String KEY_IS_LOCAL = "is_local_user";
    private static final String KEY_USER_ID = "local_user_id";
    private static final String KEY_USER_EMAIL = "local_user_email";
    private static final String KEY_USER_NAME = "local_user_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLocalUser(long id, String email, String username) {
        prefs.edit()
                .putBoolean(KEY_IS_LOCAL, true)
                .putLong(KEY_USER_ID, id)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, username)
                .apply();
    }

    @Nullable
    public SessionUser getActiveLocalUser() {
        if (!prefs.getBoolean(KEY_IS_LOCAL, false)) {
            return null;
        }

        long id = prefs.getLong(KEY_USER_ID, -1);
        String email = prefs.getString(KEY_USER_EMAIL, null);
        String username = prefs.getString(KEY_USER_NAME, null);

        if (id <= 0 || email == null || username == null) {
            return null;
        }

        return new SessionUser(id, email, username);
    }

    public void clearLocalUser() {
        prefs.edit()
                .remove(KEY_IS_LOCAL)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_NAME)
                .apply();
    }
}
