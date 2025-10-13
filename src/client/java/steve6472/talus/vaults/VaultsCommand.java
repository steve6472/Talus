package steve6472.talus.vaults;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import steve6472.talus.TalusClient;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class VaultsCommand
{
    private static final Component ALREADY_LOADED = Component.translatable("talus.vaults.command.already_loaded").withStyle(ChatFormatting.RED);
    private static final Component VAULTS_NOT_LOADED = Component.translatable("talus.vaults.command.vaults_not_loaded").withStyle(ChatFormatting.RED);
    private static final Component ERROR_SAVE = Component.translatable("talus.vaults.command.error_save").withStyle(ChatFormatting.RED);
    private static final Component ERROR_LOAD = Component.translatable("talus.vaults.command.error_load").withStyle(ChatFormatting.RED);

    private static final Component SUCCESS_SAVE = Component.translatable("talus.vaults.command.success_save").withStyle(ChatFormatting.WHITE);
//    private static final Component VAULTS_HIGHLIGHTED = Component.translatable("talus.vaults.command.vaults_highlighted").withStyle(ChatFormatting.WHITE);

    private static final Component STATUS_NOT_LOADED = Component.translatable("talus.vaults.command.status.not_loaded").withStyle(ChatFormatting.WHITE);
//  private static final Component STATUS_LOADED = Component.translatable("talus.vaults.command.status.loaded").withStyle(ChatFormatting.WHITE);

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, @SuppressWarnings("unused") CommandBuildContext buildContext)
    {
        dispatcher.register(
            literal("vaults")
                .then(
                    literal("highlight")
                        .executes(c -> {
                            VaultManager vaultManager = TalusClient.instance().vaultManager;

                            if (!vaultManager.areVaultsLoaded())
                            {
                                c.getSource().sendError(TalusClient.header(VAULTS_NOT_LOADED));
                                return 0;
                            }
                            Vec3 position = c.getSource().getPosition();

                            int highlithed = vaultManager.highlightAll(position, VaultManager.INTERACT_RADIUS);
                            c.getSource().sendFeedback(TalusClient.header(Component.translatable("talus.vaults.command.vaults_highlighted", highlithed).withStyle(ChatFormatting.WHITE)));

                            return 1;
                        })
                        .then(
                            literal("clear")
                                .executes(c -> {
                                    // vaults highlight clear
                                    VaultManager vaultManager = TalusClient.instance().vaultManager;

                                    if (!vaultManager.areVaultsLoaded())
                                    {
                                        c.getSource().sendError(TalusClient.header(VAULTS_NOT_LOADED));
                                        return 0;
                                    }

                                    vaultManager.clearHighlights();

                                    return 1;
                                })
                        )
                        .then(
                            literal("locked")
                                .then(
                                    literal("ominious")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, true, true))
                                        )
                                )
                                .then(
                                    literal("normal")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, true, false))
                                        )
                                )
                                .then(
                                    literal("ignore_diff")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, true, null))
                                        )
                                )
                        )
                        .then(
                            literal("unlocked")
                                .then(
                                    literal("ominious")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, false, true))
                                        )
                                )
                                .then(
                                    literal("normal")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, false, false))
                                        )
                                )
                                .then(
                                    literal("ignore_diff")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, false, null))
                                        )
                                )
                        )
                        .then(
                            literal("ignore_lock")
                                .then(
                                    literal("ignore_diff")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, null, null))
                                        )
                                )
                                .then(
                                    literal("ominious")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, null, true))
                                        )
                                )
                                .then(
                                    literal("normal")
                                        .then(
                                            ClientCommandManager.argument("radius", integer(0, 128))
                                                .executes(c -> highlight(c, null, false))
                                        )
                                )
                        )
                )
                .then(
                    literal("save")
                        .executes(c -> {
                            // vaults save
                            VaultManager vaultManager = TalusClient.instance().vaultManager;

                            if (!vaultManager.areVaultsLoaded())
                            {
                                c.getSource().sendError(TalusClient.header(VAULTS_NOT_LOADED));
                                return 0;
                            }

                            if (vaultManager.saveVaults())
                            {
                                c.getSource().sendError(TalusClient.header(SUCCESS_SAVE));
                                return 1;
                            }
                            c.getSource().sendFeedback(TalusClient.header(ERROR_SAVE));

                            return 0;
                        })
                )
                .then(
                    literal("load")
                        .executes(c -> {
                            // vaults load
                            VaultManager vaultManager = TalusClient.instance().vaultManager;

                            if (vaultManager.areVaultsLoaded())
                            {
                                c.getSource().sendError(TalusClient.header(ALREADY_LOADED));
                                return 0;
                            }

                            if (vaultManager.loadVaults())
                            {
                                int i = vaultManager.getVaultCountForCurrentWorld();
                                c.getSource().sendFeedback(TalusClient.header(Component.translatable("talus.vaults.command.status.loaded", i).withStyle(ChatFormatting.WHITE)));
                                return 1;
                            }
                            c.getSource().sendError(TalusClient.header(ERROR_LOAD));

                            return 0;
                        })
                )
                .then(
                    literal("unload")
                        .executes(c -> {
                            // vaults unload
                            VaultManager vaultManager = TalusClient.instance().vaultManager;

                            if (!vaultManager.areVaultsLoaded())
                            {
                                c.getSource().sendError(TalusClient.header(VAULTS_NOT_LOADED));
                                return 0;
                            }

                            if (vaultManager.unloadVaults(true))
                            {
                                c.getSource().sendFeedback(TalusClient.header(SUCCESS_SAVE));
                                return 1;
                            }
                            c.getSource().sendError(TalusClient.header(ERROR_SAVE));

                            return 0;
                        })
                )
                .then(
                    literal("status")
                        .executes(c -> {
                            // vaults unload
                            VaultManager vaultManager = TalusClient.instance().vaultManager;

                            if (!vaultManager.areVaultsLoaded())
                            {
                                c.getSource().sendFeedback(TalusClient.header(STATUS_NOT_LOADED));
                            } else
                            {
                                int i = vaultManager.getVaultCountForCurrentWorld();
                                c.getSource().sendFeedback(TalusClient.header(Component.translatable("talus.vaults.command.status.loaded", i).withStyle(ChatFormatting.WHITE)));
                            }

                            return 0;
                        })
                )
        );
    }

    private static int highlight(CommandContext<FabricClientCommandSource> c, Boolean locked, Boolean ominious)
    {
        VaultManager vaultManager = TalusClient.instance().vaultManager;

        if (!vaultManager.areVaultsLoaded())
        {
            c.getSource().sendError(TalusClient.header(VAULTS_NOT_LOADED));
            return 0;
        }

        Vec3 position = c.getSource().getPosition();
        int radius = getInteger(c, "radius");

        vaultManager.clearHighlights();
        int highlighted = 0;

        if (locked == null)
        {
            if (ominious == null)
            {
                highlighted += vaultManager.highlightAll(position, radius);
            } else
            {
                highlighted += vaultManager.highlight(position, radius, true, ominious);
                highlighted += vaultManager.highlight(position, radius, false, ominious);
            }
        } else
        {
            if (ominious == null)
            {
                highlighted += vaultManager.highlight(position, radius, locked, true);
                highlighted += vaultManager.highlight(position, radius, locked, false);
            } else
            {
                highlighted += vaultManager.highlight(position, radius, locked, ominious);
            }
        }

        c.getSource().sendFeedback(TalusClient.header(Component.translatable("talus.vaults.command.vaults_highlighted", highlighted).withStyle(ChatFormatting.WHITE)));

        return 1;
    }
}
