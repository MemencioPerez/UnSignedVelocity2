package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;

public final class ChatSessionListener extends PacketListenerAbstract implements LoadablePacketListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ChatSessionListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().blockChatSessionPackets();
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.CHAT_SESSION_UPDATE) {
            event.setCancelled(true);
        }
    }
}
