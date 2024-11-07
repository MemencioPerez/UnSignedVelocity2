package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

public final class ChatSessionListener extends ConfigurablePacketListener {
    @Inject
    public ChatSessionListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.blockChatSessionPackets();
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.CHAT_SESSION_UPDATE) {
            event.setCancelled(true);
        }
    }
}
