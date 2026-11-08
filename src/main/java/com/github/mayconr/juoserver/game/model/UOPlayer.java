package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.infrastructure.i18n.ClientLocale;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UOPlayer extends UOMobile {

    public static final String MOVEMENT_SEQUENCE = "MOVEMENT.EXPECTED_SEQUENCE";

    private volatile ClientLocale locale = ClientLocale.defaultLocale();

    public void setLocale(ClientLocale locale) {
        this.locale = Objects.requireNonNull(locale, "Player locale is required");
    }

    private UUID accountId;
    private String password;
    private volatile boolean connected;
    private VendorSession vendorSession;
    private int ghostModelId;

    public UOPlayer(UOMobileData data) {
        super(data);
        this.accountId = data.getAccountId();
        this.ghostModelId = data.getGhostModelId();
    }

    @Override
    protected void populateData(UOMobileData data) {
        super.populateData(data);
        data.setGhostModelId(ghostModelId);
        data.setAccountId(accountId);
        data.setGhostModelId(ghostModelId);
    }

    public int movementSequence() {
        return runtimeAttributes().getOrDefault(MOVEMENT_SEQUENCE, 0);
    }

    public void movementSequence(int sequence) {
        runtimeAttributes().set(MOVEMENT_SEQUENCE, sequence);
    }

}
