package io.github._4drian3d.unsignedvelocity.configuration;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public interface Configuration {
    boolean removeSignedKey();

    boolean removeSignedCommandInformation();

    boolean applyChatMessages();

    boolean sendSecureChatData();

    boolean sendSafeServerStatus();

    static Configuration loadConfig(final Path path) throws IOException {
        final Path configPath = loadFiles(path);
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(configPath)
                .build();

        final CommentedConfigurationNode loaded = loader.load();

        final boolean removeSignedKey = loaded.node("remove-signed-key-on-join")
                .getBoolean(false);
        final boolean removeSigneCommandInformation = loaded.node("remove-signed-command-information")
                .getBoolean(false);
        final boolean applyChatMessages = loaded.node("apply-chat-messages")
                .getBoolean(true);
        final boolean sendSecureChatData = loaded.node("send-secure-chat-data")
                .getBoolean(false);
        final boolean sendSafeServerStatus = loaded.node("send-safe-server-status")
                .getBoolean(false);

        return new Configuration() {
            @Override
            public boolean removeSignedKey() {
                return removeSignedKey;
            }

            @Override
            public boolean removeSignedCommandInformation() {
                return removeSigneCommandInformation;
            }

            @Override
            public boolean applyChatMessages() {
                return applyChatMessages;
            }

            @Override
            public boolean sendSecureChatData() {
                return sendSecureChatData;
            }

            @Override
            public boolean sendSafeServerStatus() {
                return sendSafeServerStatus;
            }
        };
    }

    private static Path loadFiles(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }
        final Path configPath = path.resolve("config.conf");
        if (Files.notExists(configPath)) {
            try (var stream = Configuration.class.getClassLoader().getResourceAsStream("config.conf")) {
                Files.copy(Objects.requireNonNull(stream), configPath);
            }
        }
        return configPath;
    }
}
