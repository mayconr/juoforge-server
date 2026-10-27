package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.ui.gump.DeclarativeGumpUI;
import com.github.mayconr.juoserver.game.ui.gump.GumpHandler;
import com.github.mayconr.juoserver.game.ui.gump.GumpSystem;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickContext;
import com.github.mayconr.juoserver.game.ui.flow.paperdoll.OpenPaperdollContext;
import com.github.mayconr.juoserver.game.ui.flow.skill.SkillGumpContext;
import com.github.mayconr.juoserver.game.ui.flow.singleclick.SingleClickContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class UIModuleImpl implements UIModule {

    private final TooltipHandler tooltipHandler;
    private final GumpSystem gumpSystem;
    private final StatusHandler statusHandler;
    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void tooltipRequest(UOPlayer player, List<Integer> serialIds) {
        tooltipHandler.tooltipRequest(player, serialIds);
    }

    @Override
    public void openPaperdoll(UOPlayer player, int mobileSerial) {
        flows.execute(new OpenPaperdollContext(player, mobileSerial));
    }

    @Override
    public void doubleClick(UOPlayer player, DoubleClick doubleClick) {
        flows.execute(new DoubleClickContext(player, doubleClick));
    }

    @Override
    public void singleClick(UOPlayer player, SingleClickRequest request) {
        flows.execute(new SingleClickContext(player, request));
    }

    @Override
    public void sendSkillGump(UOPlayer player, int requestedSkillSerialId, SendSkillType type) {
        var result = flows.execute(new SkillGumpContext(player, requestedSkillSerialId,
                java.util.Objects.requireNonNull(type, "Skill packet type is required")));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("sendSkillGump", result, null);
        }
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
    public void sendStatusGump(UOPlayer player, int requestedStatusSerial) {
        statusHandler.sendStatusGump(player, requestedStatusSerial);
    }
}
