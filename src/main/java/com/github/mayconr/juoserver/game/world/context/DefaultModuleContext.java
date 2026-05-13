package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.item.ItemModule;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Builder
public class DefaultModuleContext implements ModuleContext {

    private final ItemModule itemModule;
    private final FlowFacade flowFacade;

    @Override
    public FlowFacade flows() {
        return flowFacade;
    }
}
