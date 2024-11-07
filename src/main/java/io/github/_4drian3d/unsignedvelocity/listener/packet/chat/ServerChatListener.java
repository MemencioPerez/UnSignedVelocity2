package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ServerChatListener extends ConfigurablePacketListener {
    @Inject
    public ServerChatListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    private static @Nullable Component getComponentFromChatPacket(PacketSendEvent event) {
        WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(event);
        ChatMessage chatMessage = packet.getMessage();
        Component messageContent = chatMessage.getChatContent();
        if (chatMessage instanceof ChatMessage_v1_19) {
            messageContent = ((ChatMessage_v1_19) chatMessage).getUnsignedChatContent();
        } else if (chatMessage instanceof ChatMessage_v1_19_1) {
            messageContent = ((ChatMessage_v1_19_1) chatMessage).getUnsignedChatContent();
        } else if (chatMessage instanceof ChatMessage_v1_19_3) {
            Optional<Component> unsignedChatContent = ((ChatMessage_v1_19_3) chatMessage).getUnsignedChatContent();
            if (unsignedChatContent.isPresent()) {
                messageContent = unsignedChatContent.get();
            }
        }
        return messageContent;
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.convertPlayerChatToSystemChat();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        ClientVersion version = event.getUser().getClientVersion();
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.CHAT_MESSAGE) {
            if (version.isNewerThan(ClientVersion.V_1_18_2)) {
                Component messageContent = getComponentFromChatPacket(event);
                WrapperPlayServerSystemChatMessage newPacket = new WrapperPlayServerSystemChatMessage(false, messageContent);
                event.getUser().sendPacketSilently(newPacket);
                event.setCancelled(true);
            }
        }
    }
}
