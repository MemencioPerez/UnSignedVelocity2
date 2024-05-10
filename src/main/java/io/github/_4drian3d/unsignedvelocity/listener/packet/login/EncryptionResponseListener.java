package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.crypto.MinecraftEncryptionUtil;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadableEventListener;
import io.github._4drian3d.unsignedvelocity.utils.ClientVersionUtil;

import java.security.PublicKey;

import static io.github._4drian3d.unsignedvelocity.listener.packet.login.EncryptionRequestListener.serverEncryptionDataCache;

public final class EncryptionResponseListener extends PacketListenerAbstract implements LoadableEventListener {
    private final UnSignedVelocity plugin;

    @Inject
    public EncryptionResponseListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public void register(UnSignedVelocity plugin) { PacketEvents.getAPI().getEventManager().registerListener(new EncryptionResponseListener(plugin)); }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().removeSignedKey();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // If the packet isn't an Encryption Response packet, do nothing
        if (event.getPacketType() != PacketType.Login.Client.ENCRYPTION_RESPONSE) {
            return;
        }

        // If the user client version isn't V_1_19 (1.19) or V_1_19_1 (1.19.1/2), do nothing
        final User user = event.getUser();
        if (ClientVersionUtil.doesNotEnforceSignedChatOnLogin(user)) {
            return;
        }

        WrapperLoginClientEncryptionResponse packet = new WrapperLoginClientEncryptionResponse(event);

        // If the packet has a Salt Signature and server encryption data cache contains a value for the user, remove it and generate a Verify Token instead
        if (packet.getSaltSignature().isPresent() && serverEncryptionDataCache.containsKey(user)) {
            PublicKey serverPublicKey = serverEncryptionDataCache.get(user).publicKey();
            byte[] serverVerifyToken = serverEncryptionDataCache.get(user).verifyToken();
            byte[] newEncryptedVerifyToken = MinecraftEncryptionUtil.encryptRSA(serverPublicKey, serverVerifyToken);

            packet.setSaltSignature(null);
            packet.setEncryptedVerifyToken(newEncryptedVerifyToken);
        }

        // Remove the user from the user encryption data cache
        serverEncryptionDataCache.remove(user);
    }
}
