package com.example.khanghvse184160;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.khanghvse184160.data.session.SessionManager;
import com.example.khanghvse184160.data.session.SessionUser;
import com.example.khanghvse184160.model.User;
import com.example.khanghvse184160.view.WelcomeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WelcomeActivity extends AppCompatActivity implements WelcomeView {
    private static final String DATABASE_URL = "https://prm-fire-base-default-rtdb.asia-southeast1.firebasedatabase.app";

    private TextView welcomeText;
    private TextView statusText;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        welcomeText = findViewById(R.id.text_welcome_message);
        statusText = findViewById(R.id.text_status_message);
        progressBar = findViewById(R.id.progress_loading);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance(DATABASE_URL).getReference("users");
        sessionManager = new SessionManager(getApplicationContext());

        showLoadingState(true);
        SessionUser localUser = sessionManager.getActiveLocalUser();
        if (localUser != null) {
            showLoadingState(false);
            String nameToDisplay = !TextUtils.isEmpty(localUser.getUsername())
                    ? localUser.getUsername()
                    : localUser.getEmail();
            displayUserInfo(nameToDisplay);
            return;
        }

        fetchProfile();
    }

    private void fetchProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showLoadingState(false);
            showFallbackMessage("Welcome!");
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
            return;
        }

        final String fallbackName = currentUser.getEmail();

        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                showLoadingState(false);
                User user = snapshot.getValue(User.class);
                if (user != null && !TextUtils.isEmpty(user.getUsername())) {
                    displayUserInfo(user.getUsername().trim());
                } else if (!TextUtils.isEmpty(fallbackName)) {
                    showFallbackMessage("Welcome, " + fallbackName + "!");
                    Toast.makeText(WelcomeActivity.this, "Profile missing username, showing email instead", Toast.LENGTH_SHORT).show();
                } else {
                    showFallbackMessage("Welcome!");
                    Toast.makeText(WelcomeActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showLoadingState(false);
                showFallbackMessage("Welcome!");
                Toast.makeText(WelcomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void displayUserInfo(String username) {
        welcomeText.setText("Welcome, " + username + "!");
        welcomeText.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.GONE);
    }

    private void showFallbackMessage(String message) {
        welcomeText.setText(message);
        welcomeText.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.GONE);
    }

    private void showLoadingState(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            statusText.setText("Loading your profile...");
            statusText.setVisibility(View.VISIBLE);
            welcomeText.setVisibility(View.INVISIBLE);
        } else if (welcomeText.getVisibility() != View.VISIBLE) {
            statusText.setVisibility(View.GONE);
        }
    }

}
