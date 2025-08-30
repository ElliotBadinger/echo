package com.siya.epistemophile;

import android.content.res.Resources;

/**
 * Utility class for formatting time durations in various string representations.
 * This class provides methods to format time in natural language and short timer format.
 */
public final class TimeFormat {
    private static final int SECONDS_PER_MINUTE = 60;

    // Prevent instantiation
    private TimeFormat() {}

    /**
     * Formats the given duration in seconds into a natural language string using Android resources.
     *
     * @param resources The Android Resources object to access strings and plurals.
     * @param totalSeconds The total duration in seconds (will be floored to integer for calculation).
     * @return A Result containing the formatted text and the main count (minutes or seconds).
     * @throws IllegalArgumentException if totalSeconds is negative.
     */
    public static Result naturalLanguage(Resources resources, float totalSeconds) {
        if (totalSeconds < 0) {
            throw new IllegalArgumentException("Total seconds cannot be negative");
        }

        return formatNaturalLanguage(resources, (int) Math.floor(totalSeconds));
    }

    private static Result formatNaturalLanguage(Resources resources, int totalSeconds) {
        int minutes = totalSeconds / SECONDS_PER_MINUTE;
        int seconds = totalSeconds % SECONDS_PER_MINUTE;

        StringBuilder sb = new StringBuilder();
        int count;

        if (minutes > 0) {
            count = minutes;
            sb.append(resources.getQuantityString(com.siya.epistemophile.R.plurals.minute, minutes, minutes));

            if (seconds > 0) {
                sb.append(resources.getString(com.siya.epistemophile.R.string.minute_second_join));
                sb.append(resources.getQuantityString(com.siya.epistemophile.R.plurals.second, seconds, seconds));
            }
        } else {
            count = seconds;
            sb.append(resources.getQuantityString(com.siya.epistemophile.R.plurals.second, seconds, seconds));
        }

        sb.append('.');
        return new Result(sb.toString(), count);
    }

    /**
     * Formats the given duration in seconds into a short timer string (e.g., "1:23").
     *
     * @param totalSeconds The total duration in seconds.
     * @return A formatted string in minutes:seconds format.
     * @throws IllegalArgumentException if totalSeconds is negative.
     */
    public static String shortTimer(float totalSeconds) {
        if (totalSeconds < 0) {
            throw new IllegalArgumentException("Total seconds cannot be negative");
        }

        int totalSecondsInt = (int) Math.floor(totalSeconds);
        int minutes = totalSecondsInt / SECONDS_PER_MINUTE;
        int seconds = totalSecondsInt % SECONDS_PER_MINUTE;

        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Encapsulates the result of natural language time formatting.
     */
    public static final class Result {
        private final String text;
        private final int count;

        public Result(String text, int count) {
            this.text = text;
            this.count = count;
        }

        public String getText() {
            return text;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return "Result{text='" + text + "', count=" + count + '}';
        }
    }
}
