package io.github._4drian3d.unsignedvelocity.listener.packet.status;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

public class ServerResponseListener extends ConfigurablePacketListener {
    @Inject
    public ServerResponseListener(Configuration configuration) {
        super(PacketListenerPriority.LOWEST, configuration);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.sendSafeServerStatus();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        ClientVersion version = event.getUser().getClientVersion();
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Status.Server.RESPONSE) {
            if (version.isNewerThan(ClientVersion.V_1_18_2)) {
                WrapperStatusServerResponse packet = new WrapperStatusServerResponse(event);
                JsonObject component = packet.getComponent();
                component.addProperty("preventsChatReports", true);
                packet.setComponent(component);
                event.markForReEncode(true);
            }
        }
    }
}
