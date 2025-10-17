package steve6472.talus.vaults;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import steve6472.talus.TalusClient;

import java.util.Iterator;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class VaultsRenderer
{
    private record RenderSettnigs(int count, double spawnChance, int color) {}

    private static final RenderSettnigs LOCKED_OMINIOUS = new RenderSettnigs(16, 0.13, 0xff_1fa3a3);
    private static final RenderSettnigs UNLOCKED_OMINIOUS = new RenderSettnigs(8, 0.08, 0x60_1fa3a3);
    private static final RenderSettnigs LOCKED_NORMAL = new RenderSettnigs(16, 0.13, 0xff_cc7a41);
    private static final RenderSettnigs UNLOCKED_NORMAL = new RenderSettnigs(8, 0.08, 0x60_cc7a41);

    public static void register(ClientLevel level)
    {
        if (level.getGameTime() % 2 != 0)
            return;

        VaultManager vaultManager = TalusClient.instance().vaultManager;
        if (!vaultManager.areVaultsLoaded())
            return;

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        final int highQualityDistance = 24;
        final int mediumQualityDistance = 48;

        long now = System.currentTimeMillis();

        for (Iterator<Pair<Long, List<MemoryVault>>> iterator = vaultManager.renderedVaults.iterator(); iterator.hasNext(); )
        {
            Pair<Long, List<MemoryVault>> renderedVault = iterator.next();
            if (renderedVault.getFirst() <= now)
            {
                iterator.remove();
                continue;
            }

            List<MemoryVault> vaults = renderedVault.getSecond();

            for (MemoryVault vault : vaults)
            {
                RenderSettnigs c = pick(vault.locked(), vault.ominious());
                double distance = vault.position().distToCenterSqr(cameraPos.x, cameraPos.y, cameraPos.z);

                if (distance <= highQualityDistance * highQualityDistance)
                {
                    FancyCubeHighlighter.render(level, vault.position(), c.count(), c.spawnChance(), 10, c.color(), 1);
                } else if (distance <= mediumQualityDistance * mediumQualityDistance)
                {
                    if (level.getGameTime() % 20 == 0)
                        FancyCubeHighlighter.render(level, vault.position(), (int) (c.count() * 0.75), c.spawnChance(), 30, c.color(), 3f);
                }
                else if (distance <= VaultManager.INTERACT_RADIUS * VaultManager.INTERACT_RADIUS)
                {
                    if (level.getGameTime() % 40 == 0)
                        FancyCubeHighlighter.render(level, vault.position(), (int) (c.count() * 0.5), c.spawnChance(), 60, c.color(), 6f);
                }
            }
        }
    }

    private static RenderSettnigs pick(boolean locked, boolean ominious)
    {
        if (locked && ominious)
            return LOCKED_OMINIOUS;
        else if (!locked && ominious)
            return UNLOCKED_OMINIOUS;
        else if (locked)
            return LOCKED_NORMAL;
        else
            return UNLOCKED_NORMAL;
    }
}
