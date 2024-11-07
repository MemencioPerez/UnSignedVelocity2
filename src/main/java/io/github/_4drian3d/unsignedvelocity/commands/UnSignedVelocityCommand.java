package io.github._4drian3d.unsignedvelocity.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import io.github._4drian3d.unsignedvelocity.utils.Constants;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class UnSignedVelocityCommand {

    public static BrigadierCommand createBrigadierCommand(final UnSignedVelocity plugin) {
        LiteralCommandNode<CommandSource> literalCommandNode = BrigadierCommand.literalArgumentBuilder("unsignedvelocity")
                .requires(source -> source.hasPermission("unsignedvelocity.admin"))
                .executes(context -> {
                    CommandSource source = context.getSource();

                    List<Component> messages = List.of(miniMessage().deserialize("<#6892bd>You are using <gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient> <#6892bd>" + Constants.VERSION + " by 4drian3d and MemencioPerez"),
                                                        miniMessage().deserialize("<#6892bd>Available commands:"),
                                                        miniMessage().deserialize("<#6892bd>/unsignedvelocity reload <dark_gray>-</dark_gray> <#6892bd>Reloads the plugin configuration and packet listeners"),
                                                        miniMessage().deserialize("<#6892bd>/unsignedvelocity status <dark_gray>-</dark_gray> <#6892bd>Displays the plugin status"));
                    messages.forEach(source::sendMessage);
                    return Command.SINGLE_SUCCESS;
                })
                .then(BrigadierCommand.requiredArgumentBuilder("subcommand", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            List.of("reload", "status").forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            CommandSource source = context.getSource();
                            String argumentProvided = context.getArgument("subcommand", String.class);

                            return switch (argumentProvided) {
                                case "reload" -> {
                                    Component message;
                                    try {
                                        plugin.setupConfiguration();
                                        plugin.setupConfigurablePacketListeners();
                                        message = miniMessage().deserialize("<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient> <#6892bd>has been successfully reloaded");
                                    } catch (IOException e) {
                                        message = miniMessage().deserialize("<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient> <#6892bd>configuration failed to load, check your configuration file and try again");
                                    }
                                    source.sendMessage(message);
                                    yield Command.SINGLE_SUCCESS;
                                }
                                case "status" -> {
                                    plugin.getPluginStatusMessages().forEach(source::sendMessage);
                                    yield Command.SINGLE_SUCCESS;
                                }
                                default -> BrigadierCommand.FORWARD;
                            };
                        })
                )
                .build();
        return new BrigadierCommand(literalCommandNode);
    }
}
