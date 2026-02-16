package de.bluecolored.bluemap.api;

import java.util.*;
import java.util.function.Consumer;

public abstract class BlueMapAPI {
    public static void onEnable(Consumer<BlueMapAPI> listener) {}
    public static void onDisable(Consumer<BlueMapAPI> listener) {}
    public static Optional<BlueMapAPI> getInstance() { return Optional.empty(); }
    public abstract Collection<BlueMapMap> getMaps();
}
