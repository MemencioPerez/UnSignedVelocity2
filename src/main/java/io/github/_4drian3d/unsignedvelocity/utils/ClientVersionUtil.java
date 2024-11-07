package io.github._4drian3d.unsignedvelocity.utils;

import com.github.retrooper.packetevents.manager.server.VersionComparison;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;

public class ClientVersionUtil {
    public static boolean doesNotEnforceKeyAuthentication(final User user) {
        ClientVersion userClientVersion = user.getClientVersion();
        return ClientVersionUtil.isNotOneOf(userClientVersion, ClientVersion.V_1_19, ClientVersion.V_1_19_1);
    }

    public static boolean isNotOneOf(ClientVersion version, ClientVersion... targetVersions) {
        for (ClientVersion targetVersion : targetVersions) {
            if (version.is(VersionComparison.EQUALS, targetVersion)) {
                return false;
            }
        }
        return true;
    }
}
