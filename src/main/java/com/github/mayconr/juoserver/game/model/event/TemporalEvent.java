package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.temporal.GameCalendar;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

import java.util.Objects;

/** Published once for each calendar period crossed by the game clock. */
public record TemporalEvent(
        TemporalChange change,
        GameDateTime dateTime
) implements GameEvent {

    public TemporalEvent {
        Objects.requireNonNull(change, "change is required");
        Objects.requireNonNull(dateTime, "dateTime is required");
    }

    public enum TemporalChange {
        HOUR_CHANGED,
        DAY_CHANGED,
        WEEK_CHANGED,
        MONTH_CHANGED,
        YEAR_CHANGED
    }

    public record GameDateTime(
            int year,
            int month,
            int week,
            int day,
            int hour,
            int minute
    ) implements Comparable<GameDateTime> {

        public GameDateTime {
            requireRange("year", year, 1, Integer.MAX_VALUE);
            requireRange("month", month, 1, GameCalendar.MONTHS_PER_YEAR);
            requireRange("week", week, 1, GameCalendar.WEEKS_PER_MONTH);
            requireRange("day", day, 1, GameCalendar.DAYS_PER_MONTH);
            requireRange("hour", hour, 0, GameCalendar.HOURS_PER_DAY - 1);
            requireRange("minute", minute, 0, GameCalendar.MINUTES_PER_HOUR - 1);

            if (week != (day - 1) / GameCalendar.DAYS_PER_WEEK + 1) {
                throw new IllegalArgumentException("Week does not match day");
            }
        }

        @Override
        public int compareTo(GameDateTime other) {
            return Long.compare(toEpochMinute(), other.toEpochMinute());
        }

        public long toEpochMinute() {
            return GameCalendar.toEpochMinute(this);
        }

        public GameDateTime plusMinutes(long minutes) {
            return GameCalendar.fromEpochMinute(Math.addExact(toEpochMinute(), minutes));
        }

        public GameDateTime plusHours(long hours) {
            return plusMinutes(Math.multiplyExact(hours, GameCalendar.MINUTES_PER_HOUR));
        }

        public GameDateTime plusDays(long days) {
            return plusMinutes(Math.multiplyExact(days, GameCalendar.MINUTES_PER_DAY));
        }

        private static void requireRange(String field, int value, int min, int max) {
            if (value < min || value > max) {
                throw new IllegalArgumentException("Invalid " + field);
            }
        }
    }
}
