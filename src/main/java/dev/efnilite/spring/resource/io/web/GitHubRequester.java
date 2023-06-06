package dev.efnilite.spring.resource.io.web;

import com.google.gson.JsonObject;
import dev.efnilite.spring.Spring;
import dev.efnilite.spring.resource.io.file.Downloadable;
import dev.efnilite.spring.resource.io.file.FileType;
import dev.efnilite.spring.resource.platform.GitHubPlatform;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;

public record GitHubRequester(GitHubPlatform platform) implements Requester {

    @Override
    @Nullable
    public String getLatestVersion() {
        JsonObject object = get();

        return object != null ? object.get("tag_name").getAsString() : null;
    }

    @Override
    @Nullable
    public Downloadable getDownload() {
        JsonObject object = get();

        if (object == null) {
            return null;
        }

        JsonObject assets = object
                .getAsJsonArray("assets")
                .get(0)
                .getAsJsonObject();

        String url = assets.get("browser_download_url").getAsString();
        String[] parts = url.split("\\.");

        try {
            return new Downloadable(FileType.valueOf(parts[parts.length - 1]
//                    .split("/")[1]
                    .replace(".", "")
                    .toUpperCase()), new URL(url).openStream());
        } catch (IOException ex) {
            Spring.logging().stack("Error while trying to get download link of %s/%s".formatted(platform), ex);
            return null;
        }
    }

    @Nullable
    private JsonObject get() {
        Request request = new Request("https://api.github.com/repos/%s/%s/releases/latest".formatted(platform.owner(), platform.repo()));
        return request.getAsJson();
    }
}
