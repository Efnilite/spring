package dev.efnilite.spring.resource;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public record OutdatedPlugin(@Nullable Plugin plugin, @NotNull Path outdatedFile) {
}
