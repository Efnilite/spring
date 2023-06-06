package dev.efnilite.spring.resource.io.web;

import com.google.gson.JsonObject;
import dev.efnilite.spring.resource.io.file.Downloadable;
import dev.efnilite.spring.resource.io.file.FileType;
import dev.efnilite.spring.resource.platform.Platform;
import dev.efnilite.spring.resource.platform.SpigotPlatform;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;

public record SpigotRequester(SpigotPlatform platform) implements Requester {

    @Override
    @Nullable
    public String getLatestVersion() {
        Request request = new Request("https://api.spiget.org/v2/resources/%s/versions/latest".formatted(platform.resourceId()));

        JsonObject object = request.getAsJson();
        if (object == null) {
            return null;
        }

        return object.get("name").getAsString();
    }

    @Override
    @Nullable
    public Downloadable getDownload() {
        DownloadInfo info = getDownloadInfo(platform);

        if (info == null) {
            return null;
        }

        if (info.external) {
            return Platform.of(info.externalUrl)
                    .getRequester()
                    .getDownload();
        }

        Request request = new Request("https://api.spiget.org/v2/resources/%s/download".formatted(platform.resourceId()));

        return new Downloadable(FileType.valueOf(info.type.replace(".", "").toUpperCase()), request.getInput());
    }

    @Nullable
    private DownloadInfo getDownloadInfo(SpigotPlatform resource) {
        Request request = new Request("https://api.spiget.org/v2/resources/%s".formatted(resource.resourceId()));

        JsonObject object = request.getAsJson();

        if (object == null) {
            return null;
        }

        JsonObject file = object.get("file").getAsJsonObject();

        return new DownloadInfo(
                object.get("external").getAsBoolean(),
                file.get("type").getAsString(),
                file.get("url").getAsString(),
                file.has("externalUrl") ? file.get("externalUrl").getAsString() : null
        );
    }

    /**
     * Record that stores information about the download of a resource.
     *
     * @param external    Whether the download link is external.
     * @param type        The file type.
     * @param url         The download url.
     * @param externalUrl When external, the link used.
     */
    private record DownloadInfo(boolean external, String type, String url, @Nullable String externalUrl) {
    }
}
