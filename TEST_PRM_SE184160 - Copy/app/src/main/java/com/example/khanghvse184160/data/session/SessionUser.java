package com.example.khanghvse184160.data.session;

public class SessionUser {
    private final long id;
    private final String email;
    private final String username;

    public SessionUser(long id, String email, String username) {
        this.id = id;
        this.email = email;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}
