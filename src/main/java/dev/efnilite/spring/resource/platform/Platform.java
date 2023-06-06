package dev.efnilite.spring.resource.platform;

import dev.efnilite.spring.Spring;
import dev.efnilite.spring.resource.io.web.Requester;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to store data to identify a resource online.
 */
public interface Platform {

    /**
     * @param url The main resource url.
     * @return The {@link Platform} instance derived from the url.
     */
    static Platform of(String url) {
        url = url.toLowerCase();

        if (url.contains("spigotmc.org")) {
            return new SpigotPlatform(getSpigotResourceId(url));
        } else if (url.contains("github.com")) {
            Map<String, String> gitHubData = getGitHubData(url);
            return new GitHubPlatform(gitHubData.get("owner"), gitHubData.get("repo"));
        } else if (url.contains("hangar.papermc.io")) {
            Map<String, String> gitHubData = getHangarData(url);
            return new HangarPlatform(gitHubData.get("author"), gitHubData.get("slug"));
        } else {
            Spring.logging().error("URL '%s' uses an unsupported resource platform.".formatted(url));
            return null;
        }
    }

    Pattern SPIGOT_RESOURCE_ID_PATTERN = Pattern.compile("\\.?(\\d+)");

    private static int getSpigotResourceId(String url) {
        Matcher matcher = SPIGOT_RESOURCE_ID_PATTERN.matcher(url);

        String resourceId = "";
        while (matcher.find()) {
            resourceId = matcher.group(matcher.groupCount());
        }

        return Integer.parseInt(resourceId);
    }

    Pattern GITHUB_OWNER_REPO_PATTERN = Pattern.compile("github\\.com/(.+?)/(.*?)(/|$)");

    private static Map<String, String> getGitHubData(String url) {
        Matcher matcher = GITHUB_OWNER_REPO_PATTERN.matcher(url);

        matcher.find();

        return Map.of("owner", matcher.group(1), "repo", matcher.group(2));
    }

    Pattern HANGAR_AUTHOR_SLUG_PATTERN = Pattern.compile("hangar\\.papermc\\.io/(.+?)/(.*?)(/|$)");

    private static Map<String, String> getHangarData(String url) {
        Matcher matcher = HANGAR_AUTHOR_SLUG_PATTERN.matcher(url);

        matcher.find();

        return Map.of("author", matcher.group(1), "slug", matcher.group(2));
    }

    @Override
    String toString();

    /**
     * @return Returns a requester for this specific type of resource.
     */
    Requester getRequester();

}