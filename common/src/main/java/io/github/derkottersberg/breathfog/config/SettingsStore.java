package io.github.derkottersberg.breathfog.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

/** Atomic configuration writes and a non-destructive recovery path for malformed user files. */
public final class SettingsStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    private final Consumer<String> warning;
    public SettingsStore(Path directory, Consumer<String> warning) {
        this.path = directory.resolve("breath_fog.json"); this.warning = warning;
    }
    public FogSettings load() {
        if (!Files.exists(path)) {
            FogSettings defaults = new FogSettings();
            try { save(defaults); } catch (IOException e) { warning.accept("Could not create Breath Fog configuration: " + e.getMessage()); }
            return defaults;
        }
        try {
            FogSettings settings = GSON.fromJson(Files.readString(path), FogSettings.class);
            if (settings == null) throw new JsonParseException("Expected a settings object");
            settings.sanitize();
            return settings;
        } catch (IOException | JsonParseException | IllegalStateException e) {
            warning.accept("Breath Fog is using defaults; the original configuration was preserved: " + e.getMessage());
            return new FogSettings();
        }
    }
    public void save(FogSettings settings) throws IOException {
        FogSettings checked = settings.copy(); checked.sanitize();
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "breath_fog-", ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(checked) + System.lineSeparator(), StandardCharsets.UTF_8);
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
