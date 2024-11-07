package io.github._4drian3d.unsignedvelocity.listener.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;

public abstract class ConfigurablePacketListener extends PacketListenerAbstract {
    public ConfigurablePacketListener(PacketListenerPriority priority) {
        super(priority);
    }

    public void register() {
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }

    public void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this);
    }

    public abstract boolean canBeLoaded();
}