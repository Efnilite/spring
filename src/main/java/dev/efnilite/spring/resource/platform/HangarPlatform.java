package dev.efnilite.spring.resource.platform;

import dev.efnilite.spring.resource.io.web.HangarRequester;
import dev.efnilite.spring.resource.io.web.Requester;

public record HangarPlatform(String author, String slug) implements Platform {

    @Override
    public String toString() {
        return "hangar.papermc.io/%s/%s".formatted(author, slug);
    }

    @Override
    public Requester getRequester() {
        return new HangarRequester(this);
    }
}
