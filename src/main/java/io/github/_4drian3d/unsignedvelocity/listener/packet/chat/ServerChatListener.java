package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;
import net.kyori.adventure.text.Component;

public final class ServerChatListener extends ConfigurablePacketListener {
    @Inject
    public ServerChatListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    private static Component getComponentFromChatPacket(PacketSendEvent event) {
        WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(event);
        ChatMessage chatMessage = packet.getMessage();
        if (chatMessage instanceof ChatMessage_v1_19_3 chatMessage_v1_19_3) {
            return chatMessage_v1_19_3.getUnsignedChatContent().orElseThrow();
        } else if (chatMessage instanceof ChatMessage_v1_19_1 chatMessage_v1_19_1) {
            return chatMessage_v1_19_1.getUnsignedChatContent();
        } else if (chatMessage instanceof ChatMessage_v1_19 chatMessage_v1_19) {
            return chatMessage_v1_19.getUnsignedChatContent();
        }
        return chatMessage.getChatContent();
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.convertPlayerChatToSystemChat();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()
                || event.getPacketType() != PacketType.Play.Server.CHAT_MESSAGE
                || !event.getUser().getClientVersion().isNewerThan(ClientVersion.V_1_18_2)) {
            return;
        }

        WrapperPlayServerSystemChatMessage newPacket = new WrapperPlayServerSystemChatMessage(false, getComponentFromChatPacket(event));
        event.getUser().sendPacketSilently(newPacket);
        event.setCancelled(true);
    }
}
