package com.github.mayconr.juoserver.game.mobile.flow.mount;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class MountContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;
    private final UONpc mountNpc;

    private UOItem mountItem;
}
