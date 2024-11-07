package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

public class ChatHeaderListener extends ConfigurablePacketListener {
    @Inject
    public ChatHeaderListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.blockChatHeaderPackets();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.PLAYER_CHAT_HEADER) {
            event.setCancelled(true);
        }
    }
}
