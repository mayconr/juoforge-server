package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public final class CombatParticipants {
    private CombatParticipants() {}

    public static boolean isAvailable(UOMobile mobile) {
        return mobile != null && mobile.isAlive()
                && (!(mobile instanceof UOPlayer player) || player.isConnected());
    }
}
