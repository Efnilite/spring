package dev.efnilite.spring.resource.io.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import dev.efnilite.spring.Spring;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Represents a request to the specified url.
 */
public record Request(String url) {

    private static final String USER_AGENT = "spring/%s (%s; %s)"
            .formatted(Spring.getPlugin().getDescription().getVersion(), System.getProperty("os.name"), System.getProperty("java.version"));

    /**
     * @return The input from the request at the link.
     */
    public InputStream getInput() {
        try {
            HttpURLConnection connection = getConnection();

            if (connection == null) {
                return null;
            }

            InputStream inputStream = connection.getInputStream();
            connection.disconnect();
            return inputStream;
        } catch (IOException ex) {
            Spring.logging().stack("Error while trying to send get HTTP request input", ex);
            return null;
        }
    }

    /**
     * @return The connection from the request at the link.
     */
    public HttpURLConnection getConnection() {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", USER_AGENT);

            // invalid resource id
            if (connection.getResponseCode() >= 400) {
                try (InputStream errorStream = connection.getErrorStream(); InputStreamReader reader = new InputStreamReader(errorStream)) {

                    JsonObject object = new JsonStreamParser(reader)
                            .next()
                            .getAsJsonObject();
                    String error = object.has("error") ? object.get("error").getAsString() : object.get("message").getAsString();

                    Spring.logging().stack("Error while trying to request data for resource at url '%s': %s".formatted(url, error),
                            "check the provided url", new IllegalArgumentException(error));
                }
                return null;
            }

            return connection;
        } catch (IOException ex) {
            Spring.logging().stack("Error while trying to send a HTTP request", ex);
            return null;
        }
    }

    /**
     * @return The input stream from the http request as a json object.
     */
    @Nullable
    public JsonObject getAsJson() {
        try (InputStreamReader reader = new InputStreamReader(getInput())) {
            return new JsonStreamParser(reader)
                    .next()
                    .getAsJsonObject();
        } catch (IOException ex) {
            Spring.logging().stack("Error while trying to get HTTP response as JSON object", ex);
            return null;
        }
    }
}