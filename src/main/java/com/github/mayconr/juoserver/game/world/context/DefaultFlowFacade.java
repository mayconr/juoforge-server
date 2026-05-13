package com.github.mayconr.juoserver.game.world.context;

import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractContext;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowTraceFormatter;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Builder
public class DefaultFlowFacade implements FlowFacade {

    private final FlowRegistry registry;

    @Override
    public <T extends AbstractContext> StepResult execute(T context) {
        Flow<T> flow = registry.get((Class<T>) context.getClass());

        if(flow == null){
            throw new IllegalStateException(
                    "Flow not registered for " + context.getClass()
            );
        }

        var result = flow.execute(context);
        if (log.isDebugEnabled()) {
            log.debug("Executed flow steps:\n{}", FlowTraceFormatter.format(context.trace()));
        }

        return  result;
    }
}
