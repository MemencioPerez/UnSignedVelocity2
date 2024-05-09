package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;

import java.security.PublicKey;
import java.util.WeakHashMap;

public final class EncryptionRequestListener implements EventListener, PacketListener {
    @Inject
    private Configuration configuration;
    @Inject
    private UnSignedVelocity plugin;
    public static WeakHashMap<User, ServerEncryptionData> serverEncryptionDataCache = new WeakHashMap<>();

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
    public void onPacketSend(PacketSendEvent event) {
        // If the packet isn't an Encryption Request packet, do nothing
        if (event.getPacketType() != PacketType.Login.Server.ENCRYPTION_REQUEST) {
            return;
        }

        // If the user client version isn't V_1_19 (1.19) or V_1_19_1 (1.19.1/2), do nothing
        final User user = event.getUser();
        if (!userClientVersionEnforcesSigning(user)) {
            return;
        }

        WrapperLoginServerEncryptionRequest packet = new WrapperLoginServerEncryptionRequest(event);

        PublicKey serverPacketKey = packet.getPublicKey();
        byte[] serverVerifyToken = packet.getVerifyToken();

        serverEncryptionDataCache.put(user, new ServerEncryptionData(serverPacketKey, serverVerifyToken));
    }

    public static final class ServerEncryptionData {
        PublicKey publicKey;
        byte[] verifyToken;

        public ServerEncryptionData(PublicKey publicKey, byte[] verifyToken) {
            this.publicKey = publicKey;
            this.verifyToken = verifyToken;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public byte[] getVerifyToken() {
            return verifyToken;
        }
    }
}
