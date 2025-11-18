package io.github._4drian3d.unsignedvelocity.listener.event;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.ConfigurableListener;

public final class GameProfileRequestListener implements ConfigurableListener {
    private final EventManager eventManager;
    private final UnSignedVelocity plugin;
    private final Configuration configuration;

    @Inject
    public GameProfileRequestListener(EventManager eventManager, UnSignedVelocity plugin, Configuration configuration) {
        this.eventManager = eventManager;
        this.plugin = plugin;
        this.configuration = configuration;
    }

    @Subscribe
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        ((LoginInboundConnection) event.getConnection()).setPlayerKey(null);
    }

    @Override
    public void register() {
        eventManager.register(plugin, this);
    }

    @Override
    public void unregister() {
        eventManager.unregisterListener(plugin, this);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.removeSignedKeyOnJoin();
    }
}
