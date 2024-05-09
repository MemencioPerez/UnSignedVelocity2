package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadableEventListener;
import io.github._4drian3d.unsignedvelocity.utils.ClientVersionUtil;

import java.security.PublicKey;
import java.util.WeakHashMap;

public final class EncryptionRequestListener extends PacketListenerAbstract implements LoadableEventListener {
    private final UnSignedVelocity plugin;
    public static final WeakHashMap<User, ServerEncryptionData> serverEncryptionDataCache = new WeakHashMap<>();

    @Inject
    public EncryptionRequestListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public void register(UnSignedVelocity plugin) { PacketEvents.getAPI().getEventManager().registerListener(new EncryptionRequestListener(plugin)); }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().removeSignedKey();
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        // If the packet isn't an Encryption Request packet, do nothing
        if (event.getPacketType() != PacketType.Login.Server.ENCRYPTION_REQUEST) {
            return;
        }

        // If the user client version isn't V_1_19 (1.19) or V_1_19_1 (1.19.1/2), do nothing
        final User user = event.getUser();
        if (ClientVersionUtil.doesNotEnforceSignedChatOnLogin(user)) {
            return;
        }

        WrapperLoginServerEncryptionRequest packet = new WrapperLoginServerEncryptionRequest(event);

        PublicKey serverPacketKey = packet.getPublicKey();
        byte[] serverVerifyToken = packet.getVerifyToken();

        serverEncryptionDataCache.put(user, new ServerEncryptionData(serverPacketKey, serverVerifyToken));
    }

    public record ServerEncryptionData(PublicKey publicKey, byte[] verifyToken) {}
}
