package com.example.unyieldingmight;

public class User {
    public enum Role { ADMIN, CUSTOMER }
    private int id;
    private String username;
    private String password;
    private String email;
    private Role userClass;

    public User(int id, String username, String password, String email, Role userClass) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.userClass = userClass;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getUserClass() { return userClass; }
    public void setUserClass(Role userClass) { this.userClass = userClass; }

    // Helper methods
    public boolean isAdmin() { return userClass == Role.ADMIN; }
    public boolean isCustomer() { return userClass == Role.CUSTOMER; }
}
