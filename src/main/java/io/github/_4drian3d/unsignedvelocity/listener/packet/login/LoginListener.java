package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.crypto.MinecraftEncryptionUtil;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.listener.LoadablePacketListener;
import io.github._4drian3d.unsignedvelocity.utils.ClientVersionUtil;

import java.security.PublicKey;
import java.util.WeakHashMap;

public final class LoginListener extends PacketListenerAbstract implements LoadablePacketListener {
    private final UnSignedVelocity plugin;
    public static final WeakHashMap<User, byte[]> SERVER_ENCRYPTED_VERIFY_TOKENS_CACHE = new WeakHashMap<>();

    @Inject
    public LoginListener(UnSignedVelocity plugin) {
        super(PacketListenerPriority.LOWEST);
        this.plugin = plugin;
    }

    @Override
    public boolean canBeLoaded() {
        return plugin.getConfiguration().removeSignedKey();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final User user = event.getUser();
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Login.Client.LOGIN_START) {
            if (ClientVersionUtil.doesNotEnforceSignedChatOnLogin(user)) {
                return;
            }

            WrapperLoginClientLoginStart packet = new WrapperLoginClientLoginStart(event);

            if (packet.getSignatureData().isEmpty()) {
                return;
            }
            packet.setSignatureData(null);
        } else if (packetType == PacketType.Login.Client.ENCRYPTION_RESPONSE) {
            if (ClientVersionUtil.doesNotEnforceSignedChatOnLogin(user)) {
                return;
            }

            WrapperLoginClientEncryptionResponse packet = new WrapperLoginClientEncryptionResponse(event);

            if (packet.getSaltSignature().isPresent() && SERVER_ENCRYPTED_VERIFY_TOKENS_CACHE.containsKey(user)) {
                byte[] encryptedVerifyToken = SERVER_ENCRYPTED_VERIFY_TOKENS_CACHE.get(user);

                packet.setSaltSignature(null);
                packet.setEncryptedVerifyToken(encryptedVerifyToken);
            }
            SERVER_ENCRYPTED_VERIFY_TOKENS_CACHE.remove(user);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        final User user = event.getUser();
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Login.Server.ENCRYPTION_REQUEST) {
            if (ClientVersionUtil.doesNotEnforceSignedChatOnLogin(user)) {
                return;
            }
            WrapperLoginServerEncryptionRequest packet = new WrapperLoginServerEncryptionRequest(event);
            PublicKey serverPublicKey = packet.getPublicKey();
            byte[] serverVerifyToken = packet.getVerifyToken();
            byte[] encryptedVerifyToken = MinecraftEncryptionUtil.encryptRSA(serverPublicKey, serverVerifyToken);
            SERVER_ENCRYPTED_VERIFY_TOKENS_CACHE.put(user, encryptedVerifyToken);
        }
    }
}
