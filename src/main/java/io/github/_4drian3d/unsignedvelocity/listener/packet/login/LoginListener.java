package io.github._4drian3d.unsignedvelocity.listener.packet.login;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.VersionComparison;
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

public final class LoginListener extends ConfigurablePacketListener {
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
        final User user = event.getUser();
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Login.Client.LOGIN_START) {
            if (!user.getClientVersion().is(VersionComparison.EQUALS, ClientVersion.V_1_19) && !user.getClientVersion().is(VersionComparison.EQUALS, ClientVersion.V_1_19_1)) {
                return;
            }

            WrapperLoginClientLoginStart packet = new WrapperLoginClientLoginStart(event);

            if (packet.getSignatureData().isEmpty()) {
                return;
            }
            packet.setSignatureData(null);
            event.markForReEncode(true);
        } else if (packetType == PacketType.Login.Client.ENCRYPTION_RESPONSE) {
            if (!user.getClientVersion().is(VersionComparison.EQUALS, ClientVersion.V_1_19) && !user.getClientVersion().is(VersionComparison.EQUALS, ClientVersion.V_1_19_1)) {
                return;
            }

            WrapperLoginClientEncryptionResponse packet = new WrapperLoginClientEncryptionResponse(event);

            boolean cacheContainsUser = cache.getIfPresent(user) != null;
            if (packet.getSaltSignature().isPresent() && cacheContainsUser) {
                byte[] encryptedVerifyToken = cache.getIfPresent(user);

                packet.setSaltSignature(null);
                packet.setEncryptedVerifyToken(encryptedVerifyToken);
            }
            cache.invalidate(user);
            event.markForReEncode(true);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        final User user = event.getUser();
        final PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Login.Server.ENCRYPTION_REQUEST) {
            if (!user.getClientVersion().is(VersionComparison.EQUALS, ClientVersion.V_1_19) && !user.getClientVersion().is(VersionComparison.EQUALS, ClientVersion.V_1_19_1)) {
                return;
            }
            WrapperLoginServerEncryptionRequest packet = new WrapperLoginServerEncryptionRequest(event);
            PublicKey serverPublicKey = packet.getPublicKey();
            byte[] serverVerifyToken = packet.getVerifyToken();
            byte[] encryptedVerifyToken = MinecraftEncryptionUtil.encryptRSA(serverPublicKey, serverVerifyToken);
            cache.put(user, encryptedVerifyToken);
        }
    }
}
