package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.google.inject.Inject;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;
import io.github._4drian3d.unsignedvelocity.utils.ConnectionUtil;

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
        // If the packet isn't a Server Chat Packet, do nothing
        if (event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) {
            return;
        }

        final ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        if (ConnectionUtil.hasDisconnected(player)) return;

        final WrapperPlayClientChatMessage packet = new WrapperPlayClientChatMessage(event);

        // If the packet doesn't have Message Sign Data, do nothing
        if (packet.getMessageSignData().isEmpty()) {
            return;
        }

        MessageSignData packetMessageSignData = packet.getMessageSignData().get();
        Instant packetTimestamp = packetMessageSignData.getTimestamp();
        packet.setMessageSignData(new MessageSignData(new SaltSignature(0L, new byte[0]), packetTimestamp)); // Setting the message salt long to 0L and signature byte array length to zero to disable chat signing
    }
}
