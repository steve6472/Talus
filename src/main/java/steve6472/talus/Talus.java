package steve6472.talus;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class Talus implements ModInitializer
{
    public static final String MOD_ID = "talus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Path CONFIG_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    private static Talus instance;

    public boolean canConfigure = true;

    @Override
    public void onInitialize()
    {
        instance = this;
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        createConfigFolder();
    }

    private void createConfigFolder()
    {
        if (!Files.exists(CONFIG_FOLDER))
        {
            try
            {
                Files.createDirectory(CONFIG_FOLDER);
            } catch (IOException e)
            {
                canConfigure = false;
                throw new RuntimeException(e);
            }
        }
    }

    public static Talus instance()
    {
        return instance;
    }

    public static ResourceLocation id(String key)
    {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, key);
    }
}