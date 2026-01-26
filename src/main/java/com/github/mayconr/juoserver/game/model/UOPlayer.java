package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UOPlayer extends UOMobile {
    private final UUID accountId;
    private String password;
    private boolean connected;

    public UOPlayer(UOMobile mobile, UUID accountId) {
        super(mobile);
        this.accountId = accountId;
    }

}
