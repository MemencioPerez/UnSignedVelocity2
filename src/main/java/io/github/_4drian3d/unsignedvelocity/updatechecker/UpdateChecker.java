package io.github._4drian3d.unsignedvelocity.updatechecker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.utils.Constants;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class UpdateChecker {
    private final ComponentLogger logger;

    @Inject
    public UpdateChecker(ComponentLogger logger) {
        this.logger = logger;
    }

    private static Version getLatestVersion() throws IOException {
        String url = "https://api.github.com/repos/MemencioPerez/UnSignedVelocity2/releases/latest";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return Version.parse(jsonObject.get("tag_name").getAsString());
        }
    }

    public void checkForUpdates() {
        try {
            Version currentVersion = Version.parse(Constants.VERSION);
            Version latestVersion = getLatestVersion();
            if (currentVersion.isLowerThan(latestVersion)) {
                logger.info(miniMessage().deserialize(
                        "<#6892bd>There is an update available for <gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity2</gradient><#6892bd>: " + latestVersion + " (Your version: " + currentVersion + ")"));
            } else if (currentVersion.isHigherThan(latestVersion)) {
                logger.info(miniMessage().deserialize(
                        "<#6892bd>You are using a development build of <gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity2</gradient><#6892bd>: " + currentVersion + " (Latest release: " + latestVersion + ")"));
            } else {
                logger.info(miniMessage().deserialize(
                        "<#6892bd>You are using the latest version of <gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity2</gradient>"));
            }
        } catch (IOException e) {
            logger.error("Cannot check for updates", e);
        }
    }
}
