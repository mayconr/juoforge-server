package com.github.mayconr.juoserver.game.ui.flow.paperdoll;

import com.github.mayconr.juoserver.game.model.event.PaperdollOpened;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.OpenPaperdoll;

public final class OpenPaperdollStep extends AbstractFlowStep<OpenPaperdollContext> {
    private final EventBus events;
    private final RealmStorage storage;

    public OpenPaperdollStep(EventBus events, RealmStorage storage) {
        super("open-paperdoll");
        this.events = events;
        this.storage = storage;
    }

    @Override
    public StepResult execute(OpenPaperdollContext context) {
        var player = context.getPlayer();
        if (context.getMobileSerial() == player.getSerialId()) {
            events.publish(new PaperdollOpened(player, player, OpenPaperdoll.Flag.NORMAL));
        } else {
            storage.getMobile(context.getMobileSerial()).ifPresent(mobile ->
                    events.publish(new PaperdollOpened(player, mobile, OpenPaperdoll.Flag.NORMAL)));
        }
        return StepResult.success();
    }
}
