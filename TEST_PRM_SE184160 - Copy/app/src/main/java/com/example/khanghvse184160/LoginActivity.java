package com.example.khanghvse184160;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.khanghvse184160.data.session.SessionManager;
import com.example.khanghvse184160.presenter.LoginPresenter;
import com.example.khanghvse184160.view.LoginView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements LoginView {
    static final String EXTRA_REGISTRATION_SUCCESS = "extra_registration_success";
    private static final String DATABASE_URL = "https://prm-fire-base-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2001;

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton buttonLogin;
    private MaterialButton buttonGoogleLogin;
    private MaterialButton buttonSignup;
    private LoginPresenter presenter;
    private boolean isSubmitting = false;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;
    private SessionManager sessionManager;
    private TextInputEditText pendingVoiceTarget;

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
        setContentView(R.layout.activity_login);

        emailLayout = findViewById(R.id.email_input_layout);
        passwordLayout = findViewById(R.id.password_input_layout);
        emailInput = findViewById(R.id.email_login);
        passwordInput = findViewById(R.id.password_login);
        buttonLogin = findViewById(R.id.button_login_submit);
        buttonGoogleLogin = findViewById(R.id.button_login_google);
        buttonSignup = findViewById(R.id.button_new_user);

        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(getApplicationContext());
        googleSignInClient = buildGoogleSignInClient();
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        showErrorMessage("Google sign-in canceled");
                        return;
                    }
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        } else {
                            showErrorMessage("Google sign-in failed");
                            setGoogleSignInEnabled(true);
                        }
                    } catch (ApiException e) {
                        showErrorMessage("Google sign-in failed");
                        setGoogleSignInEnabled(true);
                    }
                });

        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (pendingVoiceTarget == null) {
                        return;
                    }

                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String spokenText = matches.get(0);
                            if (pendingVoiceTarget == passwordInput && spokenText != null) {
                                spokenText = spokenText.replace(" ", "");
                            }
                            if (spokenText != null) {
                                pendingVoiceTarget.setText(spokenText);
                                pendingVoiceTarget.setSelection(spokenText.length());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "No speech input detected", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Speech recognition canceled", Toast.LENGTH_SHORT).show();
                    }
                    pendingVoiceTarget = null;
                });

        presenter = new LoginPresenter(this, getApplicationContext());

        consumeRegistrationSuccessExtra();

        emailInput.addTextChangedListener(inputWatcher);
        passwordInput.addTextChangedListener(inputWatcher);
        emailLayout.setStartIconOnClickListener(v -> startVoiceRecognition(emailInput));
        passwordLayout.setStartIconOnClickListener(v -> startVoiceRecognition(passwordInput));

        validateInputs(false);

        buttonLogin.setOnClickListener(v -> {
            if (!validateInputs(true)) {
                showErrorMessage("Please fix the highlighted fields");
                return;
            }

            isSubmitting = true;
            setLoginEnabled(false);

            String email = getTrimmedText(emailInput);
            String password = getPasswordValue(passwordInput);

            presenter.performLogin(email, password);
        });

        buttonSignup.setOnClickListener(v -> presenter.onSignupClicked());
        buttonGoogleLogin.setOnClickListener(v -> {
            setGoogleSignInEnabled(false);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        consumeRegistrationSuccessExtra();
    }

    private void startVoiceRecognition(TextInputEditText target) {
        if (target == null) {
            return;
        }
        pendingVoiceTarget = target;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    thisz,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION
            );
            return;
        }
        launchSpeechRecognizer();
    }

    private void launchSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.prompt_voice_input));
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
            pendingVoiceTarget = null;
            return;
        }
        speechRecognizerLauncher.launch(intent);
    }

    private void consumeRegistrationSuccessExtra() {
        String successMessage = getIntent().getStringExtra(EXTRA_REGISTRATION_SUCCESS);
        if (!TextUtils.isEmpty(successMessage)) {
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
            getIntent().removeExtra(EXTRA_REGISTRATION_SUCCESS);
        }
    }

    private boolean validateInputs(boolean showErrors) {
        String email = getTrimmedText(emailInput);
        String password = getPasswordValue(passwordInput);

        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            valid = false;
            if (showErrors) {
                emailLayout.setError("Email is required");
            } else {
                emailLayout.setError(null);
            }
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            valid = false;
            if (showErrors) {
                emailLayout.setError("Enter a valid email");
            } else {
                emailLayout.setError(null);
            }
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            valid = false;
            if (showErrors) {
                passwordLayout.setError("Password is required");
            } else {
                passwordLayout.setError(null);
            }
        } else {
            passwordLayout.setError(null);
        }

        if (isSubmitting) {
            setLoginEnabled(false);
        } else {
            setLoginEnabled(valid);
        }

        return valid;
    }

    private boolean areFieldsValid() {
        String email = getTrimmedText(emailInput);
        String password = getPasswordValue(passwordInput);

        return !TextUtils.isEmpty(email)
                && Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && !TextUtils.isEmpty(password);
    }

    private void setLoginEnabled(boolean enabled) {
        buttonLogin.setEnabled(enabled);
        buttonLogin.setAlpha(enabled ? 1f : 0.5f);
    }

    private void setGoogleSignInEnabled(boolean enabled) {
        if (buttonGoogleLogin == null) {
            return;
        }
        buttonGoogleLogin.setEnabled(enabled);
        buttonGoogleLogin.setAlpha(enabled ? 1f : 0.5f);
    }

    private String getTrimmedText(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private String getPasswordValue(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString() : "";
    }

    @Override
    public void showSuccessMessage(String message) {
        isSubmitting = false;
        setLoginEnabled(true);
        setGoogleSignInEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginError(String error) {
        isSubmitting = false;
        setLoginEnabled(areFieldsValid());
        setGoogleSignInEnabled(true);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToSignup() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    @Override
    public void navigateToHome() {
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }

    @Override
    public void onLocalLoginSuccess() {
        firebaseAuth.signOut();
    }

    @Override
    public void showErrorMessage(String message) {
        isSubmitting = false;
        setLoginEnabled(areFieldsValid());
        setGoogleSignInEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchSpeechRecognizer();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
                pendingVoiceTarget = null;
            }
        }
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(this, options);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (TextUtils.isEmpty(idToken)) {
            showErrorMessage("Missing Google ID token");
            return;
        }

        isSubmitting = true;
        setLoginEnabled(false);
        setGoogleSignInEnabled(false);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    isSubmitting = false;
                    setLoginEnabled(areFieldsValid());
                    setGoogleSignInEnabled(true);

                    if (task.isSuccessful()) {
                        sessionManager.clearLocalUser();
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        ensureUserProfile(user);
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Google authentication failed";
                        showErrorMessage(message);
                    }
                });
    }

    private void ensureUserProfile(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            showErrorMessage("Unable to retrieve user information");
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users");
        usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    showSuccessMessage("Signed in with Google");
                    navigateToHome();
                    return;
                }

                Map<String, Object> payload = new HashMap<>();
                payload.put("uid", firebaseUser.getUid());
                payload.put("username", !TextUtils.isEmpty(firebaseUser.getDisplayName())
                        ? firebaseUser.getDisplayName()
                        : firebaseUser.getEmail());
                payload.put("email", firebaseUser.getEmail());
                payload.put("createdAt", ServerValue.TIMESTAMP);

                usersRef.child(firebaseUser.getUid()).setValue(payload)
                        .addOnCompleteListener(saveTask -> {
                            if (saveTask.isSuccessful()) {
                                showSuccessMessage("Signed in with Google");
                                navigateToHome();
                            } else {
                                String msg = saveTask.getException() != null
                                        ? saveTask.getException().getMessage()
                                        : "Failed to store profile";
                                showErrorMessage(msg);
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showErrorMessage(error.getMessage());
            }
        });
    }
}

