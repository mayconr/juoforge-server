package com.github.mayconr.juoserver.game.mobile.flow.unmount;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class UnmountContext extends SyncFlowContext<Void> {
    private final UOMobile mobile;

    private UOItem mountItem;
    private String mountNpcName;
}
