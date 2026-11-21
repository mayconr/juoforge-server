package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record MobileResistancesChanged(UOMobile mobile, Values previous) implements GameEvent {
    /** Effective resistance values captured before the change. */
    public record Values(int physical, int fire, int cold, int poison, int energy) {
        public static Values from(UOMobile mobile) {
            return new Values(
                    mobile.getPhysicalResist(),
                    mobile.getFireResist(),
                    mobile.getColdResist(),
                    mobile.getPoisonResist(),
                    mobile.getEnergyResist()
            );
        }
    }
}
