package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

public final class ClientChatListener extends ConfigurablePacketListener {
    private static final SaltSignature EMPTY_SALT_SIGNATURE = new SaltSignature(0L, new byte[0]);

    @Inject
    public ClientChatListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.applyChatMessages();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled() || event.getPacketType() != PacketType.Play.Client.CHAT_MESSAGE) return;

        WrapperPlayClientChatMessage packet = new WrapperPlayClientChatMessage(event);
        packet.getMessageSignData().ifPresent(messageSignData -> {
            packet.setMessageSignData(new MessageSignData(EMPTY_SALT_SIGNATURE, messageSignData.getTimestamp()));
            event.markForReEncode(true);
        });
    }
}
