package io.github._4drian3d.unsignedvelocity.listener.packet.status;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;

public final class ServerResponseListener extends PacketListenerAbstract implements LoadablePacketListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ServerResponseListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().sendSafeServerStatus();
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        final User user = event.getUser();
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Status.Server.RESPONSE) {
            if (user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19)) {
                WrapperStatusServerResponse packet = new WrapperStatusServerResponse(event);
                JsonObject component = packet.getComponent();
                component.addProperty("preventsChatReports", true);
                packet.setComponent(component);
            }
        }
    }
}
