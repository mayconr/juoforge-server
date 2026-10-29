package com.github.mayconr.juoserver.game.region;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.region.flow.transition.*;
import com.github.mayconr.juoserver.game.region.shared.UpdateMobileRegionStep;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.region.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.mobile.flow.movement.apply.ApplyMoveStep;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.relocation.ApplyTeleport;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegionTransitionFlowTest {
    private final com.github.mayconr.juoserver.game.ai.AIModule ai =
            mock(com.github.mayconr.juoserver.game.ai.AIModule.class);
    private final RegionSystem regions = mock(RegionSystem.class);
    private final DefaultEventBus events = new DefaultEventBus();
    private final RegionModule module = new RegionModuleImpl(regions);
    private final UOPlayer player = mock(UOPlayer.class);
    private final Location from = mock(Location.class);
    private final Location to = mock(Location.class);
    private final RegionNode city = new RegionNode("city", "City", RegionType.CITY, null);
    private final RegionNode first = new RegionNode("first", "First", RegionType.CITY, null);
    private final RegionNode second = new RegionNode("second", "Second", RegionType.CITY, null);
    private final List<MobileRegionChanged> changes = new ArrayList<>();

    @BeforeEach
    void initialize() {
        when(player.getSerialId()).thenReturn(42);
        city.addChild(first);
        city.addChild(second);
        var registry = new DefaultFlowRegistry();
        registry.register("regionTransition", RegionTransitionFlowDefinition.build(regions, events, module,
                        ai, mock(com.github.mayconr.juoserver.game.mobile.WorldMobile.class)),
                RegionTransitionContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        events.register(MobileRegionChanged.class, changes::add);
    }

    private void move(UOMobile mobile, RegionNode oldRegion, RegionNode newRegion, boolean teleport) {
        when(regions.getRegion(from)).thenReturn(Optional.ofNullable(oldRegion));
        when(regions.getRegion(to)).thenReturn(Optional.ofNullable(newRegion));
        module.regionTransition(mobile, from, to);
    }

    @Test
    void activatesAiThroughFlowOnlyWhenPlayerChangesRegion() {
        when(regions.findAiActivationRegion(second)).thenReturn(Optional.of(city));
        move(player, first, second, false);
        verify(ai).activateRegion(city);
        clearInvocations(ai);

        move(player, second, second, false);
        move(mock(UONpc.class), first, second, false);
        clearInvocations(ai);
        move(player, second, null, false);
        verify(ai).deactivateRegion(city);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doesNotRequestDeactivationBetweenMicroregionsInSameAiScope(boolean teleport) {
        city.setAiActivationScope(true);
        when(regions.findAiActivationRegion(first)).thenReturn(Optional.of(city));
        when(regions.findAiActivationRegion(second)).thenReturn(Optional.of(city));

        move(player, first, second, teleport);

        verify(ai, never()).deactivateRegion(any());
        verify(ai).activateRegion(city);
        assertEquals(Set.of(42), module.getPlayerSerials(city));
        assertEquals(List.of(new MobileRegionChanged(player, first, second)), changes);
    }

    @Test
    void indexesFirstMovementAndUpdatesBeforePublishingTransition() {
        assertTrue(module.getPlayerSerials(city).isEmpty());
        move(player, first, first, false);
        var snapshot = module.getPlayerSerials(first);
        assertEquals(Set.of(42), snapshot);
        assertEquals(Set.of(42), module.getPlayerSerials(city));
        assertTrue(changes.isEmpty());
        events.register(MobileRegionChanged.class, event -> {
            assertTrue(module.getPlayerSerials(first).isEmpty());
            assertEquals(Set.of(42), module.getPlayerSerials(second));
            assertEquals(Set.of(42), module.getPlayerSerials(city));
        });
        move(player, first, second, false);
        assertEquals(List.of(new MobileRegionChanged(player, first, second)), changes);
        assertEquals(Set.of(42), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(7));
        move(player, second, second, false);
        assertEquals(1, changes.size());
    }

    @Test
    void handlesMissingRegionsAndRemovesPreviousMembership() {
        move(player, null, first, false);
        assertEquals(Set.of(42), module.getPlayerSerials(first));
        move(player, first, null, false);
        assertTrue(module.getPlayerSerials(first).isEmpty());
        assertTrue(module.getPlayerSerials(city).isEmpty());
        move(player, null, null, false);
        assertTrue(changes.isEmpty());
    }

    @Test
    void preservesNpcEventsWithoutIndexingNpcs() {
        var npc = mock(UONpc.class);
        move(npc, first, second, false);
        assertEquals(List.of(new MobileRegionChanged(npc, first, second)), changes);
        assertTrue(module.getPlayerSerials(second).isEmpty());
        assertTrue(module.getPlayerSerials(city).isEmpty());
    }

    @Test
    void teleportMovementReplacesMembershipWithoutDuplicatesOrOtherPlayerLoss() {
        move(player, first, first, false);
        var other = mock(UOPlayer.class);
        when(other.getSerialId()).thenReturn(43);
        move(other, first, first, false);
        move(player, first, second, true);
        move(player, second, second, true);
        assertEquals(Set.of(43), module.getPlayerSerials(first));
        assertEquals(Set.of(42), module.getPlayerSerials(second));
        assertEquals(Set.of(42, 43), module.getPlayerSerials(city));
        assertEquals(1, changes.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void actualMovementKeepsOriginBeforeMobileLocationChanges(boolean teleport) {
        var position = new java.util.concurrent.atomic.AtomicReference<Location>(new PointInTheWorld(10, 20, 0));
        when(player.getX()).thenAnswer(call -> position.get().getX());
        when(player.getY()).thenAnswer(call -> position.get().getY());
        when(player.getZ()).thenAnswer(call -> position.get().getZ());
        doAnswer(call -> {
            position.set(call.getArgument(0));
            return null;
        }).when(player).setLocation(any(Location.class));
        when(regions.getRegion(any(Location.class))).thenAnswer(call ->
                Optional.of(call.<Location>getArgument(0).getX() == 10 ? first : second));
        var storage = mock(RealmStorage.class);
        var destination = new PointInTheWorld(11, 20, 0);
        if (teleport) {
            var context = TeleportContext.of(player, destination);
            context.setDirection(Direction.EAST);
            new ApplyTeleport(storage).execute(context);
            new UpdateMobileRegionStep<TeleportContext>(module).execute(context);
            new com.github.mayconr.juoserver.game.mobile.flow.teleport.notification.NotifyTeleportStep(events).execute(context);
        } else {
            var context = MovementContext.of(player, Direction.EAST, false);
            context.setTargetLocation(destination);
            new ApplyMoveStep(storage).execute(context);
            new UpdateMobileRegionStep<MovementContext>(module).execute(context);
            new com.github.mayconr.juoserver.game.mobile.flow.movement.notification.NotifyMovementStep(events).execute(context);
        }
        assertEquals(11, player.getX());
        assertEquals(List.of(new MobileRegionChanged(player, first, second)), changes);
        assertTrue(module.getPlayerSerials(first).isEmpty());
        assertEquals(Set.of(42), module.getPlayerSerials(second));
        verify(storage).updateMobileLocation(player, new PointInTheWorld(10, 20, 0), destination);
    }
}
