package io.github._4drian3d.unsignedvelocity.listener.packet.chat;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
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
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ServerChatListener extends PacketListenerAbstract implements LoadablePacketListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ServerChatListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().convertPlayerChatToSystemChat();
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.CHAT_MESSAGE) {
            if (event.getUser().getClientVersion().isOlderThan(ClientVersion.V_1_19)) {
                return;
            }

            Component messageContent = getComponentFromChatPacket(event);
            final WrapperPlayServerSystemChatMessage newPacket = new WrapperPlayServerSystemChatMessage(false, messageContent);
            event.getUser().sendPacketSilently(newPacket);
            event.setCancelled(true);
        }
    }

    private static @Nullable Component getComponentFromChatPacket(PacketSendEvent event) {
        final WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(event);
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
}
