package com.example.khanghvse184160.model;

import androidx.annotation.Keep;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@Keep // Prevent R8/ProGuard from stripping this model (optional but recommended)
@IgnoreExtraProperties // Ignore unknown fields when reading from RTDB
public class User {

    private String uid;
    private String username;
    private String email;
    private Long createdAt; // optional timestamp (ms since epoch)

    // Required by Firebase
    public User() { }

    // Keep your existing constructor order for backward compatibility
    public User(String username, String email, String uid) {
        this.username = username;
        this.email = email;
        this.uid = uid;
        this.createdAt = System.currentTimeMillis();
    }

    // Canonical constructor (use if you want full control)
    public User(String uid, String username, String email, Long createdAt) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    // Convenience factory
    public static User of(String uid, String username, String email) {
        return new User(uid, username, email, System.currentTimeMillis());
    }

    // Getters / Setters (Firebase needs public getters to serialize private fields)
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    // Helpful for partial updates: dbRef.updateChildren(user.toMap())
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("uid", uid);
        m.put("username", username);
        m.put("email", email);
        m.put("createdAt", createdAt);
        return m;
    }
}
