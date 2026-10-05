package io.github.derkottersberg.breathfog.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.derkottersberg.breathfog.client.BreathFogConfigScreen;

public final class BreathFogModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return BreathFogConfigScreen::new; }
}
