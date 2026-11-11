package com.github.mayconr.juoserver.game.temporal;

import com.github.mayconr.juoserver.game.GamePlaySettings.Temporal;
import com.github.mayconr.juoserver.game.model.event.TemporalEvent.GameDateTime;

/** Single source of truth for game calendar units and conversions. */
public final class GameCalendar {
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int HOURS_PER_DAY = 24;
    public static final int DAYS_PER_WEEK = 7;
    public static final int DAYS_PER_MONTH = 28;
    public static final int MONTHS_PER_YEAR = 12;
    public static final int WEEKS_PER_MONTH = DAYS_PER_MONTH / DAYS_PER_WEEK;

    public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
    public static final int DAYS_PER_YEAR = DAYS_PER_MONTH * MONTHS_PER_YEAR;

    private GameCalendar() {
    }

    /** Converts elapsed real seconds into elapsed game seconds. */
    public static double toGameSeconds(double realSeconds, Temporal settings) {
        return realSeconds * SECONDS_PER_MINUTE / settings.realSecondsPerGameMinute();
    }

    public static long toEpochMinute(GameDateTime dateTime) {
        long days = (long) (dateTime.year() - 1) * DAYS_PER_YEAR
                + (long) (dateTime.month() - 1) * DAYS_PER_MONTH
                + (dateTime.day() - 1);

        return days * MINUTES_PER_DAY
                + (long) dateTime.hour() * MINUTES_PER_HOUR
                + dateTime.minute();
    }

    public static GameDateTime fromEpochMinute(long epochMinute) {
        requireNonNegative(epochMinute);

        long days = totalDays(epochMinute);
        int minuteOfDay = (int) (epochMinute % MINUTES_PER_DAY);
        int day = dayOfMonth(days);

        return new GameDateTime(
                Math.toIntExact(totalYears(epochMinute) + 1),
                (int) (totalMonths(epochMinute) % MONTHS_PER_YEAR + 1),
                weekOfMonth(day),
                day,
                minuteOfDay / MINUTES_PER_HOUR,
                minuteOfDay % MINUTES_PER_HOUR
        );
    }

    public static GameDateTime fromEpochSecond(long epochSecond) {
        requireNonNegative(epochSecond);
        return fromEpochMinute(epochSecond / SECONDS_PER_MINUTE);
    }

    public static long totalHours(long epochMinute) {
        return epochMinute / MINUTES_PER_HOUR;
    }

    public static long totalDays(long epochMinute) {
        return epochMinute / MINUTES_PER_DAY;
    }

    public static long totalWeeks(long epochMinute) {
        return totalDays(epochMinute) / DAYS_PER_WEEK;
    }

    public static long totalMonths(long epochMinute) {
        return totalDays(epochMinute) / DAYS_PER_MONTH;
    }

    public static long totalYears(long epochMinute) {
        return totalDays(epochMinute) / DAYS_PER_YEAR;
    }

    private static int dayOfMonth(long totalDays) {
        return (int) (totalDays % DAYS_PER_MONTH + 1);
    }

    private static int weekOfMonth(int dayOfMonth) {
        return (dayOfMonth - 1) / DAYS_PER_WEEK + 1;
    }

    private static void requireNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Date before epoch");
        }
    }
}
