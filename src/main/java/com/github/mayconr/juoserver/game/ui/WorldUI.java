package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.GumpHandler;

/** UI operations available to shard code, without client packet handlers. */
public interface WorldUI {
    void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    void sendSkillGump(UOPlayer player, int requestedSkillSerialId);

    void sendStatusGump(UOPlayer player, int requestedStatusSerial);
}
