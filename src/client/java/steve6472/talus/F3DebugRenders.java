package steve6472.talus;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static steve6472.talus.Talus.id;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class F3DebugRenders
{
    public static void registerDebugEntries()
    {
        var groupCustom = id("custom");
        var groupLookAt = id("lookat");

        DebugScreenEntries.register(id("talus_basic"), (displayer, level, clientChunk, serverChunk) ->
        {
            Minecraft minecraft = Minecraft.getInstance();
            Entity entity = minecraft.getCameraEntity();
            if (entity == null)
                return;

            int i = minecraft.getFramerateLimitTracker().getFramerateLimit();
            Options options = minecraft.options;
            String fpsText = String.format(Locale.ROOT, "%d fps T: %s%s", minecraft.getFps(), i == 260 ? "inf" : i, options.enableVsync().get() ? " vsync" : "");

            BlockPos blockPos = minecraft.getCameraEntity().blockPosition();
            String posText = String.format(Locale.ROOT, "Pos: %s%d %s%d %s%d", ChatFormatting.RED, blockPos.getX(), ChatFormatting.GREEN, blockPos.getY(), ChatFormatting.BLUE, blockPos.getZ());

            String facingText = getFacingString(entity);
            displayer.addToGroup(groupCustom, fpsText);
            displayer.addToGroup(groupCustom, facingText);
            displayer.addToGroup(groupCustom, posText);
        });

        DebugScreenEntries.register(id("talus_lookat_no_tag"), (displayer, level, clientChunk, serverChunk) ->
        {
            Minecraft minecraft = Minecraft.getInstance();
            Entity entity = minecraft.getCameraEntity();
            Level level2 = SharedConstants.DEBUG_SHOW_SERVER_DEBUG_VALUES ? level : Minecraft.getInstance().level;
            if (entity == null || level2 == null)
                return;
            HitResult hitResult = entity.pick(20.0, 0.0F, false);

            List<String> list = new ArrayList<>();
            if (hitResult.getType() == HitResult.Type.BLOCK)
            {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState blockState = level2.getBlockState(blockPos);
                list.add(ChatFormatting.UNDERLINE + "Targeted Block: " + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
                list.add(String.valueOf(BuiltInRegistries.BLOCK.getKey(blockState.getBlock())));

                for (Map.Entry<Property<?>, Comparable<?>> propertyComparableEntry : blockState.getValues().entrySet())
                {
                    list.add(getPropertyValueString(propertyComparableEntry));
                }
            }
            displayer.addToGroup(groupLookAt, list);
        });
    }

    private static String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry)
    {
        Property<?> property = entry.getKey();
        Comparable<?> comparable = entry.getValue();
        String string = Util.getPropertyName(property, comparable);

        if (Boolean.TRUE.equals(comparable))
        {
            string = ChatFormatting.GREEN + string;
        } else if (Boolean.FALSE.equals(comparable))
        {
            string = ChatFormatting.RED + string;
        }

        return property.getName() + ": " + string;
    }

    private static String getFacingString(Entity entity)
    {
        Direction direction = entity.getDirection();
        String dirString;
        switch (direction)
        {
            case NORTH -> dirString = "-Z";
            case SOUTH -> dirString = "+Z";
            case WEST -> dirString = "-X";
            case EAST -> dirString = "+X";
            default -> dirString = "Invalid";
        }

        return String.format(Locale.ROOT, "%s (%s) (%.1f / %.1f)", direction, dirString, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot()));
    }
}
