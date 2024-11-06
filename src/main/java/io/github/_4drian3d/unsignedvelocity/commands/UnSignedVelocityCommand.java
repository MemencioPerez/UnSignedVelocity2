package io.github._4drian3d.unsignedvelocity.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import io.github._4drian3d.unsignedvelocity.UnSignedVelocity;
import net.kyori.adventure.text.Component;

import java.util.List;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

public final class UnSignedVelocityCommand {

    public static BrigadierCommand createBrigadierCommand(final UnSignedVelocity plugin) {
        LiteralCommandNode<CommandSource> literalCommandNode = BrigadierCommand.literalArgumentBuilder("unsignedvelocity")
                .requires(source -> source.hasPermission("unsignedvelocity.admin"))
                .executes(context -> {
                    CommandSource source = context.getSource();

                    Component message = miniMessage().deserialize("<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient>");
                    source.sendMessage(message);
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
                                    if (plugin.setupConfiguration()) {
                                        plugin.setupLoadablePacketListeners();
                                        message = miniMessage().deserialize("<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient> <#6892bd>has been successfully reloaded");
                                    } else {
                                        message = miniMessage().deserialize("<gradient:#166D3B:#7F8C8D:#A29BFE>UnSignedVelocity</gradient> <#6892bd>configuration failed to load, check your configuration file and try again");
                                    }
                                    source.sendMessage(message);
                                    yield Command.SINGLE_SUCCESS;
                                }
                                case "status" -> {
                                    plugin.getPluginStatus(plugin.getConfiguration()).forEach(source::sendMessage);
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
