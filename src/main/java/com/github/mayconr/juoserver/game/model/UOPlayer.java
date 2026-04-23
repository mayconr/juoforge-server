package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UOPlayer extends UOMobile {
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
}
