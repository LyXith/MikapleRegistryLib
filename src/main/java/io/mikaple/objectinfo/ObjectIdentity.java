package io.mikaple.objectinfo;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

public record ObjectIdentity(String namespace, String name) {
    private static final Pattern NS = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern NAME = Pattern.compile("[a-z0-9/._-]+");
    public ObjectIdentity {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(name);
        if (!NS.matcher(namespace).matches()) throw new IllegalArgumentException();
        if (!NAME.matcher(name).matches()) throw new IllegalArgumentException();
    }

    public ObjectIdentity(String id) {
        if (!id.contains(":")) throw new IllegalArgumentException();
        String[] splits = id.split(":", 2);
        if (splits.length < 2 || splits[1].contains(":")) throw new IllegalArgumentException();
        if (!NS.matcher(splits[0]).matches()) throw new IllegalArgumentException();
        if (!NAME.matcher(splits[1]).matches()) throw new IllegalArgumentException();
        this(splits[0], splits[1]);
    }

    @NotNull
    public String toString() { return namespace + ":" + name; }
}
