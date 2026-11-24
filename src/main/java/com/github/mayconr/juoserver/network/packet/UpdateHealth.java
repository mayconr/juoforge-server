package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.mobile.VitalType;
import com.github.mayconr.juoserver.game.model.UOMobile;

/**
 * Let the wallpaper gray when hitpoints = 0
 */
public class UpdateHealth extends UpdateVitals {
    public static final int CODE = (byte) 0xA1;
    public UpdateHealth(UOMobile mobile) {
        this(mobile.getSerialId(), mobile.getMaxHitpoints(), mobile.getHitpoints());
    }

    public UpdateHealth(int serial, int max, int value) {
        super(VitalType.HITPOINTS, serial, max, value);
    }
}
