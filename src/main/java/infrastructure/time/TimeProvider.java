package infrastructure.time;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Centralized application time source.
 *
 * <p>Default behavior uses the system clock. Tests may replace the clock temporarily
 * to make time-based branches deterministic without changing production logic.
 */
public final class TimeProvider {

    private static volatile Clock clock = Clock.systemDefaultZone();

    private TimeProvider() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public static LocalDate today() {
        return LocalDate.now(clock);
    }

    public static Clock getClock() {
        return clock;
    }

    public static void setClock(Clock newClock) {
        clock = Objects.requireNonNull(newClock, "Clock must not be null.");
    }

    public static void reset() {
        clock = Clock.systemDefaultZone();
    }
}
