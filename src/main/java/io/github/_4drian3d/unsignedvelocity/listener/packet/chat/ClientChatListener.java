package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;

import java.time.Instant;

public final class ClientChatListener extends PacketListenerAbstract implements LoadablePacketListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ClientChatListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().applyChatMessages();
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.CHAT_MESSAGE) {
            final WrapperPlayClientChatMessage packet = new WrapperPlayClientChatMessage(event);
            if (packet.getMessageSignData().isEmpty()) {
                return;
            }
            MessageSignData packetMessageSignData = packet.getMessageSignData().get();
            Instant packetTimestamp = packetMessageSignData.getTimestamp();
            packet.setMessageSignData(new MessageSignData(new SaltSignature(0L, new byte[0]), packetTimestamp));
        }
    }
}