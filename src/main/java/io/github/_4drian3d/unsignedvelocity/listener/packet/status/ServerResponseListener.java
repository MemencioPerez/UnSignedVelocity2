package io.github._4drian3d.unsignedvelocity.listener.packet.status;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadableEventListener;

public final class ServerResponseListener extends PacketListenerAbstract implements LoadableEventListener {
    private final UnSignedVelocity plugin;

    @Inject
    public ServerResponseListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public void register(UnSignedVelocity plugin) { PacketEvents.getAPI().getEventManager().registerListener(new ServerResponseListener(plugin)); }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().sendSafeServerStatus();
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        final User user = event.getUser();
        if (event.getPacketType() != PacketType.Status.Server.RESPONSE) {
            return;
        }
        if (user.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19)) {
            WrapperStatusServerResponse packet = new WrapperStatusServerResponse(event);
            JsonObject component = packet.getComponent();
            component.addProperty("preventsChatReports", true);
            packet.setComponent(component);
        }
    }
}
