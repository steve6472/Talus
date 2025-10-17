package steve6472.talus.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by steve6472
 * Date: 10/17/2025
 * Project: Talus <br>
 */
public class HighlightParticle extends SingleQuadParticle
{
    HighlightParticle(ClientLevel clientLevel, double d, double e, double f, int color, float scale, TextureAtlasSprite textureAtlasSprite)
    {
        super(clientLevel, d, e, f, textureAtlasSprite);
        color = ARGB.scaleRGB(color, 0.875F + this.random.nextFloat() * 0.25F, 0.875F + this.random.nextFloat() * 0.25F, 0.875F + this.random.nextFloat() * 0.25F);
        this.alpha = (float) ARGB.alpha(color) / 255f;
        this.rCol = (float) ARGB.red(color) / 255f;
        this.gCol = (float) ARGB.green(color) / 255f;
        this.bCol = (float) ARGB.blue(color) / 255f;
        this.quadSize = 0.125f * scale;
    }

    @Override
    protected @NotNull Layer getLayer()
    {
        return Layer.TRANSLUCENT;
    }

    public int getLightColor(float f)
    {
        return 0xf000f0;
    }

    public static class Provider implements ParticleProvider<HighlightParticleOption>
    {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet)
        {
            this.sprite = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(HighlightParticleOption particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource)
        {
            HighlightParticle particle = new HighlightParticle(clientLevel, d, e, f, particleOptions.color(), particleOptions.scale(), sprite.get(randomSource));
            particle.setLifetime(particleOptions.duration());
            return particle;
        }
    }
}
