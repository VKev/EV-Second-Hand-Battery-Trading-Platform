// com.example.khanghvse184160.view.LoginView
package com.example.khanghvse184160.view;

public interface LoginView {
    void showSuccessMessage(String message);
    void showLoginError(String error);
    void navigateToSignup();
    void navigateToHome();
    void showErrorMessage(String message);
    void onLocalLoginSuccess();
}
