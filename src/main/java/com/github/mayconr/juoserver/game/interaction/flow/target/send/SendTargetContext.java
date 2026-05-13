package com.github.mayconr.juoserver.game.interaction.flow.target.send;

import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class SendTargetContext extends SyncFlowContext<Void> {
    private final UOPlayer player;
    private final CursorType type;
    private final Consumer<TargetResult> consumer;
}
