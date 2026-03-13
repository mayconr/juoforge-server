package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class UOPlayer extends UOMobile {
    private UUID accountId;
    private String password;
    private boolean connected;
    private VendorSession vendorSession;

    public UOPlayer(Integer serialId, Integer modelId, Integer x, Integer y, Integer z, String name, String displayName, Map<String, Object> persistentAttrMap) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap);
    }

    public UOPlayer(UOMobile mobile, UUID accountId) {
        super(mobile);
        this.accountId = accountId;
    }

}
