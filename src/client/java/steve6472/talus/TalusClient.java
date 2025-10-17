package steve6472.talus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.particle.EndRodParticle;
import net.minecraft.client.particle.TrailParticle;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import steve6472.talus.keys.ElytraSwap;
import steve6472.talus.particle.HighlightParticle;
import steve6472.talus.vaults.VaultManager;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class TalusClient implements ClientModInitializer
{
    public static final KeyMapping.Category TALUS_KEY_CATEGORY = KeyMapping.Category.register(Talus.id("main"));

    private static TalusClient instance;

    public VaultManager vaultManager;

    @Override
    public void onInitializeClient()
    {
        instance = this;

        ParticleFactoryRegistry.getInstance().register(TalusParticle.HIGHLIGHT_PARTICLE, HighlightParticle.Provider::new);

        ElytraSwap.init();
        F3DebugRenders.registerDebugEntries();

        vaultManager = new VaultManager();
        vaultManager.init();
    }

    public static Component header(Component tail)
    {
        return Component
            .literal("[").withStyle(ChatFormatting.GREEN)
            .append(Component
                .literal("Talus").withStyle(ChatFormatting.DARK_GREEN))
            .append("] ").withStyle(ChatFormatting.GREEN)
            .append(tail);
    }

    public static TalusClient instance()
    {
        return instance;
    }
}