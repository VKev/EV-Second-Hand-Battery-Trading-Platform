package com.example.khanghvse184160.presenter;

import android.content.Context;

import com.example.khanghvse184160.model.AuthModel;
import com.example.khanghvse184160.view.LoginView;

public class LoginPresenter {
    private final LoginView view;
    private final AuthModel model;

    public LoginPresenter(LoginView view, Context context) {
        this.view = view;
        this.model = new AuthModel(context.getApplicationContext());
    }

    public void performLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showErrorMessage("Email and password cannot be empty");
            return;
        }

        model.login(email, password, new AuthModel.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                view.showSuccessMessage(message);
                view.onLocalLoginSuccess();
                view.navigateToHome();
            }

            @Override
            public void onFailure(String error) {
                view.showLoginError(error);
            }
        });
    }

    public void onSignupClicked() {
        view.navigateToSignup();
    }
}
