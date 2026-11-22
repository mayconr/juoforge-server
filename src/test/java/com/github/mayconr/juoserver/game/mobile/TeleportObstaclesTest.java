package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportFlowDefinition;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.region.RegionModule;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TeleportObstaclesTest {
    private final UOFileReader fileReader = mock(UOFileReader.class);
    private final EventBus events = mock(EventBus.class);
    private final WorldStorage storage = mock(WorldStorage.class);
    private final UOPlayer player = mock(UOPlayer.class);
    private final PointInTheWorld destination = new PointInTheWorld(10, 10, 0);
    private final MobileModuleImpl module = new MobileModuleImpl(null, null, events, storage, new GamePlaySettings());

    @BeforeEach
    void setUp() {
        var registry = new DefaultFlowRegistry();
        var modules = GameModules.builder().region(mock(RegionModule.class)).build();
        var infra = GameInfra.builder().storage(storage).eventBus(events).fileReader(fileReader).build();
        registry.register("teleport", TeleportFlowDefinition.build(modules, infra), TeleportContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
    }

    @Test
    void rejectsObstaclesByDefault() {
        when(fileReader.hasBlockingCollision(player, destination)).thenReturn(true);

        module.teleport(player, destination);

        assertNotTeleported();
    }

    @Test
    void rejectsObstaclesWhenNotIgnored() {
        when(fileReader.hasBlockingCollision(player, destination)).thenReturn(true);

        module.teleport(player, destination, false);

        assertNotTeleported();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void teleportsToAFreeDestination(boolean ignoreObstacles) {
        module.teleport(player, destination, ignoreObstacles);

        assertTeleported();
    }

    @Test
    void ignoredObstaclesAreNotEvenValidated() {
        when(fileReader.hasBlockingCollision(player, destination)).thenReturn(true);

        module.teleport(player, destination, true);

        assertTeleported();
        verifyNoInteractions(fileReader);
    }

    @Test
    void ignoringObstaclesKeepsTheOtherValidations() {
        when(player.getX()).thenReturn(10);
        when(player.getY()).thenReturn(10);

        module.teleport(player, destination, true);

        assertNotTeleported();
    }

    private void assertTeleported() {
        verify(player).setLocation(destination);
        verify(events).publish(any(MobileMoved.class));
    }

    private void assertNotTeleported() {
        verify(player, never()).setLocation(destination);
        verify(events, never()).publish(any(MobileMoved.class));
    }
}
