package io.github._4drian3d.unsignedvelocity;

import com.google.common.collect.Lists;
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
import io.github._4drian3d.unsignedvelocity.commands.UnSignedVelocityCommand;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.configuration.ConfigurationModule;
import io.github._4drian3d.unsignedvelocity.configuration.ConfigurationProvider;
import io.github._4drian3d.unsignedvelocity.listener.ConfigurableListener;
import io.github._4drian3d.unsignedvelocity.listener.event.GameProfileRequestListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ChatHeaderListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ChatSessionListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ClientChatListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ServerChatListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.command.CommandListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.data.ServerDataListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.status.ServerResponseListener;
import io.github._4drian3d.unsignedvelocity.updatechecker.UpdateChecker;
import io.github._4drian3d.unsignedvelocity.utils.Constants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.velocity.Metrics;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

@Plugin(
        id = "unsignedvelocity2",
        name = "UnSignedVelocity2",
        authors = {"4drian3d", "MemencioPerez"},
        version = Constants.VERSION,
        dependencies = { @Dependency(id = "packetevents") }
)
public final class UnSignedVelocity {

    private final ProxyServer server;
    private Injector injector;
    private final Path dataDirectory;
    private final Metrics.Factory factory;
    private final ComponentLogger logger;
    private ConfigurationModule configurationModule;
    private Set<? extends ConfigurableListener> listeners;

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
            forciblyDisableForceKeyAuthentication();
            loadMainFeatures();
            factory.make(this, 24373);
            UnSignedVelocityCommand.register(server.getCommandManager(), this);
            getPluginLoadMessages(true).forEach(logger::info);
            UpdateChecker.checkForUpdates(logger);
        } catch (IOException e) {
            logger.error("Cannot load configuration", e);
        }
    }

    public void loadMainFeatures() throws IOException {
        setupConfigurationModule();
        setupConfigurableListeners();
    }

    private void setupConfigurationModule() throws IOException {
        Configuration configuration = Configuration.loadConfig(dataDirectory);
        if (configurationModule == null) {
            configurationModule = new ConfigurationModule(configuration);
            injector = injector.createChildInjector(configurationModule);
        } else {
            ConfigurationProvider configurationProvider = configurationModule.getConfigurationProvider();
            configurationProvider.updateConfiguration(configuration);
        }
    }

    private void forciblyDisableForceKeyAuthentication() {
        VelocityConfiguration velocityConfiguration = ((VelocityServer) server).getConfiguration();
        if (velocityConfiguration.isForceKeyAuthentication()) {
            logger.warn("The “force-key-authentication” option is enabled in the Velocity configuration file (velocity.toml).");
            logger.warn("This option is incompatible with UnSignedVelocity, so the plugin will disable it.");
            logger.warn("To avoid this warning, set the “force-key-authentication” option to “false” in the Velocity configuration and restart the proxy.");
            System.setProperty("auth.forceSecureProfiles", "false");
        }
    }

    private void setupConfigurableListeners() {
        if (listeners != null && !listeners.isEmpty()) {
            listeners.forEach(ConfigurableListener::unregister);
        }

        Set<? extends ConfigurableListener> loadableListeners = Stream.of(
                        GameProfileRequestListener.class,
                        CommandListener.class,
                        ClientChatListener.class,
                        ServerChatListener.class,
                        ChatHeaderListener.class,
                        ChatSessionListener.class,
                        ServerDataListener.class,
                        ServerResponseListener.class
                ).map(injector::getInstance)
                .filter(ConfigurableListener::canBeLoaded)
                .collect(Collectors.toSet());

        loadableListeners.forEach(ConfigurableListener::register);
        listeners = loadableListeners;
    }

    public List<Component> getPluginLoadMessages(boolean firstLoad) {
        List<Component> messages = getPluginStatusMessages();
        messages.add(0, miniMessage().deserialize(
                "<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity2</gradient> <#6892bd>has been successfully " + (firstLoad ? "loaded" : "reloaded")));
        return messages;
    }

    public List<Component> getPluginStatusMessages() {
        Configuration configuration = configurationModule.getConfigurationProvider().get();
        return Lists.newArrayList(
                miniMessage().deserialize(
                        "<#6892bd>Remove Signed Key: <aqua>" + configuration.removeSignedKeyOnJoin()),
                miniMessage().deserialize(
                        "<#6892bd>UnSigned <dark_gray>|</dark_gray> Commands: <aqua>" + configuration.removeSignedCommandInformation() + "</aqua> <dark_gray>|</dark_gray> Chat: <aqua>" + configuration.applyChatMessages()),
                miniMessage().deserialize(
                        "<#6892bd>Convert Player Chat Messages to System Chat Messages: <aqua>" + configuration.convertPlayerChatToSystemChat()),
                miniMessage().deserialize(
                        "<#6892bd>Block <dark_gray>|</dark_gray> <#6892bd>Chat Header Packets: <aqua>" + configuration.blockChatHeaderPackets() + "</aqua> <dark_gray>|</dark_gray> <#6892bd>Chat Session Packets: <aqua>" + configuration.blockChatSessionPackets() + "</aqua>"),
                miniMessage().deserialize(
                        "<#6892bd>Secure Chat Data: <aqua>" + configuration.sendSecureChatData() + " <dark_gray>|</dark_gray> <#6892bd>Safe Server Status: <aqua>" + configuration.sendSafeServerStatus())
        );
    }
}