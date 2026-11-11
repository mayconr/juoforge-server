package com.github.mayconr.juoserver.game.temporal;

import com.github.mayconr.juoserver.game.GamePlaySettings.Temporal;
import com.github.mayconr.juoserver.game.model.event.TemporalEvent;
import com.github.mayconr.juoserver.game.model.event.TemporalEvent.GameDateTime;
import com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.property.WorldKey;
import com.github.mayconr.juoserver.infrastructure.storage.property.Properties;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.LongUnaryOperator;

import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.DAY_CHANGED;
import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.HOUR_CHANGED;
import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.MONTH_CHANGED;
import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.WEEK_CHANGED;
import static com.github.mayconr.juoserver.game.model.event.TemporalEvent.TemporalChange.YEAR_CHANGED;

@RequiredArgsConstructor
public class TemporalModuleImpl implements TemporalModule {
    static final WorldKey<GameClockState> CLOCK_KEY = WorldKey.of("temporal.clock", GameClockState.class);

    /** Absorbs floating-point error so accumulated ticks reach whole game seconds. */
    private static final double ROUNDING_TOLERANCE = 1e-9;

    private final Properties properties;
    private final EventBus eventBus;
    private final Temporal settings;

    private double accumulatedGameSeconds;
    private GameClock clock;

    @Override
    public void initialize(ModuleContext context) {
        this.clock = restoreClock();
        this.accumulatedGameSeconds = 0;
    }

    private GameClock restoreClock() {
        return properties.get(CLOCK_KEY)
                .map(GameClock::restore)
                .orElseGet(() -> new GameClock(0));
    }

    @Override
    public GameDateTime now() {
        return GameCalendar.fromEpochSecond(clock().epochSeconds());
    }

    @Override
    public void update(double delta) {
        validateDelta(delta);
        long elapsedSeconds = consumeElapsedGameSeconds(delta);

        if (elapsedSeconds == 0) {
            return;
        }

        long previousMinute = currentEpochMinute();
        clock().advance(elapsedSeconds);
        long currentMinute = currentEpochMinute();

        if (currentMinute != previousMinute) {
            persistClock();
            publishChanges(previousMinute, currentMinute);
        }
    }

    /** Runs once per game minute; the world storage writes only the latest value on the next save. */
    private void persistClock() {
        properties.put(CLOCK_KEY, clock().state());
    }

    private long consumeElapsedGameSeconds(double delta) {
        accumulatedGameSeconds += GameCalendar.toGameSeconds(delta, settings);
        long elapsedSeconds = (long) Math.floor(accumulatedGameSeconds + ROUNDING_TOLERANCE);
        accumulatedGameSeconds -= elapsedSeconds;
        return elapsedSeconds;
    }

    private void publishChanges(long previousMinute, long currentMinute) {
        var elapsed = new ElapsedMinutes(previousMinute, currentMinute);
        GameDateTime dateTime = GameCalendar.fromEpochMinute(currentMinute);

        publishIf(elapsed.crossed(GameCalendar::totalHours), HOUR_CHANGED, dateTime);
        publishIf(elapsed.crossed(GameCalendar::totalDays), DAY_CHANGED, dateTime);
        publishIf(elapsed.crossed(GameCalendar::totalWeeks), WEEK_CHANGED, dateTime);
        publishIf(elapsed.crossed(GameCalendar::totalMonths), MONTH_CHANGED, dateTime);
        publishIf(elapsed.crossed(GameCalendar::totalYears), YEAR_CHANGED, dateTime);
    }

    private void publishIf(boolean condition, TemporalChange change, GameDateTime dateTime) {
        if (condition) {
            eventBus.publish(new TemporalEvent(change, dateTime));
        }
    }

    private long currentEpochMinute() {
        return clock().epochSeconds() / GameCalendar.SECONDS_PER_MINUTE;
    }

    private GameClock clock() {
        return Objects.requireNonNull(clock, "Temporal module is not initialized");
    }

    private static void validateDelta(double delta) {
        if (!Double.isFinite(delta) || delta < 0) {
            throw new IllegalArgumentException("Invalid delta");
        }
    }

    private record ElapsedMinutes(long previous, long current) {

        boolean crossed(LongUnaryOperator period) {
            return period.applyAsLong(previous) != period.applyAsLong(current);
        }
    }
}
