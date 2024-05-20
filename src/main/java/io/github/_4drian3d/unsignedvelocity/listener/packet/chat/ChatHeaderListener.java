package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;

public final class ChatHeaderListener extends PacketListenerAbstract implements LoadablePacketListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ChatHeaderListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().blockChatHeaderPackets();
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.isCancelled()) return;
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.PLAYER_CHAT_HEADER) {
            event.setCancelled(true);
        }
    }
}
