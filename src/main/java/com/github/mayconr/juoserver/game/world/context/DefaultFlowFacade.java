package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.SyncFlowContext;
import com.github.mayconr.juoserver.infrastructure.flow.FlowTraceFormatter;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Builder
public class DefaultFlowFacade implements FlowFacade {

    private final FlowRegistry registry;

    @Override
    public <T extends AbstractContext> void execute(T context) {
        Flow<T> flow = registry.get((Class<T>) context.getClass());

        if(flow == null){
            throw new IllegalStateException(
                    "Flow not registered for " + context.getClass()
            );
        }

        flow.execute(context);

        log.info("Executed flow steps:\n{}", FlowTraceFormatter.format(context.trace()));
    }
}
