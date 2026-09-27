package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.DefaultWorld;
import org.springframework.test.util.ReflectionTestUtils;
import com.github.mayconr.juoserver.infrastructure.flow.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShardFlowRegistrationTest {
    static class ShardContext extends AbstractSyncFlowContext<Void> {}

    private DefaultWorld world(DefaultFlowRegistry registry) {
        var world = mock(DefaultWorld.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(world, "flowRegistry", registry);
        return world;
    }

    @Test
    void factoryReceivesRuntimeAndFlowExecutesThroughSharedFacade() {
        var cfg = new DefaultWorldCfg();
        var registry = new DefaultFlowRegistry();
        var runtime = mock(ServerRuntime.class);
        when(runtime.flows()).thenReturn(new DefaultFlowFacade(registry));
        var creations = new AtomicInteger();
        var executions = new AtomicInteger();
        cfg.addFlow(ShardContext.class, actual -> {
            assertSame(runtime, actual);
            creations.incrementAndGet();
            return FlowFactory.<ShardContext>builder()
                    .step(new AbstractFlowStep<ShardContext>("ShardStep") {
                        @Override
                        public StepResult execute(ShardContext context) {
                            executions.incrementAndGet();
                            return StepResult.success();
                        }
                    }).build();
        });
        assertEquals(0, creations.get());
        var world = world(registry);
        cfg.flowList().forEach(entry -> WorldBootstrap.registerShardFlow(world, runtime, entry));
        assertEquals(1, creations.get());
        assertEquals(0, executions.get());
        assertTrue(runtime.flows().execute(new ShardContext()).flowSucceeded());
        assertEquals(1, executions.get());
        assertThrows(UnsupportedOperationException.class, () -> cfg.flowList().clear());
    }

    @Test
    void rejectsDuplicateConfigurationAndCoreContextReplacement() {
        var cfg = new DefaultWorldCfg();
        var builds = new AtomicInteger();
        cfg.addFlow(ShardContext.class, runtime -> {
            builds.incrementAndGet();
            return FlowFactory.<ShardContext>builder().build();
        });
        assertThrows(IllegalArgumentException.class, () -> cfg.addFlow(ShardContext.class,
                runtime -> FlowFactory.<ShardContext>builder().build()));
        var registry = new DefaultFlowRegistry();
        var core = FlowFactory.<ShardContext>builder().build();
        registry.register("core", core, ShardContext.class);
        assertThrows(IllegalArgumentException.class,
                () -> WorldBootstrap.registerShardFlow(world(registry), mock(ServerRuntime.class), cfg.flowList().getFirst()));
        assertEquals(1, builds.get());
        assertThrows(IllegalArgumentException.class,
                () -> registry.register("replacement", core, ShardContext.class));
        assertSame(core, registry.get(ShardContext.class));
    }

    @Test
    void rejectsNullRegistrationInputsAndFactoryResults() {
        var cfg = new DefaultWorldCfg();
        assertThrows(NullPointerException.class, () -> cfg.addFlow(ShardContext.class, null));
        assertThrows(NullPointerException.class, () -> cfg.addFlow(null,
                runtime -> FlowFactory.<ShardContext>builder().build()));
        cfg.addFlow(ShardContext.class, runtime -> null);
        var registry = new DefaultFlowRegistry();
        assertThrows(NullPointerException.class,
                () -> WorldBootstrap.registerShardFlow(world(registry), mock(ServerRuntime.class), cfg.flowList().getFirst()));
        assertNull(registry.get(ShardContext.class));
    }
}
