package io.github.derkottersberg.breathfog.qa;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import io.github.derkottersberg.breathfog.client.*;
import java.nio.file.*;
import java.util.*;



import net.minecraft.client.*;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;

/** File-driven development instrumentation. No part of this helper is shipped in the mod. */
public final class QaClient {
    public static QaClient active;
    public static java.util.function.Function<net.minecraft.client.gui.screens.Screen, net.minecraft.client.gui.screens.Screen> nativeConfigFactory;
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private Path root;
    private long ticks, lastFrame;
    private String requestId="", shot;
    private int shotMinimum, shotDeadline, peak, frames;
    private int shotReadyAt=-1;
    private long measureUntil;
    private String measurement;
    private final ArrayList<Double> intervals=new ArrayList<>();
    private final ArrayList<RemotePlayer> actors=new ArrayList<>();
    private double actorMotion;
    private String drive="none";
    private String lastError="";
    private boolean inTick;
    private String capturedShot="";
    private int capturedParticles;
    private java.util.concurrent.CompletableFuture<Void> resourceReload;
    private long renderSample;
    private long renderTick;
    public void tickFromRender(Minecraft client) {
        long now=System.nanoTime();
        if (now-renderTick<50_000_000L) return;
        renderTick=now;
        tick(client);
    }
    public void observeRender(Minecraft client) {
        long now=System.nanoTime();
        if (now-renderSample<1_000_000_000L) return;
        renderSample=now;
        JsonObject diagnostic=new JsonObject();
        diagnostic.addProperty("world",client.level!=null);
        diagnostic.addProperty("paused",client.isPaused());
        diagnostic.addProperty("tickCallbacks",ticks);
        diagnostic.addProperty("controllerTicks",BreathFogClient.instance().tickTime());
        diagnostic.addProperty("singletonMatches",client==Minecraft.getInstance());
        diagnostic.addProperty("screen",client.gui.screen()==null ? "none" : client.gui.screen().getClass().getSimpleName());
        if (client.level!=null) {
            diagnostic.addProperty("frozen",client.level.tickRateManager().isFrozen());
            diagnostic.addProperty("rate",client.level.tickRateManager().tickrate());
            diagnostic.addProperty("gameTime",client.level.getGameTime());
        }
        try { Files.writeString(root.resolve("render-status.json"),JSON.toJson(diagnostic)); }
        catch(Exception e) { throw new IllegalStateException(e); }
    }
    public void initialize() {
        initialize(Minecraft.getInstance().gameDirectory.toPath());
    }
    public void initialize(Path directory) {
        active=this;
        root=directory.resolve("qa");
        try { Files.createDirectories(root); } catch (Exception e) { throw new RuntimeException(e); }


    }
    public void tick(Minecraft client) {
        if (inTick) return;
        inTick=true;
        ticks++;
        try {
            if (ticks==1) {
                client.options.pauseOnLostFocus=false;
                client.options.inactivityFpsLimit().set(InactivityFpsLimit.MINIMIZED);
                client.options.renderDistance().set(5);
                client.options.simulationDistance().set(5);
                client.options.enableVsync().set(false);
                client.options.framerateLimit().set(60);
            }
            Path control=root.resolve("control.json");
            if (Files.exists(control)) {
                JsonObject command=JsonParser.parseString(Files.readString(control)).getAsJsonObject();
                String id=command.get("id").getAsString();
                if (!id.equals(requestId)) { requestId=id; lastError=""; apply(client,command); }
            }
            if (client.level!=null && client.player!=null) {
                client.options.keyUp.setDown(!drive.equals("none"));
                client.options.keySprint.setDown(drive.equals("sprint") || drive.equals("swim"));
                client.options.keyShift.setDown(drive.equals("sneak"));
                capture(client);
                for (RemotePlayer actor:actors) {
                    if (actorMotion!=0) actor.setPos(actor.getX()+Math.sin(ticks*.07)*actorMotion,actor.getY(),actor.getZ());
                    actor.setSprinting(actorMotion>.06);
                }
                peak=Math.max(peak,BreathFogClient.instance().liveParticles());
                if (measurement!=null && ticks>=measureUntil) {
                    Collections.sort(intervals);
                    JsonObject result=new JsonObject();
                    result.addProperty("label",measurement); result.addProperty("frames",intervals.size());
                    if (!intervals.isEmpty()) {
                        result.addProperty("medianMs",intervals.get(intervals.size()/2));
                        result.addProperty("p95Ms",intervals.get(Math.min(intervals.size()-1,(int)(intervals.size()*.95))));
                    }
                    result.addProperty("peakBreathParticles",peak);
                    Files.writeString(root.resolve(measurement+".json"),JSON.toJson(result));
                    measurement=null;
                }
            }
            if (ticks%5==0) {
                JsonObject status=new JsonObject(); status.addProperty("id",requestId);
                status.addProperty("ticks",ticks); status.addProperty("world",client.level!=null);
                status.addProperty("controllerTicks",BreathFogClient.instance().tickTime());
                status.addProperty("paused",client.isPaused());
                status.addProperty("reloading",resourceReload!=null && !resourceReload.isDone());
                status.addProperty("particles",BreathFogClient.instance().liveParticles());
                status.addProperty("peak",peak); status.addProperty("error",lastError);
                status.addProperty("screen",client.gui.screen()==null ? "none":client.gui.screen().getClass().getSimpleName());
                status.addProperty("pixelated",BreathFogClient.instance().settings().pixelated);
                status.addProperty("capturedShot",capturedShot);
                status.addProperty("capturedParticles",capturedParticles);
                status.addProperty("camera",client.options.getCameraType().name());
                status.addProperty("fov",client.options.fov().get());
                status.addProperty("throttle",client.getFramerateLimitTracker().getThrottleReason().name());
                var emitters=BreathFogClient.class.getDeclaredField("emitters"); emitters.setAccessible(true);
                status.addProperty("emitters",((Map<?,?>)emitters.get(BreathFogClient.instance())).size());
                status.addProperty("actors",actors.size()); status.addProperty("measuring",measurement);
                if (client.player!=null && client.level!=null) {
                    status.addProperty("gameTime",client.level.getGameTime());
                    status.addProperty("position",client.player.position().toString());
                    status.addProperty("biome",client.level.getBiome(client.player.blockPosition()).unwrapKey().map(k->k.identifier().toString()).orElse("unknown"));
                    status.addProperty("players",client.level.players().size());
                    status.addProperty("name",client.player.getName().getString());
                    status.addProperty("pose",client.player.getPose().name());
                    status.addProperty("sprinting",client.player.isSprinting());
                    status.addProperty("underWater",client.player.isUnderWater());
                    status.addProperty("sleeping",client.player.isSleeping());
                    status.addProperty("alive",client.player.isAlive());
                    status.addProperty("riding",client.player.isPassenger());
                    var owned=BreathFogClient.class.getDeclaredField("particles"); owned.setAccessible(true);
                    var fade=BreathParticle.class.getDeclaredField("collisionFade"); fade.setAccessible(true);
                    int collided=0;
                    for (Object particle:(List<?>)owned.get(BreathFogClient.instance())) if (fade.getDouble(particle)<0.999) collided++;
                    status.addProperty("collidedParticles",collided);
                }
                Files.writeString(root.resolve("status.json"),JSON.toJson(status));
            }
        } catch (Exception e) { lastError=e.toString(); e.printStackTrace(); }
        finally { inTick=false; }
    }
    private void apply(Minecraft client,JsonObject command) throws Exception {
        client.options.pauseOnLostFocus=false;
        client.options.enableVsync().set(false);
        client.options.framerateLimit().set(260);
        client.options.inactivityFpsLimit().set(InactivityFpsLimit.MINIMIZED);
        if (command.has("camera")) client.options.setCameraType(CameraType.valueOf(command.get("camera").getAsString()));
        if (command.has("fov")) client.options.fov().set(command.get("fov").getAsInt());
        if (command.has("enabled")) {
            var settings=BreathFogClient.instance().settings(); settings.enabled=command.get("enabled").getAsBoolean();
            if (command.has("intensity")) settings.intensity=command.get("intensity").getAsDouble();
            BreathFogClient.instance().updateSettings(settings);
        }
        if (command.has("pixelated")) { var settings=BreathFogClient.instance().settings(); settings.pixelated=command.get("pixelated").getAsBoolean(); BreathFogClient.instance().updateSettings(settings); }
        if (command.has("closeScreen")) client.gui.setScreen(null);
        if (command.has("config")) client.setScreenAndShow(new BreathFogConfigScreen(client.gui.screen()));
        if (command.has("nativeConfig")) {
            if (nativeConfigFactory==null) throw new IllegalStateException("Native config integration is unavailable");
            client.setScreenAndShow(nativeConfigFactory.apply(client.gui.screen()));
        }
        if (command.has("uiText") && client.gui.screen()!=null) {
            for (var child:client.gui.screen().children()) {
                if (child instanceof net.minecraft.client.gui.components.EditBox field) { field.setValue(command.get("uiText").getAsString()); break; }
            }
        }
        if (command.has("guiScale")) { client.options.guiScale().set(command.get("guiScale").getAsInt()); client.resizeGui(); }
        if (command.has("uiClick") && client.gui.screen()!=null) {
            String label=command.get("uiClick").getAsString();
            boolean found=false;
            for (var child:client.gui.screen().children()) {
                if (child instanceof net.minecraft.client.gui.components.Button button && button.getMessage().getString().equals(label)) {
                    button.onPress(new net.minecraft.client.input.MouseButtonInfo(0,0)); found=true; break;
                }
            }
            if (!found) throw new IllegalStateException("UI button not found: "+label);
        }
        if (command.has("clientCommand") && client.player!=null) {
            client.player.connection.sendCommand(command.get("clientCommand").getAsString());
            client.gui.setScreen(null); // Mirror vanilla chat's close-on-submit after dispatch.
        }
        if (command.has("preview")) BreathFogClient.instance().preview();
        if (command.has("cancelPreview")) {
            var field=BreathFogClient.class.getDeclaredField("previewUntil"); field.setAccessible(true);
            field.setLong(BreathFogClient.instance(),BreathFogClient.instance().tickTime());
        }
        if (command.has("drive")) drive=command.get("drive").getAsString();
        if (command.has("respawn") && client.player!=null) client.player.respawn();
        if (command.has("useBlock") && client.player!=null) {
            var coordinates=command.getAsJsonArray("useBlock");
            var position=new net.minecraft.core.BlockPos(coordinates.get(0).getAsInt(),coordinates.get(1).getAsInt(),coordinates.get(2).getAsInt());
            client.gameMode.useItemOn(client.player,net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(position),net.minecraft.core.Direction.UP,position,false));
        }
        if (command.has("wake") && client.getSingleplayerServer()!=null) {
            var server=client.getSingleplayerServer(); var id=client.player.getUUID();
            server.execute(()-> { var player=server.getPlayerList().getPlayer(id); if (player!=null) player.stopSleepInBed(false,true); });
        }
        if (command.has("yaw") && client.player!=null) {
            float yaw=command.get("yaw").getAsFloat(); client.player.setYRot(yaw); client.player.setYHeadRot(yaw);
            client.player.setXRot(command.has("pitch") ? command.get("pitch").getAsFloat():0);
        }
        if (command.has("commands") && client.getSingleplayerServer()!=null) {
            var server=client.getSingleplayerServer(); var commands=command.getAsJsonArray("commands");
            server.execute(()-> { for (JsonElement c:commands) server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),c.getAsString()); });
        }
        if (command.has("actors") && client.level!=null) {
            for (RemotePlayer actor:actors) client.level.removeEntity(actor.getId(),Entity.RemovalReason.DISCARDED);
            actors.clear();
            int count=command.get("actors").getAsInt();
            for (int i=0;i<count;i++) {
                var actor=new RemotePlayer(client.level,new GameProfile(UUID.nameUUIDFromBytes(("breath-fog-qa-"+i).getBytes()),"QA"+i));
                actor.setId(100000+i); actor.setPos(client.player.getX()+(i%4-1.5)*1.8,client.player.getY(),client.player.getZ()+4+(i/4)*2);
                actor.setYRot(180); actor.setYHeadRot(180); client.level.addEntity(actor); actors.add(actor);
            }
        }
        if (command.has("motion")) actorMotion=command.get("motion").getAsDouble();
        if (command.has("reload")) resourceReload=client.reloadResourcePacks();
        if (command.has("connect")) {
            client.disconnectWithSavingScreen();
            String address=command.get("connect").getAsString();
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),client,
                net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address),
                new net.minecraft.client.multiplayer.ServerData("QA",address,net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);
            actors.clear();
        }
        if (command.has("disconnect")) { client.disconnectWithSavingScreen(); actors.clear(); }
        if (command.has("shader")) {
            Class<?> iris=Class.forName("net.irisshaders.iris.Iris");
            Object cfg=iris.getMethod("getIrisConfig").invoke(null);
            String name=command.get("shader").getAsString();
            cfg.getClass().getMethod("setShaderPackName",String.class).invoke(cfg,name);
            cfg.getClass().getMethod("setShadersEnabled",boolean.class).invoke(cfg,!name.equals("off"));
            cfg.getClass().getMethod("save").invoke(cfg);
            iris.getMethod("reload").invoke(null);
        }
        if (command.has("shot")) {
            shot=command.get("shot").getAsString(); shotMinimum=command.has("minimumParticles") ? command.get("minimumParticles").getAsInt():0;
            shotDeadline=(int)ticks+200; shotReadyAt=-1;
        }
        if (command.has("measure")) {
            measurement=command.get("measure").getAsString(); intervals.clear(); peak=0; lastFrame=0;
            measureUntil=ticks+(command.has("seconds") ? command.get("seconds").getAsInt():20)*20;
        }
        if (command.has("stop")) client.stop();
        if (command.has("fps")) client.options.framerateLimit().set(command.get("fps").getAsInt());
    }
    private void render() {
        long now=System.nanoTime();
        if (measurement!=null && lastFrame!=0) intervals.add((now-lastFrame)/1_000_000.0);
        lastFrame=now; frames++;
    }
    private void capture(Minecraft client) {
        if (shot!=null && shotReadyAt<0 && BreathFogClient.instance().liveParticles()>=shotMinimum) shotReadyAt=(int)ticks+5;
        if (shot!=null && ((shotReadyAt>=0 && ticks>=shotReadyAt) || ticks>=shotDeadline)) {
            capturedShot=shot;
            capturedParticles=BreathFogClient.instance().liveParticles();
            Path file=root.resolve(shot+".png"); shot=null;
            Screenshot.takeScreenshot(client.gameRenderer.mainRenderTarget(),image -> {
                try { image.writeToFile(file); } catch (Exception e) { e.printStackTrace(); } finally { image.close(); }
            });
        }
    }
}

