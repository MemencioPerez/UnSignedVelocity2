package io.github._4drian3d.unsignedvelocity.listener.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.ConfigurableListener;

public abstract class ConfigurablePacketListener extends PacketListenerAbstract implements ConfigurableListener {
    protected final Configuration configuration;

    public ConfigurablePacketListener(PacketListenerPriority priority, Configuration configuration) {
        super(priority);
        this.configuration = configuration;
    }

    public final void register() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }

    public final void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
    }
}
