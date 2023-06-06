package dev.efnilite.spring.resource.platform;

import dev.efnilite.spring.resource.io.web.GitHubRequester;
import dev.efnilite.spring.resource.io.web.Requester;

public record GitHubPlatform(String owner, String repo) implements Platform {

    @Override
    public String toString() {
        return "github.com/%s/%s".formatted(owner, repo);
    }

    @Override
    public Requester getRequester() {
        return new GitHubRequester(this);
    }
}
