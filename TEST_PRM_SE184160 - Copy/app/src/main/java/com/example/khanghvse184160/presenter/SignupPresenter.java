package com.example.khanghvse184160.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.khanghvse184160.model.AuthModel;
import com.example.khanghvse184160.view.SignupView;

public class SignupPresenter {
    private final SignupView view;
    private final AuthModel model;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public SignupPresenter(SignupView view, Context context) {
        this.view = view;
        this.model = new AuthModel(context.getApplicationContext());
    }

    public void performSignup(String username,
                              String email,
                              String password,
                              String confirmPassword) {
        if (isEmpty(username) || isEmpty(email) || isEmpty(password) || isEmpty(confirmPassword)) {
            view.showSignupError("All fields must be filled");
            return;
        }
        if (!password.equals(confirmPassword)) {
            view.showSignupError("Passwords do not match");
            return;
        }

        model.signup(email, password, username, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                mainHandler.post(() -> view.showSignupSuccess(message));
            }

            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> view.showSignupError(error));
            }
        });
    }

    public void onLoginClicked() {
        view.navigateToLogin();
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
