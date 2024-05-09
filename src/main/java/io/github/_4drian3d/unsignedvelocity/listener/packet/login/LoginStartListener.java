package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;

import java.util.HashSet;

public final class LoginStartListener implements EventListener, PacketListener {
    @Inject
    private Configuration configuration;
    @Inject
    private UnSignedVelocity plugin;

    @Override
    public void register() {
        PacketEvents.getAPI()
                .getEventManager()
                .registerListener(this, PacketListenerPriority.LOWEST);
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.removeSignedKey();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // If the packet isn't a Login Start packet, do nothing
        if (event.getPacketType() != PacketType.Login.Client.LOGIN_START) {
            return;
        }

        // If the user client version isn't V_1_19 (1.19) or V_1_19_1 (1.19.1/2), do nothing
        final User user = event.getUser();
        if (!userClientVersionEnforcesSigning(user)) {
            return;
        }

        WrapperLoginClientLoginStart packet = new WrapperLoginClientLoginStart(event);

        // If the packet doesn't have Signature Data, do nothing
        if (packet.getSignatureData().isEmpty()) {
            return;
        }

        packet.setSignatureData(null);

    }
}
