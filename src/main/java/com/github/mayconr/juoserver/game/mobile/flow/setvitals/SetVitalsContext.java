package com.github.mayconr.juoserver.game.mobile.flow.setvitals;

import com.github.mayconr.juoserver.game.mobile.VitalType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class SetVitalsContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;
    private final VitalType type;
    private final int max;
    private final int value;
}
