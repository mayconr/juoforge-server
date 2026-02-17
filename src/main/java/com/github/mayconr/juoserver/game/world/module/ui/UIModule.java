package com.github.mayconr.juoserver.game.world.module.ui;

import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.world.module.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.world.module.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.game.world.module.ui.gump.GumpSystem;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class UIModule implements WorldModule, UICommands {

    private final TooltipHandler tooltipHandler;
    private final DoubleClickHandler doubleClickHandler;
    private final SingleClickHandler singleClickHandler;
    private final SkillUIHandler skillUIHandler;
    private final GumpSystem gumpSystem;
    private final MessageHandler messageHandler;
    private final StatusHandler statusHandler;

    @Override
    public void update(double delta) {

    }

    @Override
    public void tooltipRequest(UOPlayer player, List<Integer> serialIds) {
        tooltipHandler.tooltipRequest(player, serialIds);
    }

    @Override
    public void doubleClick(UOPlayer player, DoubleClick doubleClick) {
        doubleClickHandler.doubleClick(player, doubleClick);
    }

    @Override
    public void singleClick(UOPlayer player, SingleClickRequest request) {
        singleClickHandler.singleClick(player, request);
    }

    @Override
    public void sendSkillGump(UOPlayer player, int requestedSkillSerialId) {
        skillUIHandler.sendSkillGump(player, requestedSkillSerialId);
    }

    @Override
    public void sendGump(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler) {
        gumpSystem.send(player, gumpUI, handler);
    }

    @Override
    public void onGumpSelection(UOPlayer player, GumpSelection gumpSelection) {
        gumpSystem.onGumpSelection(player, gumpSelection);
    }

    @Override
    public void sendMessage(UOPlayer player, String text, MessageOptions options) {
        messageHandler.sendMessage(player, text, options);
    }

    @Override
    public void sendStatusGump(UOPlayer player, int requestedStatusSerial) {
        statusHandler.sendStatusGump(player, requestedStatusSerial);
    }
}
