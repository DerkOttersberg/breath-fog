package io.github.derkottersberg.breathfog.config;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettingsStoreTest {
    @TempDir Path directory;
    @Test void roundTripPersistsChoicesAndCreatesMissingDirectory() throws Exception {
        SettingsStore store = new SettingsStore(directory.resolve("nested"), fail -> fail(fail));
        FogSettings settings = store.load(); settings.nearbyPlayers = false; settings.intensity = 1.3;
        store.save(settings);
        assertFalse(store.load().nearbyPlayers); assertEquals(1.3, store.load().intensity);
    }
    @Test void corruptFileIsPreservedAndDefaultsAreRecovered() throws Exception {
        Path file = directory.resolve("breath_fog.json"); Files.writeString(file, "{broken json");
        ArrayList<String> warnings = new ArrayList<>();
        assertTrue(new SettingsStore(directory, warnings::add).load().enabled);
        assertEquals("{broken json", Files.readString(file)); assertEquals(1, warnings.size());
    }
    @Test void partialFutureConfigKeepsDefaultsAndClampsInvalidNumbers() throws Exception {
        Files.writeString(directory.resolve("breath_fog.json"), "{\"enabled\":false,\"intensity\":900,\"futureOption\":true}");
        FogSettings settings = new SettingsStore(directory, fail -> fail(fail)).load();
        assertFalse(settings.enabled); assertTrue(settings.firstPerson); assertEquals(2, settings.intensity);
        settings.intensity = Double.NaN; settings.sanitize(); assertEquals(1, settings.intensity);
    }
}
