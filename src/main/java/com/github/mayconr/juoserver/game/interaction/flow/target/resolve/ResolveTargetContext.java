package com.github.mayconr.juoserver.game.interaction.flow.target.resolve;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.shared.step.LightOfSightContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.network.packet.Target;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
@Slf4j
public class ResolveTargetContext extends AbstractSyncFlowContext<Void> implements LightOfSightContext {
    private final UOPlayer player;
    private final Target target;
    private final boolean validateLOS;

    private Consumer<TargetResult> callback;
    private Location targetLocation;
    private CursorTarget cursorTarget;
    private Integer serialId;
    private Integer modelId;

    private UOMobile mobile;
    private UOItem item;

    private TargetResult targetResult;

    public static ResolveTargetContext of(UOPlayer player, Target target, boolean validateLOS) {
        return new ResolveTargetContext(player, target, validateLOS);
    }

    @Override
    public UOPlayer targetSource() {
        return player;
    }

    @Override
    public Location targetDestination() {
        return targetLocation;
    }
}
