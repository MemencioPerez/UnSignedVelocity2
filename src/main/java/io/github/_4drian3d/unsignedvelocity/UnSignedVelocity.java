package io.github._4drian3d.unsignedvelocity;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ChatHeaderListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ChatSessionListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ClientChatListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ServerChatListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.command.CommandListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.login.LoginListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.data.ServerDataListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.status.ServerResponseListener;
import io.github._4drian3d.unsignedvelocity.utils.Constants;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.velocity.Metrics;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

@Plugin(
        id = "unsignedvelocity",
        name = "UnSignedVelocity",
        authors = {"4drian3d"},
        version = Constants.VERSION,
        dependencies = { @Dependency(id = "packetevents") }
)
public final class UnSignedVelocity {

    private final ProxyServer server;
    private final Injector injector;
    private final Path dataDirectory;
    private final Metrics.Factory factory;
    private final ComponentLogger logger;
    private Configuration configuration;

    @Inject
    public UnSignedVelocity(ProxyServer server, Injector injector, @DataDirectory Path dataDirectory, Metrics.Factory factory, ComponentLogger logger) {
        this.server = server;
        this.injector = injector;
        this.dataDirectory = dataDirectory;
        this.factory = factory;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        boolean forceKeyAuthentication = ((VelocityServer) server).getConfiguration().isForceKeyAuthentication();
        if (forceKeyAuthentication) {
            logger.error("ERROR: The 'force-key-authentication' option in the Velocity configuration file (velocity.toml) is set to 'true'.");
            logger.error("UnSignedVelocity requires that option to be set to 'false', so the plugin will not load.");
            logger.error("If you want to use UnSignedVelocity, set 'force-key-authentication' to 'false' in Velocity settings and restart the proxy.");
            return;
        }

        factory.make(this, 17514);

        try {
            configuration = Configuration.loadConfig(dataDirectory);
        } catch (IOException e) {
            logger.error("Cannot load configuration", e);
            return;
        }

        Stream.of(
            LoginListener.class,
            CommandListener.class,
            ClientChatListener.class,
            ServerChatListener.class,
            ChatHeaderListener.class,
            ChatSessionListener.class,
            ServerDataListener.class,
            ServerResponseListener.class
        ).map(injector::getInstance)
        .filter(LoadablePacketListener::canBeLoaded)
        .forEach(LoadablePacketListener::register);

        logger.info(miniMessage().deserialize(
                "<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient> <#6892bd>has been successfully loaded"));
        logger.info(miniMessage().deserialize(
                "<#6892bd>Remove Signed Key: <aqua>{}"), configuration.removeSignedKeyOnJoin());
        logger.info(miniMessage().deserialize(
                        "<#6892bd>UnSigned <dark_gray>|</dark_gray> Commands: <aqua>{}</aqua> <dark_gray>|</dark_gray> Chat: <aqua>{}"),
                configuration.removeSignedCommandInformation(),
                configuration.applyChatMessages());
        logger.info(miniMessage().deserialize(
                "<#6892bd>Convert Player Chat Messages to System Chat Messages: <aqua>{}</aqua>"), configuration.convertPlayerChatToSystemChat());
        logger.info(miniMessage().deserialize(
                        "<#6892bd>Block <dark_gray>|</dark_gray> <#6892bd>Chat Header Packets: <aqua>{}</aqua> <dark_gray>|</dark_gray> <#6892bd>Chat Session Packets: <aqua>{}</aqua>"),
                configuration.blockChatHeaderPackets(),
                configuration.blockChatSessionPackets());
        logger.info(miniMessage().deserialize(
                        "<#6892bd>Secure Chat Data: <aqua>{} <dark_gray>|</dark_gray> <#6892bd>Safe Server Status: <aqua>{}"),
                configuration.sendSecureChatData(),
                configuration.sendSafeServerStatus());
    }

    public ProxyServer getServer() {
        return server;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}