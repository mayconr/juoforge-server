package com.github.mayconr.juoserver.game.temporal;

import com.github.mayconr.juoserver.game.GamePlaySettings.Temporal;
import com.github.mayconr.juoserver.game.model.event.TemporalEvent;
import com.github.mayconr.juoserver.game.model.event.TemporalEvent.GameDateTime;
import com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.CachedWorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.property.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.DAY_CHANGED;
import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.HOUR_CHANGED;
import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.WEEK_CHANGED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TemporalModuleImplTest {
    private static final Temporal SETTINGS = new Temporal();
    private static final double REAL_SECONDS_PER_MINUTE = SETTINGS.realSecondsPerGameMinute();
    private static final double REAL_SECONDS_PER_HOUR = REAL_SECONDS_PER_MINUTE * GameCalendar.MINUTES_PER_HOUR;
    private static final double REAL_SECONDS_PER_DAY = REAL_SECONDS_PER_HOUR * GameCalendar.HOURS_PER_DAY;

    private final EventBus eventBus = mock(EventBus.class);
    private final Properties properties = new CachedWorldStorage(null, null, null, null).properties();
    private final TemporalModuleImpl module = createModule(SETTINGS);

    @BeforeEach
    void setUp() {
        module.initialize(mock(ModuleContext.class));
    }

    @Test
    void startsAtEpoch() {
        assertEquals(GameCalendar.fromEpochMinute(0), module.now());
    }

    @Test
    void advancesOneGameMinuteEveryFiveRealSecondsByDefault() {
        module.update(5);

        assertEquals(1, module.now().minute());
    }

    @Test
    void restoresThePersistedClock() {
        long epochSeconds = new GameDateTime(1, 3, 2, 10, 14, 30).toEpochMinute() * GameCalendar.SECONDS_PER_MINUTE;
        properties.put(TemporalModuleImpl.CLOCK_KEY, new GameClockState(epochSeconds));
        var restored = createModule(SETTINGS);

        restored.initialize(mock(ModuleContext.class));

        assertEquals(new GameDateTime(1, 3, 2, 10, 14, 30), restored.now());
    }

    @Test
    void persistsTheClockWhenTheGameMinuteChanges() {
        module.update(REAL_SECONDS_PER_MINUTE / 2);
        assertEquals(Optional.empty(), properties.get(TemporalModuleImpl.CLOCK_KEY));

        module.update(REAL_SECONDS_PER_MINUTE / 2);

        assertEquals(
                Optional.of(new GameClockState(GameCalendar.SECONDS_PER_MINUTE)),
                properties.get(TemporalModuleImpl.CLOCK_KEY)
        );
    }

    @Test
    void usesConfiguredRealSecondsPerGameMinute() {
        var fastModule = createModule(new Temporal(1));
        fastModule.initialize(mock(ModuleContext.class));

        fastModule.update(GameCalendar.MINUTES_PER_HOUR);

        assertEquals(1, fastModule.now().hour());
        assertEquals(
                List.of(new TemporalEvent(HOUR_CHANGED, new GameDateTime(1, 1, 1, 1, 1, 0))),
                publishedEvents()
        );
    }

    @Test
    void accumulatesSmallGameLoopDeltas() {
        int ticks = (int) Math.round(REAL_SECONDS_PER_MINUTE / 0.05);
        for (int i = 0; i < ticks; i++) {
            module.update(0.05);
        }

        assertEquals(1, module.now().minute());
    }

    @Test
    void publishesNothingWithinTheSameHour() {
        module.update(REAL_SECONDS_PER_HOUR - REAL_SECONDS_PER_MINUTE);

        verifyNoInteractions(eventBus);
    }

    @Test
    void publishesHourChangedWhenHourRollsOver() {
        module.update(REAL_SECONDS_PER_HOUR);

        assertEquals(
                List.of(new TemporalEvent(HOUR_CHANGED, new GameDateTime(1, 1, 1, 1, 1, 0))),
                publishedEvents()
        );
    }

    @Test
    void publishesHourAndDayChangedWhenDayRollsOver() {
        module.update(REAL_SECONDS_PER_DAY);

        var dateTime = new GameDateTime(1, 1, 1, 2, 0, 0);
        assertEquals(
                List.of(new TemporalEvent(HOUR_CHANGED, dateTime), new TemporalEvent(DAY_CHANGED, dateTime)),
                publishedEvents()
        );
    }

    @Test
    void publishesWeekChangedAfterSevenDays() {
        module.update(REAL_SECONDS_PER_DAY * GameCalendar.DAYS_PER_WEEK);

        assertEquals(List.of(HOUR_CHANGED, DAY_CHANGED, WEEK_CHANGED), publishedChanges());
    }

    @Test
    void publishesEveryPeriodWhenYearRollsOver() {
        module.update(REAL_SECONDS_PER_DAY * GameCalendar.DAYS_PER_YEAR);

        assertEquals(List.of(TemporalChange.values()), publishedChanges());
        assertEquals(new GameDateTime(2, 1, 1, 1, 0, 0), module.now());
    }

    @Test
    void listenersRegisteredOnTemporalEventCanFilterByChange() {
        var bus = new DefaultEventBus();
        var received = new ArrayList<TemporalEvent>();
        bus.register(TemporalEvent.class, received::add, event -> event.change() == DAY_CHANGED);

        var busModule = new TemporalModuleImpl(properties, bus, SETTINGS);
        busModule.initialize(mock(ModuleContext.class));
        busModule.update(REAL_SECONDS_PER_DAY);

        assertEquals(List.of(new TemporalEvent(DAY_CHANGED, new GameDateTime(1, 1, 1, 2, 0, 0))), received);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsInvalidDelta(double delta) {
        assertThrows(IllegalArgumentException.class, () -> module.update(delta));
    }

    @Test
    void failsWhenNotInitialized() {
        var uninitialized = createModule(SETTINGS);

        assertThrows(NullPointerException.class, () -> uninitialized.update(1));
    }

    private TemporalModuleImpl createModule(Temporal settings) {
        return new TemporalModuleImpl(properties, eventBus, settings);
    }

    private List<TemporalEvent> publishedEvents() {
        ArgumentCaptor<TemporalEvent> captor = ArgumentCaptor.forClass(TemporalEvent.class);
        verify(eventBus, atLeastOnce()).publish(captor.capture());
        return captor.getAllValues();
    }

    private List<TemporalChange> publishedChanges() {
        return publishedEvents().stream()
                .map(TemporalEvent::change)
                .toList();
    }
}
