package io.github._4drian3d.unsignedvelocity.listener.packet.command;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.crypto.MessageSignData;
import com.github.retrooper.packetevents.util.crypto.SaltSignature;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatCommand;
import com.google.inject.Inject;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadableEventListener;

import java.time.Instant;

public final class CommandListener extends PacketListenerAbstract implements LoadableEventListener {
    private final UnSignedVelocity plugin;

    @Inject
    public CommandListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public void register(UnSignedVelocity plugin) { PacketEvents.getAPI().getEventManager().registerListener(new CommandListener(plugin)); }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().removeSignedCommandInformation();
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        // If the packet isn't a Server Command Packet, do nothing
        if (event.getPacketType() != PacketType.Play.Client.CHAT_COMMAND) {
            return;
        }
        final ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();
        if (!checkConnection(player)) return;

        final WrapperPlayClientChatCommand packet = new WrapperPlayClientChatCommand(event);
        MessageSignData packetMessageSignData = packet.getMessageSignData();
        Instant packetTimestamp = packetMessageSignData.getTimestamp();
        packet.setMessageSignData(new MessageSignData(new SaltSignature(0L, new byte[0]), packetTimestamp)); // Setting the message salt long to 0L and signature byte array length to zero to disable chat signing
    }
}
