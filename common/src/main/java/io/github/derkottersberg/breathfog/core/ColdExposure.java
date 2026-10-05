package io.github.derkottersberg.breathfog.core;

/** Portable cold-biome policy; Minecraft values are supplied by the client adapter. */
public final class ColdExposure {
    private double intensity;
    public static double target(double temperature, boolean taggedCold) {
        if (!Double.isFinite(temperature)) return 0;
        if (temperature >= 0.5 && !taggedCold) return 0;
        return Math.max(0.22, Math.min(1.0, (0.5 - temperature) / 0.7));
    }
    public double advance(double target) {
        // Ten ticks from fully warm to fully cold, with no overshoot.
        intensity += Math.max(-0.1, Math.min(0.1, target - intensity));
        return intensity;
    }
    public double intensity() { return intensity; }
}
