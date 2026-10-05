package io.github.derkottersberg.breathfog.core;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class FogPolicyTest {
    @Test void coldPolicyIncludesTaigaAndModdedTagsWithoutIncludingWarmBiomes() {
        assertEquals(0, ColdExposure.target(0.8, false));
        assertEquals(0, ColdExposure.target(0.5, false));
        assertTrue(ColdExposure.target(0.25, false) > 0);
        assertTrue(ColdExposure.target(-0.5, false) > ColdExposure.target(0.25, false));
        assertTrue(ColdExposure.target(0.8, true) > 0);
        assertEquals(0, ColdExposure.target(Double.NaN, true));
        ColdExposure exposure = new ColdExposure();
        for (int i = 0; i < 10; i++) exposure.advance(1);
        assertEquals(1, exposure.intensity(), 1e-9);
        for (int i = 0; i < 10; i++) exposure.advance(0);
        assertEquals(0, exposure.intensity(), 1e-9);
    }
    @Test void idleAndSprintCadencesStayInsideSpecifiedRanges() {
        for (boolean sprint : new boolean[]{false, true}) {
            BreathClock clock = new BreathClock(17);
            int last = -1, breaths = 0;
            for (int tick = 0; tick < 20000; tick++) {
                if (clock.tick(true, sprint) != 0) continue;
                if (last >= 0) assertTrue(tick-last >= (sprint ? 44 : 64) && tick-last <= (sprint ? 64 : 96));
                last = tick; breaths++;
            }
            assertTrue(breaths > 100);
        }
    }
    @Test void playersHaveDifferentInitialPhasesAndSuppressionQueuesNoCatchup() {
        HashSet<Integer> phases = new HashSet<>();
        for (int seed = 0; seed < 24; seed++) {
            BreathClock clock = new BreathClock(seed);
            for (int tick = 0; tick < 100; tick++) if (clock.tick(true, false) == 0) { phases.add(tick); break; }
        }
        assertTrue(phases.size() > 12);
        BreathClock clock = new BreathClock(0); clock.preview();
        for (int i = 0; i < 500; i++) assertEquals(-1, clock.tick(false, false));
        for (int i = 0; i < 11; i++) assertEquals(-1, clock.tick(true, false));
        assertEquals(0, clock.tick(true, false));
    }
    @Test void completeExhalationsAllocateExactDistanceBudget() {
        for (int setting = 0; setting < 3; setting++) for (double distance : new double[]{0, 10, 24}) {
            int count = FogBudget.particles(distance, setting, false), sum = 0;
            for (int tick = 0; tick < 7; tick++) sum += FogBudget.emissionForTick(count, tick);
            assertEquals(count, sum);
        }
        assertEquals(0, FogBudget.particles(33, 0, false));
        assertTrue(FogBudget.particles(4, 2, false) < FogBudget.particles(4, 0, false));
        assertEquals(0, FogBudget.emissionForTick(12, 8));
    }
    @Test void cameraNeverBlindsCrosshairAndFadeEnvelopeHasNoHardEndpoints() {
        assertEquals(0, CameraComfort.attenuation(0.05, 0, false));
        assertEquals(1, CameraComfort.attenuation(2, 1, false));
        assertTrue(CameraComfort.attenuation(1, 1, true) < CameraComfort.attenuation(1, 0.9, true));
        assertEquals(0, CameraComfort.envelope(0)); assertEquals(0, CameraComfort.envelope(1));
        assertTrue(CameraComfort.envelope(0.005) < 0.01);
        assertTrue(CameraComfort.envelope(0.999) < 0.001);
    }
    @Test void analyticFlowIsBoundedSmoothAndApproximatelyDivergenceFree() {
        double[] flow = new double[3], plus = new double[3], minus = new double[3];
        FlowField.curl(0.3, 1.2, -0.4, 50, 0.7, flow);
        for (double value : flow) assertTrue(Double.isFinite(value) && Math.abs(value) <= 0.016);
        double h = 1e-5, div = 0;
        for (int axis=0; axis<3; axis++) {
            FlowField.curl(0.3+(axis==0?h:0),1.2+(axis==1?h:0),-0.4+(axis==2?h:0),50,0.7,plus);
            FlowField.curl(0.3-(axis==0?h:0),1.2-(axis==1?h:0),-0.4-(axis==2?h:0),50,0.7,minus);
            div += (plus[axis]-minus[axis])/(2*h);
        }
        assertEquals(0, div, 1e-6);
    }
    @Test void wakesDisturbNearbyVaporButIgnoreStationaryPlayersAndTeleports() {
        double[] impulse = new double[3];
        FlowField.wake(0.2, 0, 0, 0, 0, 0, 0.12, 0, 0, impulse);
        assertTrue(Math.abs(impulse[0]) + Math.abs(impulse[2]) > 0);
        double[] stationary = new double[3], teleport = new double[3], far = new double[3];
        FlowField.wake(0.2,0,0,0,0,0,0,0,0,stationary);
        FlowField.wake(0.2,0,0,0,0,0,10,0,0,teleport);
        FlowField.wake(5,0,0,0,0,0,0.1,0,0,far);
        assertArrayEquals(new double[3], stationary); assertArrayEquals(new double[3], teleport); assertArrayEquals(new double[3], far);
    }
}
