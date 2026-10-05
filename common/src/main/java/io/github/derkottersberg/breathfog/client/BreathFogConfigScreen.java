package io.github.derkottersberg.breathfog.client;

import io.github.derkottersberg.breathfog.config.FogSettings;
import java.io.IOException;
import java.util.Locale;
import net.minecraft.client.gui.screens.Screen;

public final class BreathFogConfigScreen extends SettingsScreen {
    private FogSettings draft;
    private String intensity, firstIntensity;
    public BreathFogConfigScreen(Screen parent) {
        super(parent,"Breath Fog","Quiet atmosphere. Your view. Your settings.");
        setDraft(BreathFogClient.instance().settings());
    }
    private void setDraft(FogSettings settings) {
        draft=settings;
        intensity=String.format(Locale.ROOT,"%.2f",draft.intensity);
        firstIntensity=String.format(Locale.ROOT,"%.2f",draft.firstPersonIntensity);
    }
    @Override protected void buildSettings() {
        toggleSetting("Visibility","Breath fog","Show visible exhalations in cold biomes.",draft.enabled,v->draft.enabled=v);
        toggleSetting("Visibility","First person","Subtle vapor below your aim, with a clear central view.",draft.firstPerson,v->draft.firstPerson=v);
        toggleSetting("Visibility","Third person","See your breath from either third-person camera.",draft.thirdPerson,v->draft.thirdPerson=v);
        toggleSetting("Visibility","Nearby players","See nearby players' breath. They do not need this mod.",draft.nearbyPlayers,v->draft.nearbyPlayers=v);
        textSetting("Appearance","Vapor intensity","0.10–2.00. Adjust density without increasing particle counts.",intensity,v->intensity=v);
        textSetting("Appearance","First-person strength","0.10–1.50. A separate multiplier for your first-person view.",firstIntensity,v->firstIntensity=v);
        actionSetting("Appearance","Preview breath","Try a local 20-second preview, even in warm biomes.","Preview",button->{
            BreathFogClient.instance().preview(); onClose();
        });
    }
    @Override protected void resetDraft() { setDraft(new FogSettings()); }
    @Override protected void saveDraft() {
        FogSettings result=draft.copy();
        result.intensity=decimal(intensity,0.1,2,"Vapor intensity");
        result.firstPersonIntensity=decimal(firstIntensity,0.1,1.5,"First-person strength");
        try { BreathFogClient.instance().updateSettings(result); }
        catch (IOException e) { throw new IllegalArgumentException("Could not save settings: "+e.getMessage()); }
    }
}
