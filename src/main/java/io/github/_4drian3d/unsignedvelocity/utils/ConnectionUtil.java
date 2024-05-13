package io.github._4drian3d.unsignedvelocity.utils;

import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

public final class ConnectionUtil {
    public static boolean hasDisconnected(final ConnectedPlayer player) {
        try {
            player.ensureAndGetCurrentServer().ensureConnected();
            return false;
        } catch(final IllegalStateException e) {
            // The player is probably on a fake server or
            // some other place generated by another plugin,
            // so handling the packet would be avoided
            return true;
        }
    }
}