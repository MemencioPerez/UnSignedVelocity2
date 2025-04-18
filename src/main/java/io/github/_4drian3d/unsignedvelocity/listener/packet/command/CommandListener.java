package io.github._4drian3d.unsignedvelocity.listener.packet.command;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

import java.time.Instant;

public class CommandListener extends ConfigurablePacketListener {
    @Inject
    public CommandListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.removeSignedCommandInformation();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.CHAT_COMMAND) {
            WrapperPlayClientChatCommand packet = new WrapperPlayClientChatCommand(event);
            MessageSignData packetMessageSignData = packet.getMessageSignData();
            Instant packetTimestamp = packetMessageSignData.getTimestamp();
            packet.setMessageSignData(new MessageSignData(new SaltSignature(0L, new byte[0]), packetTimestamp));
            event.markForReEncode(true);
        }
    }
}
