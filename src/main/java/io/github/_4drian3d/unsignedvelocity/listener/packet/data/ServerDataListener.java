package io.github._4drian3d.unsignedvelocity.listener.packet.data;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerJoinGame;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.ConfigurablePacketListener;

public final class ServerDataListener extends ConfigurablePacketListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ServerDataListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().sendSecureChatData();
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.isCancelled()) return;
        final User user = event.getUser();
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Server.SERVER_DATA) {
            if (user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_1) && user.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_20_3)) {
                final WrapperPlayServerServerData packet = new WrapperPlayServerServerData(event);
                if (!packet.isEnforceSecureChat()) {
                    packet.setEnforceSecureChat(true);
                }
                event.markForReEncode(true);
            }
        } else if (packetType == PacketType.Play.Server.JOIN_GAME) {
            if (user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20_5)) {
                final WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(event);
                if (!packet.isEnforcesSecureChat()) {
                    packet.setEnforcesSecureChat(true);
                }
                event.markForReEncode(true);
            }
        }
    }
}
