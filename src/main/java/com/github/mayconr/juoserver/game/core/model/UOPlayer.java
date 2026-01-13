package com.github.mayconr.juoserver.game.core.model;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

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

    public UOPlayer(
            int serialId,
            int modelId,
            int x,
            int y,
            int z,
            String name,
            Direction direction,
            int hue,
            CharacterStatus status,
            Notoriety notoriety,
            String accountId,
            String password) {
        super(
                serialId,
                modelId,
                x,
                y,
                z,
                name,
                direction,
                hue,
                status,
                notoriety,
                Race.HUMAN,
                Gender.MALE);
        this.accountId = UUID.fromString(accountId);
        this.password = password;
    }
}
