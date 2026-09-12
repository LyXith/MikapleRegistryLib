package io.mikaple;

import java.util.HashMap;
import java.util.Map;

public class Registries {
    private static final Map<String,Registry<?>> registries = new HashMap<>();
    private static boolean isFrozen = false;
    public static boolean isFrozen() {
        return isFrozen;
    }
    public static void setFrozen(boolean freeze) {
        isFrozen = freeze;
    }

    public static boolean addRegistry(Registry<?> registry) {
        if (isFrozen) return false;
        if (registry == null) throw new IllegalArgumentException();
        return registries.putIfAbsent(registry.getType(), registry) == null;
    }

    public static boolean removeRegistry(Registry<?> registry) {
        if (isFrozen) return false;
        return registries.remove(registry.getType(), registry);
    }

    public static Registry<?> get(String type) {
        return registries.get(type);
    }
}
