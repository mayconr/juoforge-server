package com.github.mayconr.juoserver.game.core.session.player;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.SelectedObject;
import com.github.mayconr.juoserver.game.core.event.SelectedStatics;
import com.github.mayconr.juoserver.game.core.model.CursorTarget;
import com.github.mayconr.juoserver.game.core.model.CursorType;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.core.session.SessionOutbound;
import com.github.mayconr.juoserver.game.packet.Target;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TargetService {

    private final UOMobile mobile;
    private final SessionOutbound outbound;
    private final EventBus eventBus;
    private final AtomicInteger cursorId = new AtomicInteger();
    private final Random random = new Random();

    public void handleSendTarget(CursorType type) {
        cursorId.set(random.nextInt());
        outbound.writeAndFlush(new Target(cursorId.get(), CursorTarget.LOCATION, type));
        log.info("Target [{}] sent to client", 1);
    }

    public void handleTarget(Target target) {
        if (cursorId.get() == target.getCursorId()) {
            switch (target.getTarget()) {
                case LOCATION -> eventBus.publish(
                        new SelectedStatics(
                                mobile,
                                target.getModelId(),
                                target.getX(),
                                target.getY(),
                                target.getZ()));
                case OBJECT -> eventBus.publish(
                        new SelectedObject(
                                mobile,
                                target.getClickedSerialId(),
                                target.getX(),
                                target.getY(),
                                target.getZ()));
                default -> log.warn(
                        "Invalid cursor target [{}] for mobile [{}-{}]",
                        target.getTarget(),
                        mobile.getSerialId(),
                        mobile.getName());
            }
        }
    }
}
