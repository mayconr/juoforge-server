package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.WarModeType;

/** Combat operations without client packets or module lifecycle methods. */
public interface WorldCombat {
    void toggleWarMode(UOPlayer player, WarModeType type);

    /** Queues an attack for processing on the combat update. */
    void requestAttack(UOMobile attacker, int targetSerial);

    /** Queues cancellation before attacks are executed on the next combat update. */
    void requestCancelAttack(UOMobile mobile);

    void regen(UOMobile mobile, double interval);
}
