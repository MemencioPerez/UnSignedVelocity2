package io.github._4drian3d.unsignedvelocity.listener.packet.data;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

public final class ServerDataListener extends ConfigurablePacketListener {
    @Inject
    public ServerDataListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.sendSecureChatData();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;

        ClientVersion version = event.getUser().getClientVersion();
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.SERVER_DATA
                && version.isNewerThan(ClientVersion.V_1_19)
                && version.isOlderThan(ClientVersion.V_1_20_5)) {
            WrapperPlayServerServerData packet = new WrapperPlayServerServerData(event);
            packet.setEnforceSecureChat(true);
            event.markForReEncode(true);
        } else if (packetType == PacketType.Play.Server.JOIN_GAME && version.isNewerThan(ClientVersion.V_1_20_3)) {
            WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(event);
            packet.setEnforcesSecureChat(true);
            event.markForReEncode(true);
        }
    }
}
