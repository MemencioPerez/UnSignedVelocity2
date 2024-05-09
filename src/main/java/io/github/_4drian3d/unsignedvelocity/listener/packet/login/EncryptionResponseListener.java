package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;
import com.google.inject.Inject;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.EventListener;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import javax.crypto.Cipher;
import java.security.PublicKey;

import static io.github._4drian3d.unsignedvelocity.listener.packet.login.EncryptionRequestListener.serverEncryptionDataCache;

public final class EncryptionResponseListener implements EventListener, PacketListener {
    @Inject
    private Configuration configuration;
    @Inject
    private UnSignedVelocity plugin;
    @Inject
    private ComponentLogger logger;

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
        // If the packet isn't an Encryption Response packet, do nothing
        if (event.getPacketType() != PacketType.Login.Client.ENCRYPTION_RESPONSE) {
            return;
        }

        // If the user client version isn't V_1_19 (1.19) or V_1_19_1 (1.19.1/2), do nothing
        final User user = event.getUser();
        if (!userClientVersionEnforcesSigning(user)) {
            return;
        }

        WrapperLoginClientEncryptionResponse packet = new WrapperLoginClientEncryptionResponse(event);

        // If the packet has a Salt Signature and server encryption data cache contains a value for the user, remove it and generate a Verify Token instead
        if (packet.getSaltSignature().isPresent() && serverEncryptionDataCache.containsKey(user)) {
            PublicKey serverPublicKey = serverEncryptionDataCache.get(user).getPublicKey();
            byte[] serverVerifyToken = serverEncryptionDataCache.get(user).getVerifyToken();
            byte[] newEncryptedVerifyToken = encryptVerifyToken(serverPublicKey, serverVerifyToken);

            replaceSaltSignatureWithVerifyToken(packet, newEncryptedVerifyToken);
        }

        // Remove the user from the user encryption data cache
        serverEncryptionDataCache.remove(user);

        // This has to be set to false because the packet re-encoding disconnects the user by some reason
        event.markForReEncode(false);
    }

    private byte[] encryptVerifyToken(PublicKey publicKey, byte[] verifyToken) {
        byte[] encryptedVerifyToken = null;
        try {
            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedVerifyToken = cipher.doFinal(verifyToken);
        } catch (Exception e) {
            logger.error("An error occurred while trying to encrypt the Verify Token for ENCRYPTION_RESPONSE packet.", e);
        }
        return encryptedVerifyToken;
    }

    private void replaceSaltSignatureWithVerifyToken(WrapperLoginClientEncryptionResponse packet, byte[] token) {
        // This would be easier to do with the setters declared within the PacketWrapper,
        // but I can't use them because the packet re-encoding disconnects the user
        byte[] encryptedSharedSecret = packet.getEncryptedSharedSecret();
        Object buf = packet.getBuffer();

        ByteBufHelper.readerIndex(buf, 1);
        ByteBufHelper.writerIndex(buf, 1);

        packet.writeByteArray(encryptedSharedSecret);
        packet.writeBoolean(true);
        packet.writeByteArray(token);
        packet.read();
    }
}
