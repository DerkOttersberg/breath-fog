package io.github.derkottersberg.breathfog.core;

/** Pure distance and vanilla-particle-setting policy. Never exceeds the hard live cap. */
public final class FogBudget {
    public static final int LIVE_CAP = 256, EMITTER_CAP = 24;
    public static final double RANGE = 32;
    private FogBudget() { }
    public static int particles(double distance, int particleSetting, boolean ownPlayer) {
        if (!Double.isFinite(distance) || distance < 0 || distance > RANGE) return 0;
        int count = distance < 8 ? 12 : distance < 16 ? 6 : 3;
        if (particleSetting == 1) count = Math.max(2, count / 2);
        if (particleSetting >= 2) count = ownPlayer ? 3 : 1;
        return count;
    }
    public static int emissionForTick(int count, int exhaleTick) {
        if (exhaleTick < 0 || exhaleTick >= BreathClock.EXHALE_TICKS) return 0;
        return (count * (exhaleTick + 1) / BreathClock.EXHALE_TICKS) - (count * exhaleTick / BreathClock.EXHALE_TICKS);
    }
}
