package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadableEventListener;

public final class LoginStartListener extends PacketListenerAbstract implements LoadableEventListener {
    private final UnSignedVelocity plugin;

    @Inject
    public LoginStartListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public void register(UnSignedVelocity plugin) { PacketEvents.getAPI().getEventManager().registerListener(new LoginStartListener(plugin)); }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().removeSignedKey();
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
