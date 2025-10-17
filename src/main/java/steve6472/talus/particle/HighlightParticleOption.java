package steve6472.talus.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;
import steve6472.talus.TalusParticle;

/**
 * Created by steve6472
 * Date: 10/17/2025
 * Project: Talus <br>
 */
public record HighlightParticleOption(int color, int duration, float scale) implements ParticleOptions
{
    public static final MapCodec<HighlightParticleOption> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(HighlightParticleOption::color),
        ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(HighlightParticleOption::duration),
        ExtraCodecs.POSITIVE_FLOAT.fieldOf("scale").forGetter(HighlightParticleOption::scale)
    ).apply(instance, HighlightParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HighlightParticleOption> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, HighlightParticleOption::color,
        ByteBufCodecs.INT, HighlightParticleOption::duration,
        ByteBufCodecs.FLOAT, HighlightParticleOption::scale,
        HighlightParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType()
    {
        return TalusParticle.HIGHLIGHT_PARTICLE;
    }
}
