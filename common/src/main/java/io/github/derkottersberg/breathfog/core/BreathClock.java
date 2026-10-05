package io.github.derkottersberg.breathfog.core;

import java.util.SplittableRandom;

/** One per visible player. Resetting eligibility never queues a burst of missed breaths. */
public final class BreathClock {
    public static final int EXHALE_TICKS = 7;
    private final SplittableRandom random;
    private int remaining, exhaleAge = -1;
    private boolean previousSprint;
    public BreathClock(long seed) {
        random = new SplittableRandom(seed);
        remaining = 8 + random.nextInt(81);
    }
    /** Returns 0..6 during exhalation, -1 otherwise. */
    public int tick(boolean eligible, boolean sprinting) {
        if (!eligible) {
            exhaleAge = -1;
            remaining = Math.max(remaining, 12);
            previousSprint = sprinting;
            return -1;
        }
        if (sprinting && !previousSprint) remaining = Math.min(remaining, 44);
        previousSprint = sprinting;
        if (exhaleAge >= 0) {
            int result = exhaleAge++;
            if (exhaleAge == EXHALE_TICKS) exhaleAge = -1;
            return result;
        }
        if (--remaining > 0) return -1;
        remaining = (sprinting ? random.nextInt(44, 65) : random.nextInt(64, 97)) - (EXHALE_TICKS - 1);
        exhaleAge = 1;
        return 0;
    }
    public void preview() { remaining = 1; exhaleAge = -1; }
}
