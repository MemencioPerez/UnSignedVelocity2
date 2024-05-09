package io.github._4drian3d.unsignedvelocity.listener.packet.data;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadableEventListener;

public final class ServerDataListener extends PacketListenerAbstract implements LoadableEventListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ServerDataListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public void register(UnSignedVelocity plugin) { PacketEvents.getAPI().getEventManager().registerListener(new ServerDataListener(plugin)); }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().sendSecureChatData();
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.JOIN_GAME) {
            return;
        }
        final WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(event);
        if (!packet.isEnforcesSecureChat()) {
            packet.setEnforcesSecureChat(true);
        }
    }
}
