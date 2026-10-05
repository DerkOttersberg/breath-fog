package io.github.derkottersberg.breathfog.core;

public final class CameraComfort {
    private CameraComfort() { }
    public static double smooth(double low, double high, double value) {
        double t = Math.max(0, Math.min(1, (value - low) / (high - low)));
        return t * t * (3 - 2 * t);
    }
    public static double attenuation(double distance, double forwardCosine, boolean ownFirstPerson) {
        double nearFade = smooth(0.12, 0.36, distance);
        if (!ownFirstPerson) return nearFade;
        // A soft opening at the crosshair, fading over approximately 6–18 degrees.
        double centralFade = 1 - smooth(Math.cos(Math.toRadians(18)), Math.cos(Math.toRadians(6)), forwardCosine);
        return nearFade * (0.12 + 0.88 * centralFade) * 0.55;
    }
    public static double envelope(double ageFraction) {
        if (ageFraction <= 0 || ageFraction >= 1) return 0;
        return smooth(0, 0.16, ageFraction) * (1 - smooth(0.25, 1, ageFraction));
    }
}
