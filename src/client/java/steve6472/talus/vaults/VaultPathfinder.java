package steve6472.talus.vaults;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import steve6472.talus.TalusClient;

import java.util.*;

/**
 * Created by steve6472
 * Date: 10/17/2025
 * Project: Talus <br>
 */
public class VaultPathfinder
{
    private static final int SEARCH_DISTANCE_LOW = 24;
    private static final int SEARCH_DISTANCE_HIGH = 48;
    private static int toCheck = 0;
    private static List<Match> foundVaults = new ArrayList<>();
    private static Path closest;
    private static Zombie dummy;

    public static void register(Level level)
    {
        VaultManager vaultManager = TalusClient.instance().vaultManager;
        if (!vaultManager.areVaultsLoaded())
            return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        // Disallow above ground
        if (player.getBlockY() > 30)
            return;

        if (!isKey(player.getMainHandItem()) && !isKey(player.getOffhandItem()))
        {
            foundVaults.clear();
            closest = null;
            return;
        }

        if (level.getGameTime() % 3 != 0 && foundVaults.isEmpty())
            return;

        boolean holdingKeyNormal = isHoldingKeyNormal(player);
        int searchDistance = holdingKeyNormal ? SEARCH_DISTANCE_LOW : SEARCH_DISTANCE_HIGH;

        List<Match> vaults = findVaults(level, player, searchDistance);

        Zombie dummy = VaultPathfinder.dummy == null ? VaultPathfinder.dummy = getDummy(level) : VaultPathfinder.dummy;
        Objects.requireNonNull(dummy.getAttribute(Attributes.FOLLOW_RANGE)).setBaseValue(searchDistance);
        dummy.setPos(player.getX(), player.getY(), player.getZ());

        if (foundVaults.isEmpty())
        {
//            System.out.println("reset");

            foundVaults = vaults.stream().filter(bp ->
            {
                MemoryVault vault = vaultManager.getVault(bp.vault());
                if (vault == null)
                    return true;
                return vault.locked();
            }).sorted((a, b) ->
            {
                double distA = a.vault().distToCenterSqr(player.getX(), player.getY(), player.getZ());
                double distB = b.vault().distToCenterSqr(player.getX(), player.getY(), player.getZ());
                if (distA == distB)
                    return 0;
                return distA < distB ? -1 : 1;
            }).toList();
            foundVaults = new ArrayList<>(foundVaults);
            toCheck = Math.min(foundVaults.size() / 3 + 1, holdingKeyNormal ? 5 : 3);

            if (foundVaults.isEmpty())
            {
                closest = null;
                return;
            } else
            {
                player.displayClientMessage(
                    Component.translatable(
                        "talus.vaults.pathfind.display_count",
                        Component.literal("" + foundVaults.size()).withStyle(ChatFormatting.GREEN),
                        Component.literal("" + searchDistance).withStyle(ChatFormatting.GREEN)
                    ), true);
            }
        }

        if (closest != null && (!closest.canReach() || closest.getNode(0).distanceTo(player.blockPosition()) > 2))
            closest = null;

        int i = 0;
        for (Iterator<Match> iterator = foundVaults.iterator(); iterator.hasNext(); )
        {
//            closest = null;
            if (i >= toCheck)
                break;

            Match vault = iterator.next();
            Path path = dummy.getNavigation().createPath(vault.vault().offset(vault.facing().getStepX(), 0, vault.facing.getStepZ()), 0);
            iterator.remove();
            i++;
            if (path == null)
                continue;

            if (closest == null)
            {
                closest = path;

                if (closest.canReach())
                {
                    foundVaults.clear();
                    break;
                }

                continue;
            }

            if (path.canReach())
            {
                closest = path;
                foundVaults.clear();
                break;
            }

            if (path.getDistToTarget() < closest.getDistToTarget())
            {
                closest = path;
            }
        }

//        System.out.println(i + " / " + toCheck + " > " + foundVaults.size() + " - " + closest);

        if (closest != null && level.getGameTime() % 3 == 0)
        {
            renderPath(level, closest, 0xa0_cc7a41);
        }
    }

    public static void reset()
    {
        dummy = null;
        closest = null;
        foundVaults.clear();
    }

    private static @NotNull Zombie getDummy(Level level)
    {
        Zombie dummyVaultMob = new Zombie(level)
        {
            @Override
            protected @NotNull PathNavigation createNavigation(Level level)
            {
                GroundPathNavigation groundPathNavigation = new GroundPathNavigation(this, level);
                groundPathNavigation.setAvoidSun(false);
                return groundPathNavigation;
            }
        };
        dummyVaultMob.setOnGround(true);
        Objects.requireNonNull(dummyVaultMob.getAttribute(Attributes.STEP_HEIGHT)).setBaseValue(3);
        dummyVaultMob.setCanBreakDoors(true);
        dummyVaultMob.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0);
        return dummyVaultMob;
    }

    private static void renderPath(Level level, Path path, @SuppressWarnings("SameParameterValue") int color)
    {
        Node lastNode = path.getNode(0);
        int realColor = path.canReach() ? color : (~color | 0xff000000);
        for (int i = 1; i < path.getNodeCount(); i++)
        {
            Node node = path.getNode(i);
            Vec3 center = lastNode.asBlockPos().getCenter();
            Vec3 center1 = node.asBlockPos().getCenter();
            FancyCubeHighlighter.line(
                level, 8, 0.7, 0.0625f,
                center.x, center.y - 0.25, center.z,
                center1.x, center1.y - 0.25, center1.z, realColor, () -> (int) (Math.random() * 5 + 3), 0.75f);
            lastNode = node;

            if (i == path.getNodeCount() - 1)
            {
                FancyCubeHighlighter.render(level, node.asBlockPos(), 4, 0.25, 0.0625f, 0.3, 5, realColor, 1f);
            }
        }
    }

    private static List<Match> findVaults(Level level, Player player, int distance)
    {
        List<Match> vaults = new ArrayList<>(24);

        for (int i = -(distance >> 4) - 1; i < (distance >> 4) + 2; i++)
        {
            for (int j = -(distance >> 4) - 1; j < (distance >> 4) + 2; j++)
            {
                int cx = i + (player.getBlockX() >> 4);
                int cz = j + (player.getBlockZ() >> 4);
                if (!level.hasChunk(cx, cz))
                    continue;

                LevelChunk chunk = level.getChunk(cx, cz);
                chunk.getBlockEntities().forEach((pos, entity) ->
                {
                    if (entity instanceof VaultBlockEntity)
                    {
                        BlockState blockState = chunk.getBlockState(pos);
                        Direction facing = blockState.getValue(VaultBlock.FACING);
                        if (pos.distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= distance * distance)
                        {
                            vaults.add(new Match(pos, facing));
                        }
                    }
                });
            }
        }

        return vaults;
    }

    private record Match(BlockPos vault, Direction facing) {}

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isKey(ItemStack itemStack)
    {
        return itemStack.is(Items.TRIAL_KEY) || itemStack.is(Items.OMINOUS_TRIAL_KEY);
    }

    private static boolean isHoldingKeyNormal(Player player)
    {
        return player.getMainHandItem().is(Items.TRIAL_KEY) || player.getOffhandItem().is(Items.TRIAL_KEY);
    }
}
