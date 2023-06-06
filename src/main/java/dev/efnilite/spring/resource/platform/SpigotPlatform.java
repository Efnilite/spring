package dev.efnilite.spring.resource.platform;

import dev.efnilite.spring.resource.io.web.Requester;
import dev.efnilite.spring.resource.io.web.SpigotRequester;

public record SpigotPlatform(int resourceId) implements Platform {

    @Override
    public String toString() {
        return "spigotmc.org/%s".formatted(resourceId);
    }

    @Override
    public Requester getRequester() {
        return new SpigotRequester(this);
    }
}
