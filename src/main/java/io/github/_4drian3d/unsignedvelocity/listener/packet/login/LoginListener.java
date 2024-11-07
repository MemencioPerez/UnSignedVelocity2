package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.crypto.MinecraftEncryptionUtil;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerEncryptionRequest;
import com.google.inject.Inject;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import io.github._4drian3d.unsignedvelocity.configuration.Configuration;
import io.github._4drian3d.unsignedvelocity.listener.packet.ConfigurablePacketListener;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

public class LoginListener extends ConfigurablePacketListener {
    private final ProxyServer server;
    private final Cache<User, byte[]> cache;

    @Inject
    public LoginListener(Configuration configuration, ProxyServer server) {
        super(PacketListenerPriority.LOWEST, configuration);
        this.server = server;
        this.cache = setupCache();
    }

    private Cache<User, byte[]> setupCache() {
        ProxyConfig proxyConfig = server.getConfiguration();

        int connectTimeout = proxyConfig.getConnectTimeout();
        int showMaxPlayers = proxyConfig.getShowMaxPlayers();

        return Caffeine.newBuilder()
                .expireAfterWrite(connectTimeout, TimeUnit.MILLISECONDS)
                .maximumSize(showMaxPlayers)
                .build();
    }

    @Override
    public boolean canBeLoaded() {
        return configuration.removeSignedKeyOnJoin();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        User user = event.getUser();
        ClientVersion version = user.getClientVersion();
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Login.Client.LOGIN_START) {
            if (version.isNewerThan(ClientVersion.V_1_18_2) && version.isOlderThan(ClientVersion.V_1_19_3)) {
                WrapperLoginClientLoginStart packet = new WrapperLoginClientLoginStart(event);
                if (packet.getSignatureData().isPresent()) {
                    packet.setSignatureData(null);
                    event.markForReEncode(true);
                }
            }
        } else if (packetType == PacketType.Login.Client.ENCRYPTION_RESPONSE) {
            if (version.isNewerThan(ClientVersion.V_1_18_2) && version.isOlderThan(ClientVersion.V_1_19_3)) {
                WrapperLoginClientEncryptionResponse packet = new WrapperLoginClientEncryptionResponse(event);
                byte[] encryptedVerifyToken = cache.getIfPresent(user);
                if (packet.getSaltSignature().isPresent() && encryptedVerifyToken != null) {
                    packet.setSaltSignature(null);
                    packet.setEncryptedVerifyToken(encryptedVerifyToken);
                }
                cache.invalidate(user);
                event.markForReEncode(true);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        User user = event.getUser();
        ClientVersion version = user.getClientVersion();
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Login.Server.ENCRYPTION_REQUEST) {
            if (version.isNewerThan(ClientVersion.V_1_18_2) && version.isOlderThan(ClientVersion.V_1_19_3)) {
                WrapperLoginServerEncryptionRequest packet = new WrapperLoginServerEncryptionRequest(event);
                PublicKey serverPublicKey = packet.getPublicKey();
                byte[] serverVerifyToken = packet.getVerifyToken();
                byte[] encryptedVerifyToken = MinecraftEncryptionUtil.encryptRSA(serverPublicKey, serverVerifyToken);
                cache.put(user, encryptedVerifyToken);
            }
        }
    }
}
