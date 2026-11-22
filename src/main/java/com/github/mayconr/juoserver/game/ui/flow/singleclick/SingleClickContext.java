package com.github.mayconr.juoserver.game.ui.flow.singleclick;

import com.github.mayconr.juoserver.game.messaging.template.MessageStyleTemplate;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class SingleClickContext extends AbstractSyncFlowContext<Void> {
    private final UOPlayer player;
    private final SingleClickRequest request;
    @Setter
    private UOObject<?> object;
    @Setter
    private MessageStyleTemplate messageStyle;
}
