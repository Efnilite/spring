package dev.efnilite.spring.resource.io.web;

import dev.efnilite.spring.resource.io.file.Downloadable;
import dev.efnilite.spring.resource.io.file.FileType;
import dev.efnilite.spring.resource.platform.HangarPlatform;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

// todo fix
public record HangarRequester(HangarPlatform platform) implements Requester {

    @Override
    @Nullable
    public String getLatestVersion() {
        Request request = new Request("https://hangar.papermc.io/api/v1/projects/%s/%s/latestrelease".formatted(platform.author(), platform.slug()));

        InputStream input = request.getInput();

        if (input == null) {
            return null;
        }

        return new BufferedReader(new InputStreamReader(input))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    @Override
    @Nullable
    public Downloadable getDownload() {
        String latestVersion = getLatestVersion();

        Request request = new Request("https://hangar.papermc.io/api/v1/projects/%s/%s/versions/%s/%s/download".formatted(platform.author(), platform.slug(), latestVersion, "PAPER"));

        return new Downloadable(FileType.JAR, request.getInput());
    }
}
