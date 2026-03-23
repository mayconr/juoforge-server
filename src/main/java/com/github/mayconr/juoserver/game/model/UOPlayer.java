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

    private int deathModelId;

    public UOPlayer(Integer serialId, Integer modelId, Integer x, Integer y, Integer z, String name, String displayName, AttributeMap persistentAttrMap) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap);
        this.deathModelId = 0x3CA;
    }

    public UOPlayer(UOMobile mobile, UUID accountId) {
        super(mobile);
        this.accountId = accountId;
    }

}
