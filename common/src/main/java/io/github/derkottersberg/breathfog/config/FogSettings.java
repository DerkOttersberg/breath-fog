package io.github.derkottersberg.breathfog.config;

public final class FogSettings {
    public boolean enabled = true;
    public boolean firstPerson = true;
    public boolean thirdPerson = true;
    public boolean nearbyPlayers = true;
    public boolean pixelated = false;
    public double intensity = 1.0;
    public double firstPersonIntensity = 1.0;
    public FogSettings copy() {
        FogSettings result = new FogSettings();
        result.enabled = enabled; result.firstPerson = firstPerson; result.thirdPerson = thirdPerson;
        result.nearbyPlayers = nearbyPlayers; result.intensity = intensity;
        result.pixelated = pixelated;
        result.firstPersonIntensity = firstPersonIntensity;
        return result;
    }
    public void sanitize() {
        intensity = finiteClamp(intensity, 0.1, 2.0);
        firstPersonIntensity = finiteClamp(firstPersonIntensity, 0.1, 1.5);
    }
    private static double finiteClamp(double value, double low, double high) {
        return Double.isFinite(value) ? Math.max(low, Math.min(high, value)) : 1.0;
    }
}
