package steve6472.talus.vaults;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import steve6472.talus.TalusClient;

import java.util.Iterator;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class VaultsRenderer implements DebugRenderer.SimpleDebugRenderer
{
    private static final float[] LOCKED_OMINIOUS = new float[] {0.12f, 0.64f, 0.64f, 0.75f};
    private static final float[] UNLOCKED_OMINIOUS = new float[] {0.12f, 0.64f, 0.64f, 0.2f};
    private static final float[] LOCKED_NORMAL = new float[] {0.8f, 0.48f, 0.25f, 0.75f};
    private static final float[] UNLOCKED_NORMAL = new float[] {0.8f, 0.48f, 0.25f, 0.2f};

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum)
    {
        VaultManager vaultManager = TalusClient.instance().vaultManager;
        if (!vaultManager.areVaultsLoaded())
        {
            return;
        }

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
                float[] c = pick(vault.locked(), vault.ominious());
                DebugRenderer.renderFilledBox(poseStack, multiBufferSource, vault.position(), 1f / 16f, c[0], c[1], c[2], c[3]);
            }
        }
    }

    private static float[] pick(boolean locked, boolean ominious)
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
