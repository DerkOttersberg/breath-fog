package io.github.derkottersberg.breathfog.client;

import com.mojang.logging.LogUtils;
import io.github.derkottersberg.breathfog.BreathFog;
import io.github.derkottersberg.breathfog.config.FogSettings;
import io.github.derkottersberg.breathfog.config.SettingsStore;
import io.github.derkottersberg.breathfog.core.BreathClock;
import io.github.derkottersberg.breathfog.core.ColdExposure;
import io.github.derkottersberg.breathfog.core.FlowField;
import io.github.derkottersberg.breathfog.core.FogBudget;
import io.github.derkottersberg.breathfog.platform.ClientPlatformServices;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

/** Shared client orchestration. Reads existing world state; never sends packets or mutates entities. */
public final class BreathFogClient {
    private static final TagKey<Biome> COLD = TagKey.create(Registries.BIOME, Identifier.parse("c:is_cold"));
    private static final BreathFogClient INSTANCE = new BreathFogClient();
    private final Map<UUID, Emitter> emitters = new HashMap<>();
    private final ArrayList<AbstractClientPlayer> candidates = new ArrayList<>(24);
    private final HashSet<UUID> selectedIds = new HashSet<>(32);
    private final ArrayList<BreathParticle> particles = new ArrayList<>(FogBudget.LIVE_CAP);
    private final TextureAtlasSprite[] sprites = new TextureAtlasSprite[4];
    private final double[][] wakes = new double[8][6];
    private int wakeCount;
    private long tick, previewUntil;
    private boolean previewRequested;
    private ClientLevel level;
    private ClientPlatformServices platform;
    private SettingsStore store;
    private FogSettings settings = new FogSettings();
    private volatile boolean reloadPending;
    private boolean spritesMissing;
    private BreathFogClient() { }
    public static BreathFogClient instance() { return INSTANCE; }
    public void initialize(ClientPlatformServices services) {
        Objects.requireNonNull(services, "services");
        if (platform != null) throw new IllegalStateException("Breath Fog was initialized twice");
        platform = services;
        store = new SettingsStore(services.configDirectory(), message -> LogUtils.getLogger().warn(message));
        settings = store.load();
        LogUtils.getLogger().info("Breath Fog initialized on {}. Local particles only; no network channels.", services.loaderName());
    }
    public FogSettings settings() { return settings.copy(); }
    FogSettings currentSettings() { return settings; }
    public void updateSettings(FogSettings next) throws IOException {
        FogSettings checked = next.copy(); checked.sanitize();
        store.save(checked); settings = checked;
        if (!settings.enabled) clearParticles();
    }
    public void preview() {
        previewUntil = tick + 400;
        previewRequested=true;
    }
    /** Apply-phase callback; sprites are reacquired only after reload completion. */
    public void resourcesReloaded() { reloadPending = true; }
    public void clear() {
        clearParticles(); emitters.clear(); candidates.clear(); selectedIds.clear(); wakeCount = 0; level = null; previewUntil = 0; previewRequested=false;
        for (int i=0; i<sprites.length; i++) sprites[i] = null;
        spritesMissing=false;
    }
    private void clearParticles() {
        // Remove only particles owned by this mod. Never clear vanilla/other mods' particles.
        for (BreathParticle particle : particles) particle.remove();
        particles.clear();
    }
    public int liveParticles() { return particles.size(); }
    public long tickTime() { return tick; }
    public void tick(Minecraft client) {
        if (client.level != level) { clear(); level = client.level; }
        if (level == null || client.player == null) return;
        if (client.isPaused()) return;
        tick++;
        if (reloadPending) {
            clearParticles();
            for (int i=0; i<sprites.length; i++) sprites[i]=null;
            spritesMissing=false;
            reloadPending=false;
        }
        for (int i=particles.size()-1; i>=0; i--) if (!particles.get(i).isAlive()) particles.remove(i);
        if (!settings.enabled) { wakeCount=0; emitters.clear(); return; }
        candidates.clear();
        // Keep only the nearest 24 while scanning, avoiding a sort of the entire server player list.
        for (AbstractClientPlayer player : level.players()) {
            if (!settings.nearbyPlayers && player != client.player) continue;
            if (player.distanceToSqr(client.player) > FogBudget.RANGE*FogBudget.RANGE) continue;
            int insertion=0;
            double distance=player.distanceToSqr(client.player);
            while (insertion<candidates.size() && candidates.get(insertion).distanceToSqr(client.player)<=distance) insertion++;
            if (insertion>=FogBudget.EMITTER_CAP) continue;
            candidates.add(insertion, player);
            if (candidates.size()>FogBudget.EMITTER_CAP) candidates.removeLast();
        }
        selectedIds.clear();
        for (AbstractClientPlayer player : candidates) selectedIds.add(player.getUUID());
        // Evict before creating new emitters: a changing nearest-player set must never exceed the cap.
        emitters.keySet().removeIf(id -> !selectedIds.contains(id));
        wakeCount=0;
        for (AbstractClientPlayer player : candidates) {
            UUID id=player.getUUID();
            Emitter emitter=emitters.computeIfAbsent(id, key -> new Emitter(key.getMostSignificantBits() ^ key.getLeastSignificantBits()));
            Vec3 eye=player.getEyePosition();
            if (emitter.previousEye != null && wakeCount<wakes.length && !player.isSpectator() && !player.isInvisible()) {
                Vec3 delta=eye.subtract(emitter.previousEye);
                if (delta.lengthSqr()>0.000025 && delta.lengthSqr()<=4) {
                    double[] wake=wakes[wakeCount++];
                    wake[0]=emitter.previousEye.x; wake[1]=emitter.previousEye.y-0.25; wake[2]=emitter.previousEye.z;
                    wake[3]=delta.x; wake[4]=delta.y; wake[5]=delta.z;
                }
            }
            emitter.movement = emitter.previousEye == null ? Vec3.ZERO : eye.subtract(emitter.previousEye);
            if (emitter.movement.lengthSqr()>4) emitter.movement=Vec3.ZERO;
            emitter.previousEye=eye;
            BlockPos pos=BlockPos.containing(eye);
            if (!level.hasChunkAt(pos)) { emitter.clock.tick(false, false); continue; }
            if (tick>=emitter.nextBiomeCheck) {
                var biome=level.getBiome(pos);
                emitter.coldTarget=ColdExposure.target(biome.value().getBaseTemperature(), biome.is(COLD));
                emitter.nextBiomeCheck=tick+10;
            }
            boolean own=player==client.player;
            boolean preview=own && tick<previewUntil;
            if (own && previewRequested) { emitter.clock.preview(); previewRequested=false; }
            double cold=emitter.exposure.advance(preview ? 0.85 : emitter.coldTarget);
            boolean allowed=player.isAlive() && !player.isSleeping() && !player.isSpectator() && !player.isInvisible()
                && !player.isUnderWater() && level.getFluidState(pos).isEmpty() && cold>0.02;
            if (own) allowed &= client.options.getCameraType().isFirstPerson() ? settings.firstPerson : settings.thirdPerson;
            int exhale=emitter.clock.tick(allowed, player.isSprinting());
            if (exhale<0) continue;
            if (exhale==0) emitter.exhaleParticles=FogBudget.particles(Math.sqrt(player.distanceToSqr(client.player)), client.options.particles().get().ordinal(), own);
            int count=FogBudget.emissionForTick(emitter.exhaleParticles, exhale);
            for (int i=0; i<count && particles.size()<FogBudget.LIVE_CAP; i++) emit(client, player, emitter, cold, own, exhale);
        }
    }
    private void emit(Minecraft client, AbstractClientPlayer player, Emitter emitter, double cold, boolean own, int exhale) {
        if (spritesMissing) return;
        if (sprites[0]==null) {
            TextureAtlas atlas=client.getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES);
            for (int i=0; i<sprites.length; i++) {
                sprites[i]=atlas.getSprite(BreathFog.id("particle/wisp_"+i));
                if (sprites[i]==atlas.missingSprite()) {
                    LogUtils.getLogger().warn("Breath Fog vapor sprites are missing; emission is suspended until resource reload.");
                    spritesMissing=true;
                    return;
                }
            }
        }
        boolean first=own && client.options.getCameraType().isFirstPerson();
        Vec3 direction=Vec3.directionFromRotation(player.getXRot(), player.getYHeadRot());
        Vec3 eye=player.getEyePosition();
        Vec3 mouth=eye.add(direction.scale(first ? 0.34 : 0.22));
        if (first) {
            var up=client.gameRenderer.mainCamera().upVector();
            mouth=mouth.add(-up.x()*0.20, -up.y()*0.20, -up.z()*0.20);
        } else mouth=mouth.add(0,-0.13,0);
        if (!level.getFluidState(BlockPos.containing(mouth)).isEmpty()) return;
        var random=level.getRandom();
        double pulse=Math.sin((exhale+1.0)/8*Math.PI);
        double speed=0.043 + 0.017*pulse + random.nextDouble()*0.007;
        Vec3 velocity=direction.scale(speed).add(emitter.movement.scale(0.12));
        // A restrained fan lets the released plume open around the head in rear third person.
        double spread=(random.nextDouble()*2-1)*(first ? 0.006 : 0.025);
        double yaw=Math.toRadians(player.getYHeadRot());
        velocity=velocity.add(Math.cos(yaw)*spread,0,Math.sin(yaw)*spread);
        velocity=velocity.add((random.nextDouble()-0.5)*0.008, -0.002+(random.nextDouble()-0.5)*0.004, (random.nextDouble()-0.5)*0.008);
        mouth=mouth.add((random.nextDouble()-0.5)*0.028,(random.nextDouble()-0.5)*0.015,(random.nextDouble()-0.5)*0.028);
        BreathParticle particle=new BreathParticle(level, mouth, velocity, sprites[random.nextInt(4)], own, cold, this);
        particles.add(particle);
        client.particleEngine.add(particle);
    }
    void flow(double x,double y,double z,double phase,double[] result) {
        FlowField.curl(x,y,z,tick,phase,result);
        for (int i=0; i<wakeCount; i++) {
            double[] w=wakes[i];
            FlowField.wake(x,y,z,w[0],w[1],w[2],w[3],w[4],w[5],result);
        }
        // Multiple crossing players cannot launch a wisp across the scene.
        for (int i=0;i<3;i++) result[i]=Math.max(-0.06,Math.min(0.06,result[i]));
    }
    private static final class Emitter {
        final BreathClock clock;
        final ColdExposure exposure=new ColdExposure();
        Vec3 previousEye, movement=Vec3.ZERO;
        long nextBiomeCheck;
        double coldTarget;
        int exhaleParticles;
        Emitter(long seed) { clock=new BreathClock(seed); }
    }
}
