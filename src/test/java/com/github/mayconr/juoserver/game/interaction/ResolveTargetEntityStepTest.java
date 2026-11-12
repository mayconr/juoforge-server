package com.github.mayconr.juoserver.game.interaction;

import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.ResolveTargetContext;
import com.github.mayconr.juoserver.game.interaction.flow.target.resolve.resolution.ResolveTargetEntityStep;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResolveTargetEntityStepTest {
    private final WorldStorage storage = mock(WorldStorage.class, RETURNS_DEEP_STUBS);
    private final UOPlayer player = mock(UOPlayer.class);
    private final ResolveTargetEntityStep step = new ResolveTargetEntityStep(storage);

    @ParameterizedTest
    @CsvSource({"1, BACKPACK, false", "2, BACKPACK, true", "1, BANK, true"})
    void onlyOwnBackpackSkipsLineOfSight(int owner, Layer layer, boolean validateLOS) {
        when(player.getSerialId()).thenReturn(1);
        var backpack = item(0x40000001, ItemLocation.equipped(owner));
        when(backpack.getLayer()).thenReturn(layer);
        var bag = item(0x40000002, ItemLocation.container(backpack.getSerialId()));
        var target = item(0x40000003, ItemLocation.container(bag.getSerialId()));
        var context = context(target);

        assertTrue(step.execute(context).flowSucceeded());

        assertEquals(validateLOS, context.shouldValidateLOS());
        if (!validateLOS) {
            assertSame(player, context.targetDestination());
        }
    }

    @Test
    void groundItemUsesServerLocationAndKeepsLineOfSight() {
        var target = item(0x40000001, ItemLocation.ground());
        when(target.isOnTheGround()).thenReturn(true);
        var context = context(target);

        assertTrue(step.execute(context).flowSucceeded());

        assertSame(target, context.targetDestination());
        assertTrue(context.shouldValidateLOS());
    }

    @Test
    void cyclicContainersDoNotSkipLineOfSight() {
        var target = item(0x40000001, ItemLocation.container(0x40000001));
        var context = context(target);

        assertTrue(step.execute(context).flowSucceeded());

        assertTrue(context.shouldValidateLOS());
    }

    private UOItem item(int serial, ItemLocation location) {
        var item = mock(UOItem.class);
        when(item.getSerialId()).thenReturn(serial);
        when(item.getCurrentLocation()).thenReturn(location);
        when(storage.items().get(serial)).thenReturn(Optional.of(item));
        return item;
    }

    private ResolveTargetContext context(UOItem target) {
        var context = ResolveTargetContext.of(player, null, true);
        context.setSerialId(target.getSerialId());
        context.setTargetLocation(new PointInTheWorld(50, 60, 0));
        return context;
    }
}
