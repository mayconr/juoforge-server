package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.infrastructure.flow.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleOperationExceptionTest {
    private static class Context extends AbstractContext {}

    @Test
    void unexpectedFlowFailureRetainsOriginalCause() {
        var cause = new IllegalStateException("Storage unavailable");
        var flow = FlowFactory.<Context>builder().step(new AbstractFlowStep<Context>("storage") {
            @Override
            public StepResult execute(Context context) {
                throw cause;
            }
        }).build();

        var error = ModuleOperationException.fromFailure("createItem", flow.execute(new Context()), "{item.creation_failed}");

        assertSame(cause, error.getCause());
        assertEquals("FLOW_EXCEPTION", error.getCode());
        assertEquals("{item.creation_failed}", error.getDisplayMessage());
        assertTrue(error.getMessage().contains("Storage unavailable"));
    }

    @Test
    void nestedModuleFailurePreservesCodeDisplayMessageAndCause() {
        var nested = new ModuleOperationException("createItem", "ITEM_TEMPLATE_NOT_FOUND", "Missing horse_item",
                "{item.template_not_found}", null);
        var failure = StepResult.failure("FLOW_EXCEPTION", "CreateMountItem failed", null, nested);

        var error = ModuleOperationException.fromFailure("mount", failure, "{mount.failed}");

        assertEquals("mount", error.getOperation());
        assertEquals("ITEM_TEMPLATE_NOT_FOUND", error.getCode());
        assertEquals("{item.template_not_found}", error.getDisplayMessage());
        assertSame(nested, error.getCause());
    }
}
