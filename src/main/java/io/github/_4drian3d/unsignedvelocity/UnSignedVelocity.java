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
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ChatHeaderListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ChatSessionListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ClientChatListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.chat.ServerChatListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.command.CommandListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.login.LoginListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.data.ServerDataListener;
import io.github._4drian3d.unsignedvelocity.listener.packet.status.ServerResponseListener;
import io.github._4drian3d.unsignedvelocity.updatechecker.UpdateChecker;
import io.github._4drian3d.unsignedvelocity.utils.Constants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.velocity.Metrics;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
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
    private List<? extends ConfigurablePacketListener> packetListeners;
    private boolean firstLoad = true;

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
            getPluginLoadMessages().forEach(logger::info);
            checkForUpdates();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Failed to access 'force-key-authentication' option in Velocity configuration. Try setting it to 'false' manually and restarting the proxy. If the issue persists, please contact the plugin developer for assistance.", e);
        } catch (IOException e) {
            logger.error("Cannot load configuration", e);
        }
    }

    public void loadMainFeatures() throws IOException {
        setupConfigurationModule();
        setupConfigurablePacketListeners();
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

    private void forciblyDisableForceKeyAuthentication() throws NoSuchFieldException, IllegalAccessException {
        VelocityConfiguration velocityConfiguration = ((VelocityServer) server).getConfiguration();
        if (velocityConfiguration.isForceKeyAuthentication()) {
            logger.warn("Velocity configuration file (velocity.toml) has 'force-key-authentication' enabled, which is incompatible with UnSignedVelocity2.");
            logger.warn("UnSignedVelocity2 will attempt to disable 'force-key-authentication' at runtime. To avoid this warning, set 'force-key-authentication' to 'false' in Velocity settings and restart the proxy.");
            logger.warn("Disabling 'force-key-authentication' at runtime...");
            Field forceKeyAuthenticationField = velocityConfiguration.getClass().getDeclaredField("forceKeyAuthentication");
            forceKeyAuthenticationField.setAccessible(true);
            forceKeyAuthenticationField.setBoolean(velocityConfiguration, false);
            logger.warn("Successfully disabled 'force-key-authentication' at runtime. Note that this change does not persist to velocity.toml.");
        }
    }

    private void setupConfigurablePacketListeners() {
        if (packetListeners != null && !packetListeners.isEmpty()) {
            packetListeners.forEach(ConfigurablePacketListener::unregister);
        }

        List<? extends ConfigurablePacketListener> loadablePacketListeners = Stream.of(
                        LoginListener.class,
                        CommandListener.class,
                        ClientChatListener.class,
                        ServerChatListener.class,
                        ChatHeaderListener.class,
                        ChatSessionListener.class,
                        ServerDataListener.class,
                        ServerResponseListener.class
                ).map(injector::getInstance)
                .filter(ConfigurablePacketListener::canBeLoaded)
                .toList();

        loadablePacketListeners.forEach(ConfigurablePacketListener::register);
        packetListeners = loadablePacketListeners;
    }

    public List<Component> getPluginLoadMessages() {
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

    private void checkForUpdates() {
        injector.getInstance(UpdateChecker.class).checkForUpdates();
    }

    public boolean isFirstLoad() {
        return firstLoad;
    }

    public void setFirstLoad(boolean firstLoad) {
        this.firstLoad = firstLoad;
    }
}