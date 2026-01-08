package com.github.mayconr.juoserver.game.core.model;

public class UOAccount {
    private final String id;
    private final String username;
    private String password;

    public UOAccount(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UOAccount{" + "id='" + id + '\'' + ", username='" + username + '\'' + '}';
    }
}
