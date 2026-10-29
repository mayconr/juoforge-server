package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.mobile.shared.ValidateBackpackWeightContext;
import com.github.mayconr.juoserver.game.mobile.shared.ValidateBackpackWeightStep;
import com.github.mayconr.juoserver.game.model.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidateBackpackWeightStepTest {
    private final WorldMobile mobiles = mock(WorldMobile.class);
    private final WorldContainer containers = mock(WorldContainer.class);
    private final UOMobile mobile = mock(UOMobile.class);
    private final UOContainer backpack = mock(UOContainer.class);
    private final ValidateBackpackWeightStep<Context> step = new ValidateBackpackWeightStep<>(mobiles, containers);

    private record Context(UOMobile mobile, int addedWeight) implements ValidateBackpackWeightContext {}

    @ParameterizedTest
    @CsvSource({"60, true", "61, false"})
    void validatesAdditionalWeightAgainstCapacity(int currentWeight, boolean succeeds) {
        prepare(currentWeight);
        var result = step.execute(new Context(mobile, 20));
        assertEquals(succeeds, result.flowSucceeded());
        if (!succeeds) assertEquals("BACKPACK_OVERWEIGHT", result.code());
    }

    @Test
    void acceptsZeroAdditionalWeightAtCapacity() {
        prepare(80);
        assertTrue(step.execute(new Context(mobile, 0)).flowSucceeded());
    }

    @Test
    void rejectsMissingBackpack() {
        when(mobile.getBackpack()).thenReturn(null);
        assertEquals("BACKPACK_NOT_FOUND", step.execute(new Context(mobile, 20)).code());
    }

    @Test
    void rejectsUnloadedBackpack() {
        when(mobile.getBackpack()).thenReturn(100);
        assertEquals("BACKPACK_NOT_FOUND", step.execute(new Context(mobile, 20)).code());
    }

    private void prepare(long currentWeight) {
        when(mobile.getBackpack()).thenReturn(100);
        when(containers.getContainerBySerialId(100)).thenReturn(Optional.of(backpack));
        when(containers.getWeight(backpack)).thenReturn(currentWeight);
        when(mobiles.calculateMaxWeight(mobile)).thenReturn(80);
    }
}
