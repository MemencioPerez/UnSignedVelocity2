package io.github._4drian3d.unsignedvelocity.listener.packet.command;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

public final class CommandListener extends ConfigurablePacketListener {
    private static final SaltSignature EMPTY_SALT_SIGNATURE = new SaltSignature(0L, new byte[0]);

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
        if (event.isCancelled() || event.getPacketType() != PacketType.Play.Client.CHAT_COMMAND) return;

        WrapperPlayClientChatCommand packet = new WrapperPlayClientChatCommand(event);
        packet.setMessageSignData(new MessageSignData(EMPTY_SALT_SIGNATURE, packet.getMessageSignData().getTimestamp()));
        event.markForReEncode(true);
    }
}
