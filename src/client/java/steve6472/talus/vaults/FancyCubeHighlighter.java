package steve6472.talus.vaults;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import steve6472.talus.particle.HighlightParticleOption;

/**
 * Created by steve6472
 * Date: 10/16/2025
 * Project: Talus <br>
 */
public class FancyCubeHighlighter
{
    public static void render(ClientLevel level, BlockPos pos, int particleCount, double spawnChance, int duration, int color, float scale)
    {
        double f = (Math.sin(((level.getGameTime() + pos.hashCode()) % 314) / 10d) * 0.5 + 0.5) * 0.0625;

        // vertical edges
        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() - f, pos.getZ() - f,
            pos.getX() - f, pos.getY() + 1 + f, pos.getZ() - f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() + 1 + f, pos.getY() - f, pos.getZ() - f,
            pos.getX() + 1 + f, pos.getY() + 1 + f, pos.getZ() - f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() - f, pos.getZ() + 1 + f,
            pos.getX() - f, pos.getY() + 1 + f, pos.getZ() + 1 + f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() + 1 + f, pos.getY() - f, pos.getZ() + 1 + f,
            pos.getX() + 1 + f, pos.getY() + 1 + f, pos.getZ() + 1 + f,
            color, duration, scale);

        // Bottom face

        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() - f, pos.getZ() - f,
            pos.getX() + 1 + f, pos.getY() - f, pos.getZ() - f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() - f, pos.getZ() - f,
            pos.getX() - f, pos.getY() - f, pos.getZ() + 1 + f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() - f, pos.getZ() + 1 + f,
            pos.getX() + 1 + f, pos.getY() - f, pos.getZ() + 1 + f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() + 1 + f, pos.getY() - f, pos.getZ() - f,
            pos.getX() + 1 + f, pos.getY() - f, pos.getZ() + 1 + f,
            color, duration, scale);

        // Top face

        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() + 1 + f, pos.getZ() - f,
            pos.getX() + 1 + f, pos.getY() + 1 + f, pos.getZ() - f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() + 1 + f, pos.getZ() - f,
            pos.getX() - f, pos.getY() + 1 + f, pos.getZ() + 1 + f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() - f, pos.getY() + 1 + f, pos.getZ() + 1 + f,
            pos.getX() + 1 + f, pos.getY() + 1 + f, pos.getZ() + 1 + f,
            color, duration, scale);

        line(level, particleCount, spawnChance,
            pos.getX() + 1 + f, pos.getY() + 1 + f, pos.getZ() - f,
            pos.getX() + 1 + f, pos.getY() + 1 + f, pos.getZ() + 1 + f,
            color, duration, scale);
    }

    private static void line(ClientLevel level, int particleCount, double spawnChance, double x1, double y1, double z1, double x2, double y2, double z2, int color, int duration, float scale)
    {
        double vx = (x2 - x1);
        double vy = (y2 - y1);
        double vz = (z2 - z1);

        vx = (vx) / (double) particleCount;
        vy = (vy) / (double) particleCount;
        vz = (vz) / (double) particleCount;

        for (int iteration = 0; iteration <= particleCount; iteration++)
        {
            if (Math.random() <= spawnChance)
            {
                level.addParticle(
                    new HighlightParticleOption(color, duration, scale),
                    x1, y1, z1,
                    0, 0, 0);
            }

            x1 += vx;
            y1 += vy;
            z1 += vz;
        }
    }
}
