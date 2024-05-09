package io.github._4drian3d.unsignedvelocity.listener.packet.data;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;

public final class ServerDataListener implements EventListener, PacketListener {
    @Inject
    private UnSignedVelocity plugin;
    @Inject
    private Configuration configuration;

    @Override
    public void register() {
        PacketEvents.getAPI()
                .getEventManager()
                .registerListener(this, PacketListenerPriority.NORMAL);
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

    @Override
    public boolean canBeLoaded() {
        return configuration.sendSecureChatData();
    }
}
