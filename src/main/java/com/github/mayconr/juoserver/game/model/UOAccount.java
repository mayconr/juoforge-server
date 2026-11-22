package com.github.mayconr.juoserver.game.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UOAccount {
    private final UUID id;
    private final String username;
    private String password;
    private final int accessLevel;

    public UOAccount(UUID id, String username, String password) {
        this(id, username, password, 0);
    }

    public UOAccount(UUID id, String username, String password, int accessLevel) {
        if (accessLevel < 0) {
            throw new IllegalArgumentException("Access level must be non-negative");
        }
        this.accessLevel = accessLevel;
        this.id = id;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "UOAccount{" + "id='" + id + '\'' + ", username='" + username + '\'' + '}';
    }
}
