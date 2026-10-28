package com.github.mayconr.juoserver.game.ui.flow.tooltip;

import com.github.mayconr.juoserver.game.model.TooltipSupport;
import com.github.mayconr.juoserver.game.model.event.TooltipRequested;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.ArrayList;

public final class RequestTooltipStep extends AbstractFlowStep<TooltipContext> {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public RequestTooltipStep(EventBus eventBus, RealmStorage storage) {
        super("request-tooltip");
        this.eventBus = eventBus;
        this.storage = storage;
    }

    @Override
    public StepResult execute(TooltipContext context) {
        var player = context.getPlayer();
        var serials = context.getSerials();
        final var objects = new ArrayList<TooltipSupport>();
        for (int serialId : serials) {
            // Mobiles
            if (SerialGenerator.isMobile(serialId)) {
                storage.getMobile(serialId).ifPresent(objects::add);
                continue;
            }
            // Items
            if (SerialGenerator.isItem(serialId)) {
                storage.getItem(serialId).ifPresent(objects::add);
                continue;
            }
            // Vendor session (buy gump)
            if (SerialGenerator.isVirtual(serialId)) {
                var vendorSession = player.getVendorSession();
                if (vendorSession != null) {
                    objects.add(vendorSession.items().get(serialId));
                }
            }
        }

        if (objects.isEmpty()) {
            return StepResult.success();
        }
        eventBus.publish(new TooltipRequested(player, objects));
        return StepResult.success();
    }

}
