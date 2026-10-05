package io.github.derkottersberg.breathfog.core;

/** Analytic curl of a smoothly evolving vector potential. No grids or particle-particle solve. */
public final class FlowField {
    private FlowField() { }
    public static void curl(double x, double y, double z, double time, double phase, double[] out) {
        out[0] = out[1] = out[2] = 0;
        for (int octave = 0; octave < 2; octave++) {
            double f = octave == 0 ? 3.1 : 6.7;
            double amplitude = octave == 0 ? 0.006 : 0.002;
            double a = f*x + time*0.038 + phase;
            double b = f*y - time*0.027 + phase*1.7;
            double c = f*z + time*0.033 - phase*0.9;
            // A = (sin(b)*sin(c), sin(c)*sin(a), sin(a)*sin(b)); ∇×A.
            out[0] += amplitude * (Math.sin(a)*Math.cos(b) - Math.cos(c)*Math.sin(a));
            out[1] += amplitude * (Math.sin(b)*Math.cos(c) - Math.cos(a)*Math.sin(b));
            out[2] += amplitude * (Math.sin(c)*Math.cos(a) - Math.cos(b)*Math.sin(c));
        }
    }
    /** Adds a bounded player wake around a swept position segment, in blocks/tick. */
    public static void wake(double x, double y, double z, double px, double py, double pz,
                            double dx, double dy, double dz, double[] out) {
        double speedSq = dx*dx + dy*dy + dz*dz;
        if (speedSq < 0.000025 || speedSq > 4 || !Double.isFinite(speedSq)) return;
        double along = Math.max(0, Math.min(1, ((x-px)*dx + (y-py)*dy + (z-pz)*dz) / speedSq));
        double rx=x-px-along*dx, ry=y-py-along*dy, rz=z-pz-along*dz;
        double radiusSq = rx*rx + ry*ry + rz*rz;
        if (radiusSq >= 0.81) return;
        double falloff = 1-radiusSq/0.81;
        falloff *= falloff;
        double speed = Math.sqrt(speedSq);
        double radial = Math.min(0.025, speed*0.10) * falloff / Math.sqrt(Math.max(0.025, radiusSq));
        out[0] += dx*0.12*falloff + rx*radial - rz*speed*0.035*falloff;
        out[1] += dy*0.10*falloff + ry*radial;
        out[2] += dz*0.12*falloff + rz*radial + rx*speed*0.035*falloff;
    }
}
