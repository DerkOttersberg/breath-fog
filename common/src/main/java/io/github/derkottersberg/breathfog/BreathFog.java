package io.github.derkottersberg.breathfog;

import net.minecraft.resources.ResourceLocation;

public final class BreathFog {
    public static final String ID = "breath_fog";
    private BreathFog() { }
    public static ResourceLocation id(String path) { return new ResourceLocation(ID, path); }
}
