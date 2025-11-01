package com.example.khanghvse184160.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.khanghvse184160.data.local.AppDatabase;
import com.example.khanghvse184160.data.local.LocalUser;
import com.example.khanghvse184160.data.local.LocalUserDao;
import com.example.khanghvse184160.data.session.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthModel {

    private final LocalUserDao userDao;
    private final SessionManager sessionManager;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AuthModel(@NonNull Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userDao = database.localUserDao();
        sessionManager = new SessionManager(context);
    }

    public void login(@NonNull String email,
                      @NonNull String password,
                      @NonNull AuthCallback callback) {
        final String normalizedEmail = normalizeEmail(email);
        ioExecutor.execute(() -> {
            LocalUser user = userDao.findByEmail(normalizedEmail);
            if (user == null) {
                postFailure(callback, "Account not found");
                return;
            }

            if (!user.getPassword().equals(password)) {
                postFailure(callback, "Incorrect password");
                return;
            }

            sessionManager.saveLocalUser(user.getId(), user.getEmail(), user.getUsername());
            postSuccess(callback, "Login successful");
        });
    }

    public void signup(@NonNull String email,
                       @NonNull String password,
                       @NonNull String username,
                       @NonNull AuthCallback callback) {
        final String normalizedEmail = normalizeEmail(email);
        ioExecutor.execute(() -> {
            LocalUser existing = userDao.findByEmail(normalizedEmail);
            if (existing != null) {
                postFailure(callback, "Email is already registered");
                return;
            }

            LocalUser newUser = new LocalUser(
                    username.trim(),
                    normalizedEmail,
                    password,
                    System.currentTimeMillis()
            );

            try {
                long id = userDao.insert(newUser);
                newUser.setId(id);
                postSuccess(callback, "Registration successful");
            } catch (Exception e) {
                postFailure(callback, "Unable to register right now");
            }
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private void postSuccess(AuthCallback cb, String msg) {
        mainHandler.post(() -> cb.onSuccess(msg));
    }

    private void postFailure(AuthCallback cb, String msg) {
        mainHandler.post(() -> cb.onFailure(msg));
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String message);
    }
}
