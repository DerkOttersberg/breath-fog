package io.github.derkottersberg.breathfog.platform;

import java.nio.file.Path;

/** The entire loader boundary. Lifecycle registration stays in the loader module. */
public interface ClientPlatformServices {
    String loaderName();
    Path configDirectory();
}
