package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UOPlayer extends UOMobile {

    public static final String MOVEMENT_SEQUENCE = "MOVEMENT.EXPECTED_SEQUENCE";

    private UUID accountId;
    private String password;
    private boolean connected;
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
