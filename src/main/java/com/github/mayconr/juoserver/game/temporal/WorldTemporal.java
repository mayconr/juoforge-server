package com.github.mayconr.juoserver.game.temporal;

import com.github.mayconr.juoserver.game.model.event.TemporalEvent.GameDateTime;

public interface WorldTemporal {

    /** Current game date and time. */
    GameDateTime now();
}
