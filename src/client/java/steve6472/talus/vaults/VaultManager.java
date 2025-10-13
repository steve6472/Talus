package steve6472.talus.vaults;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import steve6472.talus.ConfigHelper;
import steve6472.talus.Talus;
import steve6472.talus.TalusClient;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class VaultManager
{
    private static final Path CONFIG_FILE = Talus.CONFIG_FOLDER.resolve("vaults.json");
    private static final Component FORCIBLY_MARKED_OPEN = Component.translatable("talus.vaults.interact.force_mark_open").withStyle(ChatFormatting.WHITE);
    private static final Component VAULT_OPENED = Component.translatable("talus.vaults.interact.vault_open").withStyle(ChatFormatting.WHITE);
    private static final Component NEW_VAULT = Component.translatable("talus.vaults.interact.new_vault").withStyle(ChatFormatting.WHITE);

    // 15 minutes
    private static final long HIGHLIGHT_DURATION = 15 * 60 * 1000;
    public static final int INTERACT_RADIUS = 128;

    public static final Codec<Map<String, List<MemoryVault>>> CODEC = Codec.unboundedMap(Codec.STRING, MemoryVault.CODEC.listOf());

    public final List<Pair<Long, List<MemoryVault>>> renderedVaults = new ArrayList<>();
    private String worldName;
    private List<MemoryVault> vaults;

    public VaultManager()
    {

    }

    public void init()
    {
        ClientPlayConnectionEvents.JOIN.register((n, p, c) ->
        {
            if (c.isSingleplayer())
            {
                IntegratedServer server = c.getSingleplayerServer();
                if (server == null)
                    return;
                worldName = server.getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
            } else
            {
                ServerData info = n.getServerData();
                if (info == null)
                    return;
                worldName = info.ip;
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((clientPacketListener, minecraft) ->
        {
            if (areVaultsLoaded())
            {
                Talus.LOGGER.info("Unloading & saving vaults");
                unloadVaults(true);
            }
        });

        UseBlockCallback.EVENT.register((player, level, interactionHand, blockHitResult) ->
        {
            if (!areVaultsLoaded())
                return InteractionResult.PASS;

            if (!level.isClientSide())
                return InteractionResult.PASS;

            if (interactionHand != InteractionHand.MAIN_HAND)
                return InteractionResult.PASS;

            BlockPos hitPos = blockHitResult.getBlockPos();
            BlockState blockState = level.getBlockState(hitPos);
            if (blockState.getBlock() != Blocks.VAULT)
                return InteractionResult.PASS;

            ItemStack stack = player.getItemInHand(interactionHand);
            boolean ominous = blockState.getValue(VaultBlock.OMINOUS);

            // add locked
            MemoryVault vault = new MemoryVault(hitPos, true, ominous);
            if (addVault(vault, false))
            {
                player.displayClientMessage(TalusClient.header(NEW_VAULT), false);
            }

            // force unlock when close
            boolean close = player.position().distanceToSqr(hitPos.getBottomCenter()) <= 2.5 * 2.5;
            if (blockState.getValue(VaultBlock.STATE) == VaultState.INACTIVE && player.isCrouching() && stack.isEmpty() && close)
            {
                vault = new MemoryVault(hitPos, false, ominous);
                addVault(vault, true);
                player.displayClientMessage(TalusClient.header(FORCIBLY_MARKED_OPEN), false);
                highlightAll(hitPos.getCenter(), INTERACT_RADIUS);
                return InteractionResult.PASS;
            }

            boolean matchKey = (ominous && stack.getItem() == Items.OMINOUS_TRIAL_KEY) || (!ominous && stack.getItem() == Items.TRIAL_KEY);

            if (matchKey && !player.isCrouching() && blockState.getValue(VaultBlock.STATE) == VaultState.ACTIVE)
            {
                vault = new MemoryVault(hitPos, false, ominous);
                player.displayClientMessage(TalusClient.header(VAULT_OPENED), false);
                addVault(vault, true);
            }

            highlightAll(hitPos.getCenter(), INTERACT_RADIUS);
            return InteractionResult.PASS;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((n, c) -> worldName = null);

        ClientCommandRegistrationCallback.EVENT.register(VaultsCommand::register);
    }

    public Collection<MemoryVault> getVaults(Vec3 center, int radius)
    {
        return vaults.stream().filter(v ->
        {
            double v1 = v.position().distToCenterSqr(center.x, center.y, center.z);
            return Math.sqrt(v1) <= radius;
        }).toList();
    }

    public int highlightAll(Vec3 center, int radius)
    {
        clearHighlights();
        int highlighted = 0;
        highlighted += highlight(center, radius, true, true);
        highlighted += highlight(center, radius, true, false);
        highlighted += highlight(center, radius, false, true);
        highlighted += highlight(center, radius, false, false);
        return highlighted;
    }

    public int highlight(Vec3 center, int radius, boolean locked, boolean ominious)
    {
        List<MemoryVault> list = getVaults(center, radius)
            .stream()
            .filter(v -> v.locked() == locked && v.ominious() == ominious)
            .toList();

        long timeTarget = System.currentTimeMillis() + HIGHLIGHT_DURATION;

        renderedVaults.add(Pair.of(timeTarget, list));
        return list.size();
    }

    public void clearHighlights()
    {
        renderedVaults.clear();
    }

    // Returns true if vault was added
    public boolean addVault(MemoryVault memoryVault, boolean replace)
    {
        MemoryVault previous = null;
        for (MemoryVault vault : vaults)
        {
            if (vault.position().equals(memoryVault.position()))
            {
                previous = vault;
                break;
            }
        }

        if (previous != null && !replace)
            return false;

        vaults.remove(previous);
        vaults.add(memoryVault);
        return true;
    }

    /// Return false if an error occured, true otherwise
    public boolean loadVaults()
    {
        renderedVaults.clear();

        if (!Talus.instance().canConfigure)
            return false;

        Map<String, List<MemoryVault>> loadedVaults = ConfigHelper.loadCodec(CONFIG_FILE.toFile(), CODEC);
        if (loadedVaults == null)
            return false;

        // Make mutable
        List<MemoryVault> memoryVaults = loadedVaults.get(worldName);
        if (memoryVaults == null)
            memoryVaults = new ArrayList<>();
        vaults = new ArrayList<>(memoryVaults);
        return true;
    }

    /// Return false if an error occured, true otherwise
    public boolean saveVaults()
    {
        if (!Talus.instance().canConfigure)
            return false;

        Map<String, List<MemoryVault>> loadedVaults = ConfigHelper.loadCodec(CONFIG_FILE.toFile(), CODEC);
        if (loadedVaults == null)
            loadedVaults = new HashMap<>();
        loadedVaults = new HashMap<>(loadedVaults);
        loadedVaults.put(worldName, vaults);
        return ConfigHelper.saveCodec(CONFIG_FILE.toFile(), CODEC, loadedVaults);
    }

    /// Return false if an error occured, true otherwise
    public boolean unloadVaults(boolean save)
    {
        boolean ret = false;
        if (save)
            ret = saveVaults();
        vaults = null;
        renderedVaults.clear();
        return ret;
    }

    public boolean areVaultsLoaded()
    {
        return vaults != null;
    }

    public int getVaultCountForCurrentWorld()
    {
        return vaults.size();
    }
}