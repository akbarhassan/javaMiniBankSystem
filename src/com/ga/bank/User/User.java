package com.ga.bank.User;

public abstract class User {

    private String fullName;
    private String userName;
    private String email;
    private String password;
    private boolean isLoggedIn;
    private Role role;
    private boolean isLocked;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    //    TODO: create password hasher and use it here
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }


    public User(String userName, String fullName, String email, String password, Role role) {
        this.userName=userName;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isLoggedIn = false;
        this.isLocked = false;
    }


}
