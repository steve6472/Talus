package steve6472.talus.vaults;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public record MemoryVault(BlockPos position, boolean locked, boolean ominious)
{
    public static final Codec<MemoryVault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("position").forGetter(MemoryVault::position),
        Codec.BOOL.fieldOf("locked").forGetter(MemoryVault::locked),
        Codec.BOOL.fieldOf("ominious").forGetter(MemoryVault::ominious)
    ).apply(instance, MemoryVault::new));
}
