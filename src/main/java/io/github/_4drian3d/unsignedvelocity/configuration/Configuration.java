package io.github._4drian3d.unsignedvelocity.configuration;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@ConfigSerializable
public record Configuration(
    boolean removeSignedKeyOnJoin,

    boolean removeSignedCommandInformation,

    boolean applyChatMessages,

    boolean convertPlayerChatToSystemChat,

    boolean blockChatHeaderPackets,

    boolean blockChatSessionPackets,

    boolean sendSecureChatData,

    boolean sendSafeServerStatus) {

    public static Configuration loadConfig(final Path path) throws IOException {
        final Path configPath = loadFiles(path);
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .path(configPath)
                .build();

        final CommentedConfigurationNode loaded = loader.load();

        return loaded.get(Configuration.class);
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
