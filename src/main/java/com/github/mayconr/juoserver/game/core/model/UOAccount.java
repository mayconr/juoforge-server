package com.github.mayconr.juoserver.game.core.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UOAccount {
    private final UUID id;
    private final String username;
    private String password;

    public UOAccount(UUID id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return "UOAccount{" + "id='" + id + '\'' + ", username='" + username + '\'' + '}';
    }
}
