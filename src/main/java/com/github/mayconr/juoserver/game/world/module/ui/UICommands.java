package com.github.mayconr.juoserver.game.world.module.ui;

import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.module.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.world.module.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;

import java.util.List;

public interface UICommands {

    void tooltipRequest(UOPlayer player, List<Integer> serials);

    void doubleClick(UOPlayer player, DoubleClick doubleClick);

    void singleClick(UOPlayer player, SingleClickRequest request);

    void sendSkillGump(UOPlayer player, int requestedSkillSerialId);

    void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    void onGumpSelection(UOPlayer player, GumpSelection gumpSelection);

    void sendMessage(UOPlayer player, String text, MessageOptions options);

    void sendStatusGump(UOPlayer player, int requestedStatusSerial);
}
