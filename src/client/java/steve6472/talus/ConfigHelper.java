package steve6472.talus;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.io.*;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
public class ConfigHelper
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonElement loadJson(File file)
    {
        if (!file.exists())
            return new JsonObject();

        try (Reader reader = new FileReader(file))
        {
            return JsonParser.parseReader(reader);
        } catch (IOException e)
        {
            Talus.LOGGER.error("Could not load json from '{}'", file);
            throw new RuntimeException(e);
        }
    }

    public static void saveJson(File file, JsonElement json)
    {
        try
        {
            String string = GSON.toJson(json);
            writeToFile(string, file);
        } catch (IOException e)
        {
            Talus.LOGGER.error("Could not save json to '{}'", file);
            throw new RuntimeException(e);
        }
    }

    private static void writeToFile(String jsonString, File file) throws IOException
    {
        File parentFile = file.getParentFile();

        if (parentFile != null && !parentFile.exists())
        {
            Talus.LOGGER.error("The parent folder '{}' does not exist", parentFile);
        }

        try (FileWriter fileWriter = new FileWriter(file))
        {
            fileWriter.write(jsonString);
        }
    }

    public static <T> T loadCodec(File file, Codec<T> codec)
    {
        JsonElement jsonElement = loadJson(file);
        if (jsonElement == null)
            return null;

        DataResult<Pair<T, JsonElement>> decode = codec.decode(JsonOps.INSTANCE, jsonElement);
        if (decode.isError())
        {
            decode.error().ifPresentOrElse(
                error -> Talus.LOGGER.error("Decoding error for file '{}': {}", file, error.message()),
                () -> Talus.LOGGER.warn("Decoding result was an error but no error was given")
            );
            return null;
        }

        return decode.getOrThrow().getFirst();
    }

    public static <T> boolean saveCodec(File file, Codec<T> codec, T object)
    {
        DataResult<JsonElement> encode = codec.encodeStart(JsonOps.INSTANCE, object);
        if (encode.isError())
        {
            encode.error().ifPresentOrElse(
                error -> Talus.LOGGER.error("Encoding error for file '{}': {}", file, error.message()),
                () -> Talus.LOGGER.warn("Encoding result was an error but no error was given")
            );
            return false;
        }

        JsonElement orThrow = encode.getOrThrow();
        saveJson(file, orThrow);
        return true;
    }
}
