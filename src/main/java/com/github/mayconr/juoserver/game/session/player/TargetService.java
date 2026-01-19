package com.github.mayconr.juoserver.game.session.player;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.SelectedObject;
import com.github.mayconr.juoserver.common.event.SelectedStatics;
import com.github.mayconr.juoserver.game.model.CursorTarget;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.Target;

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
