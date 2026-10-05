package io.github.derkottersberg.breathfog.client;

import io.github.derkottersberg.breathfog.core.CameraComfort;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.ParticleRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;

/** Standard lit translucent vanilla quad. Rendering remains entirely inside the vanilla particle path. */
public final class BreathParticle extends TextureSheetParticle {
    private final BreathFogClient controller;
    private final boolean ownPlayer;
    private final boolean pixelated;
    private final double cold, phase, startSize, endSize;
    private final double[] force=new double[3];
    private double collisionFade=1;
    public BreathParticle(ClientLevel level, Vec3 origin, Vec3 velocity, TextureAtlasSprite sprite,
                          boolean ownPlayer, double cold, BreathFogClient controller) {
        super(level,origin.x,origin.y,origin.z);
        setSprite(sprite);
        this.controller=controller; this.ownPlayer=ownPlayer; this.cold=cold;
        this.pixelated=controller.currentSettings().pixelated;
        this.xd=velocity.x; this.yd=velocity.y; this.zd=velocity.z;
        this.lifetime=18+random.nextInt(9);
        this.hasPhysics=true;
        this.setSize(0.07f,0.07f);
        this.phase=random.nextDouble()*Math.PI*2;
        this.roll=this.oRoll=pixelated ? 0 : (float)(phase);
        this.startSize=0.058+random.nextDouble()*0.020;
        this.endSize=0.27+random.nextDouble()*0.09;
        this.quadSize=(float)startSize;
        this.rCol=0.96f; this.gCol=0.975f; this.bCol=0.98f;
        this.alpha=0;
    }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
    @Override public void tick() {
        xo=x; yo=y; zo=z; oRoll=roll;
        if (++age>=lifetime || !controller.currentSettings().enabled) { remove(); return; }
        controller.flow(x,y,z,phase,force);
        xd=xd*0.945+force[0]*0.19;
        yd=yd*0.955+force[1]*0.16+0.00065;
        zd=zd*0.945+force[2]*0.19;
        double requestedX=xd, requestedY=yd, requestedZ=zd;
        move(requestedX,requestedY,requestedZ);
        if (onGround || Math.abs((x-xo)-requestedX)>0.0001 || Math.abs((y-yo)-requestedY)>0.0001 || Math.abs((z-zo)-requestedZ)>0.0001) {
            collisionFade*=0.70; xd*=0.7; yd*=0.7; zd*=0.7;
        }
        if (!pixelated) roll+=0.009f*(float)Math.sin(phase+age*0.11);
        if (collisionFade<0.025) remove();
    }
    @Override public float getQuadSize(float partialTick) {
        double t=Math.min(1,(age+partialTick)/lifetime);
        double growth=CameraComfort.smooth(0,1,t);
        if (pixelated) growth=Math.floor(growth*8)/8;
        return (float)(startSize+(endSize-startSize)*growth);
    }
    @Override public void render(VertexConsumer state, Camera camera, float partialTick) {
        var config=controller.currentSettings();
        boolean first=ownPlayer && !camera.isDetached();
        if (!config.enabled || (ownPlayer && !(first ? config.firstPerson : config.thirdPerson)) || (!ownPlayer && !config.nearbyPlayers)) return;
        double px=xo+(x-xo)*partialTick-camera.getPosition().x;
        double py=yo+(y-yo)*partialTick-camera.getPosition().y;
        double pz=zo+(z-zo)*partialTick-camera.getPosition().z;
        double distance=Math.sqrt(px*px+py*py+pz*pz);
        var forward=camera.getLookVector();
        double cosine=distance>1e-6 ? (px*forward.x()+py*forward.y()+pz*forward.z())/distance : 1;
        double comfort=CameraComfort.attenuation(distance,cosine,first);
        double envelope=CameraComfort.envelope((age+partialTick)/lifetime);
        // Vanilla's particle shader discards fragments below 0.1 alpha. The lower first-person
        // origin and viewing cone keep this readable density out of the central aiming area.
        alpha=(float)Math.min(0.45, (0.22+0.32*cold)*config.intensity*envelope*comfort*collisionFade*(first ? config.firstPersonIntensity : 1));
        if (alpha>0.001f) super.render(state,camera,partialTick);
    }
}
