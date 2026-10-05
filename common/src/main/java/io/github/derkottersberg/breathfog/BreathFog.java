package io.github.derkottersberg.breathfog;

import net.minecraft.resources.Identifier;

public final class BreathFog {
    public static final String ID = "breath_fog";
    private BreathFog() { }
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID, path); }
}
