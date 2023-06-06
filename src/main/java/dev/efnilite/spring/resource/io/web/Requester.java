package dev.efnilite.spring.resource.io.web;

import dev.efnilite.spring.resource.io.file.Downloadable;
import org.jetbrains.annotations.Nullable;

public interface Requester {

    /**
     * @return The latest version.
     */
    @Nullable
    String getLatestVersion();

    /**
     * @return The latest files.
     */
    @Nullable
    Downloadable getDownload();

}