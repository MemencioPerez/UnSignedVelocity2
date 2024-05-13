package io.github._4drian3d.unsignedvelocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.LoadableEventListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ChatListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.command.CommandListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.login.LoginListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.data.ServerDataListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.status.ServerResponseListener;
import io.github._4drian3d.unsignedvelocity.utils.Constants;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
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
        factory.make(this, 17514);

        try {
            configuration = Configuration.loadConfig(dataDirectory);
        } catch (IOException e) {
            logger.error("Cannot load configuration", e);
            return;
        }

        PluginContainer pluginContainer = server.getPluginManager().ensurePluginContainer(this);
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, pluginContainer, logger, dataDirectory));
        PacketEvents.getAPI().getSettings().checkForUpdates(false)
                .bStats(false);
        PacketEvents.getAPI().load();

        Stream.of(
            LoginListener.class,
            CommandListener.class,
            ChatListener.class,
            ServerDataListener.class,
            ServerResponseListener.class
        ).map(injector::getInstance)
        .filter(LoadableEventListener::canBeLoaded)
        .forEach(listener -> listener.register(this));

        PacketEvents.getAPI().init();

        logger.info(miniMessage().deserialize(
                "<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient> <#6892bd>has been successfully loaded"));
        logger.info(miniMessage().deserialize(
                "<#6892bd>Remove Signed Key: <aqua>{}"), configuration.removeSignedKey());
        logger.info(miniMessage().deserialize(
                        "<#6892bd>UnSigned <dark_gray>|</dark_gray> Commands: <aqua>{}</aqua> <dark_gray>|</dark_gray> Chat: <aqua>{}"),
                configuration.removeSignedCommandInformation(),
                configuration.applyChatMessages());
        logger.info(miniMessage().deserialize(
                "<#6892bd>Secure Chat Data: <aqua>{}"), configuration.sendSecureChatData());
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}