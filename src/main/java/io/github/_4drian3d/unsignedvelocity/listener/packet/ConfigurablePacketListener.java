package io.github._4drian3d.unsignedvelocity.listener.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;

public abstract class ConfigurablePacketListener extends PacketListenerAbstract {
    protected final Configuration configuration;

    public ConfigurablePacketListener(PacketListenerPriority priority, Configuration configuration) {
        super(priority);
        this.configuration = configuration;
    }

    public void register() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }

    public void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
    }

    public abstract boolean canBeLoaded();
}
