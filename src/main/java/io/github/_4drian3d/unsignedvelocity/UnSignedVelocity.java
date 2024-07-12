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
import com.velocitypowered.proxy.config.VelocityConfiguration;
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
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
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
        try {
            VelocityConfiguration velocityConfiguration = ((VelocityServer) server).getConfiguration();
            Field forceKeyAuthenticationField = velocityConfiguration.getClass().getDeclaredField("forceKeyAuthentication");
            forceKeyAuthenticationField.setAccessible(true);
            boolean forceKeyAuthenticationValue = (boolean) forceKeyAuthenticationField.get(velocityConfiguration);
            if (forceKeyAuthenticationValue) {
                logger.warn("WARN: The 'force-key-authentication' option in the Velocity configuration file (velocity.toml) is set to 'true'.");
                logger.warn("UnSignedVelocity requires that option to be set to 'false', so it will try to set it to 'true' forcefully at runtime.");
                logger.warn("If you want to hide this warning, set 'force-key-authentication' to 'false' in Velocity settings and restart the proxy.");
                logger.warn("Trying to set 'force-key-authentication' to false...");
                forceKeyAuthenticationField.setBoolean(velocityConfiguration, false);
                forceKeyAuthenticationField.setAccessible(false);
                logger.warn("The 'force-key-authentication' field was found and set to false at runtime (so this doesn't modify velocity.toml file.");
            }
        } catch (NoSuchFieldException e) {
            logger.error("The plugin cannot find 'force-key-authentication' option field, 'remove-signed-key-on-join' option will not work. Contact the developer of this plugin.", e);
        } catch (InaccessibleObjectException | SecurityException | IllegalAccessException | IllegalArgumentException | NullPointerException | ExceptionInInitializerError e ) {
            logger.error("The plugin cannot access 'force-key-authentication' option field, 'remove-signed-key-on-join' option will not work. If setting 'force-key-authentication' to 'false' manually and restarting the proxy doesn't work, contact the developer of this plugin.", e);
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