package steve6472.talus;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import steve6472.talus.particle.HighlightParticleOption;

import java.util.function.Function;

/**
 * Created by steve6472
 * Date: 10/16/2025
 * Project: Talus <br>
 */
public class TalusParticle
{
    public static final ParticleType<HighlightParticleOption> HIGHLIGHT_PARTICLE = register("highlight", true, t -> HighlightParticleOption.CODEC, t -> HighlightParticleOption.STREAM_CODEC);

    public static void init()
    {
    }

    private static <T extends ParticleOptions> ParticleType<T> register(
        String key,
        boolean overrideLimiter,
        final Function<ParticleType<T>, MapCodec<T>> function,
        final Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> function2)
    {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Talus.id(key), new ParticleType<T>(overrideLimiter)
        {
            public @NotNull MapCodec<T> codec()
            {
                return function.apply(this);
            }

            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec()
            {
                return function2.apply(this);
            }
        });
    }
}
