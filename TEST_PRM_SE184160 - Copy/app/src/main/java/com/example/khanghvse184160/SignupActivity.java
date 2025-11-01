package com.example.khanghvse184160;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khanghvse184160.presenter.SignupPresenter;
import com.example.khanghvse184160.view.SignupView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity implements SignupView {
    private TextInputLayout usernameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText usernameSignup;
    private TextInputEditText emailSignup;
    private TextInputEditText passwordSignup;
    private TextInputEditText confirmPasswordSignup;
    private MaterialButton buttonSignupSubmit;
    private boolean isSubmitting = false;
    private SignupPresenter presenter;

    private final TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            validateInputs(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameLayout = findViewById(R.id.username_input_layout);
        emailLayout = findViewById(R.id.email_input_layout);
        passwordLayout = findViewById(R.id.password_input_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_input_layout);
        usernameSignup = findViewById(R.id.username_signup);
        emailSignup = findViewById(R.id.email_signup);
        passwordSignup = findViewById(R.id.password_signup);
        confirmPasswordSignup = findViewById(R.id.confirm_password_signup);
        buttonSignupSubmit = findViewById(R.id.button_signup_submit);
        MaterialButton buttonRelogin = findViewById(R.id.button_relogin);

        presenter = new SignupPresenter(this, getApplicationContext());

        usernameSignup.addTextChangedListener(inputWatcher);
        emailSignup.addTextChangedListener(inputWatcher);
        passwordSignup.addTextChangedListener(inputWatcher);
        confirmPasswordSignup.addTextChangedListener(inputWatcher);

        validateInputs(false);

        buttonSignupSubmit.setOnClickListener(v -> {
            if (!validateInputs(true)) {
                showSignupError("Please fix the highlighted fields");
                return;
            }

            isSubmitting = true;
            setSubmitEnabled(false);

            String usernameValue = getTrimmedText(usernameSignup);
            String emailValue = getTrimmedText(emailSignup);
            String passwordValue = getRawText(passwordSignup);
            String confirmPasswordValue = getRawText(confirmPasswordSignup);

            presenter.performSignup(usernameValue, emailValue, passwordValue, confirmPasswordValue);
        });

        buttonRelogin.setOnClickListener(v -> presenter.onLoginClicked());
    }

    private boolean validateInputs(boolean showErrors) {
        String usernameValue = getTrimmedText(usernameSignup);
        String emailValue = getTrimmedText(emailSignup);
        String passwordValue = getRawText(passwordSignup);
        String confirmPasswordValue = getRawText(confirmPasswordSignup);

        boolean valid = true;

        if (TextUtils.isEmpty(usernameValue)) {
            valid = false;
            if (showErrors) {
                usernameLayout.setError("Username is required");
            }
        } else {
            usernameLayout.setError(null);
        }

        if (TextUtils.isEmpty(emailValue)) {
            valid = false;
            if (showErrors) {
                emailLayout.setError("Email is required");
            }
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            valid = false;
            emailLayout.setError("Enter a valid email");
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(passwordValue)) {
            valid = false;
            if (showErrors) {
                passwordLayout.setError("Password is required");
            }
        } else {
            passwordLayout.setError(null);
        }

        if (TextUtils.isEmpty(confirmPasswordValue)) {
            valid = false;
            if (showErrors) {
                confirmPasswordLayout.setError("Confirm password is required");
            } else {
                confirmPasswordLayout.setError(null);
            }
        } else if (!passwordValue.equals(confirmPasswordValue)) {
            valid = false;
            confirmPasswordLayout.setError("Passwords do not match");
        } else {
            confirmPasswordLayout.setError(null);
        }

        if (isSubmitting) {
            setSubmitEnabled(false);
        } else {
            setSubmitEnabled(valid);
        }

        return valid;
    }

    private boolean areFieldsValid() {
        String usernameValue = getTrimmedText(usernameSignup);
        String emailValue = getTrimmedText(emailSignup);
        String passwordValue = getRawText(passwordSignup);
        String confirmPasswordValue = getRawText(confirmPasswordSignup);

        if (TextUtils.isEmpty(usernameValue)) {
            return false;
        }

        if (TextUtils.isEmpty(emailValue) || !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            return false;
        }

        if (TextUtils.isEmpty(passwordValue)) {
            return false;
        }

        if (TextUtils.isEmpty(confirmPasswordValue)) {
            return false;
        }

        return passwordValue.equals(confirmPasswordValue);
    }

    private void setSubmitEnabled(boolean enabled) {
        buttonSignupSubmit.setEnabled(enabled);
        buttonSignupSubmit.setAlpha(enabled ? 1f : 0.5f);
    }

    private String getTrimmedText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private String getRawText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString() : "";
    }

    @Override
    public void showSignupSuccess(String message) {
        runOnUiThread(() -> {
            isSubmitting = false;
            setSubmitEnabled(true);

            String successMessage = TextUtils.isEmpty(message) ? "Registration successful" : message;
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();

            navigateToLoginWithMessage(successMessage);
        });
    }

    private void navigateToLoginWithMessage(String message) {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        if (!TextUtils.isEmpty(message)) {
            intent.putExtra(LoginActivity.EXTRA_REGISTRATION_SUCCESS, message);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void showSignupError(String error) {
        isSubmitting = false;
        setSubmitEnabled(areFieldsValid());
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        // Use simple navigation like LoginActivity does
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}






