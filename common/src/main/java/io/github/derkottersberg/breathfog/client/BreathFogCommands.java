package io.github.derkottersberg.breathfog.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Commands are registered exclusively on the viewing client. */
public final class BreathFogCommands {
    private static final CommandDispatcher<Object> LOCAL = new CommandDispatcher<>();
    static { register(LOCAL); }
    private BreathFogCommands() {}
    public static <S> void register(CommandDispatcher<S> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<S>literal("breathfog")
            .then(LiteralArgumentBuilder.<S>literal("config").executes(context -> {
                BreathFogClient.instance().requestSettingsScreen();
                return 1;
            }))
            .then(LiteralArgumentBuilder.<S>literal("preview").executes(context -> {
                BreathFogClient.instance().preview();
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Breath Fog preview: 20 seconds. Try both camera views."));
                return 1;
            })));
    }
    /** Forge's old command hooks miss the new 26.3 send path; keep our namespace local. */
    public static boolean handleIfOwned(String command) {
        String value=command.strip();
        if (!value.equals("breathfog") && !value.startsWith("breathfog ")) return false;
        try { LOCAL.execute(value, new Object()); }
        catch (com.mojang.brigadier.exceptions.CommandSyntaxException error) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("Usage: /breathfog config or /breathfog preview"));
        }
        return true;
    }
}
