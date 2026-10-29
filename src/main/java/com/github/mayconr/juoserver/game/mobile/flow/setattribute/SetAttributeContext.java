package com.github.mayconr.juoserver.game.mobile.flow.setattribute;

import com.github.mayconr.juoserver.game.mobile.MobileAttribute;
import com.github.mayconr.juoserver.game.mobile.shared.MobileStatusContext;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class SetAttributeContext extends AbstractSyncFlowContext<Void> implements MobileStatusContext {
    private final UOMobile mobile;
    private final MobileAttribute attribute;
    private final int value;
}
