package io.github._4drian3d.unsignedvelocity.commands;

import com.mojang.brigadier.Command;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.utils.Constants;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class UnSignedVelocityCommand {

    public static void register(final CommandManager commandManager, final UnSignedVelocity plugin) {
        commandManager.register(
                commandManager.metaBuilder("unsignedvelocity").plugin(plugin).build(),
                new BrigadierCommand(BrigadierCommand.literalArgumentBuilder("unsignedvelocity")
                        .requires(source -> source.hasPermission("unsignedvelocity.admin"))
                        .executes(context -> help(context.getSource()))
                        .then(BrigadierCommand.literalArgumentBuilder("reload")
                                .executes(context -> reload(context.getSource(), plugin)))
                        .then(BrigadierCommand.literalArgumentBuilder("status")
                                .executes(context -> status(context.getSource(), plugin)))));
    }

    private static int help(CommandSource source) {
        List<Component> messages = List.of(
                miniMessage().deserialize(
                        "<#6892bd>You are using <gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity2</gradient> <#6892bd>" + Constants.VERSION + " by 4drian3d & MemencioPerez"),
                miniMessage().deserialize(
                        "<#6892bd>Available commands:"),
                miniMessage().deserialize(
                        "<#6892bd>/unsignedvelocity reload <dark_gray>-</dark_gray> <#6892bd>Reloads the plugin configuration and packet listeners"),
                miniMessage().deserialize(
                        "<#6892bd>/unsignedvelocity status <dark_gray>-</dark_gray> <#6892bd>Displays the plugin status"));
        messages.forEach(source::sendMessage);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandSource source, UnSignedVelocity plugin) {
        try {
            if (plugin.isFirstLoad()) {
                plugin.setFirstLoad(false);
            }
            plugin.loadMainFeatures();
            plugin.getPluginLoadMessages().forEach(source::sendMessage);
        } catch (IOException e) {
            source.sendMessage(miniMessage().deserialize(
                    "<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity2</gradient> <#6892bd>configuration failed to load, check your configuration file and try again"));
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int status(CommandSource source, UnSignedVelocity plugin) {
        plugin.getPluginStatusMessages().forEach(source::sendMessage);
        return Command.SINGLE_SUCCESS;
    }
}
