package com.github.mayconr.juoserver.game.temporal;

import com.github.mayconr.juoserver.game.model.event.TemporalEvent.GameDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameCalendarTest {

    @Test
    void epochStartsAtFirstMinuteOfFirstYear() {
        assertEquals(new GameDateTime(1, 1, 1, 1, 0, 0), GameCalendar.fromEpochMinute(0));
    }

    @Test
    void convertsMinutesIntoCalendarFields() {
        long minutes = (long) GameCalendar.DAYS_PER_YEAR * GameCalendar.MINUTES_PER_DAY
                + 2L * GameCalendar.DAYS_PER_MONTH * GameCalendar.MINUTES_PER_DAY
                + 9L * GameCalendar.MINUTES_PER_DAY
                + 13L * GameCalendar.MINUTES_PER_HOUR
                + 45;

        assertEquals(new GameDateTime(2, 3, 2, 10, 13, 45), GameCalendar.fromEpochMinute(minutes));
    }

    @Test
    void lastMinuteOfYearRollsOverToNextYear() {
        var last = new GameDateTime(1, 12, 4, 28, 23, 59);

        assertEquals(new GameDateTime(2, 1, 1, 1, 0, 0), last.plusMinutes(1));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 59, 60, 1439, 1440, 40319, 40320, 483839, 483840, 9_999_999})
    void epochMinuteRoundTrips(long minute) {
        assertEquals(minute, GameCalendar.fromEpochMinute(minute).toEpochMinute());
    }

    @Test
    void epochSecondTruncatesToMinute() {
        assertEquals(GameCalendar.fromEpochMinute(1), GameCalendar.fromEpochSecond(119));
    }

    @Test
    void weekFollowsDayOfMonth() {
        var start = GameCalendar.fromEpochMinute(0);

        assertEquals(1, start.plusDays(6).week());
        assertEquals(2, start.plusDays(7).week());
        assertEquals(4, start.plusDays(27).week());
        assertEquals(1, start.plusDays(28).week());
    }

    @Test
    void periodTotalsAreCountedFromEpoch() {
        long minute = new GameDateTime(2, 2, 2, 8, 5, 0).toEpochMinute();

        assertEquals(1, GameCalendar.totalYears(minute));
        assertEquals(13, GameCalendar.totalMonths(minute));
        assertEquals(53, GameCalendar.totalWeeks(minute));
        assertEquals(371, GameCalendar.totalDays(minute));
        assertEquals(371L * GameCalendar.HOURS_PER_DAY + 5, GameCalendar.totalHours(minute));
    }

    @Test
    void ordersByEpochMinute() {
        var earlier = new GameDateTime(1, 5, 1, 1, 10, 0);

        assertTrue(earlier.compareTo(earlier.plusMinutes(1)) < 0);
    }

    @Test
    void rejectsDatesBeforeEpoch() {
        assertThrows(IllegalArgumentException.class, () -> GameCalendar.fromEpochMinute(-1));
        assertThrows(IllegalArgumentException.class, () -> GameCalendar.fromEpochMinute(0).plusMinutes(-1));
    }

    @Test
    void rejectsWeekInconsistentWithDay() {
        assertThrows(IllegalArgumentException.class, () -> new GameDateTime(1, 1, 1, 8, 0, 0));
    }
}
