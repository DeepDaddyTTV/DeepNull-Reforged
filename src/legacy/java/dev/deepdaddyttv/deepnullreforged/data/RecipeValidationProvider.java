package dev.deepdaddyttv.deepnullreforged.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.deepdaddyttv.deepnullreforged.DeepNullReforged;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

public class RecipeValidationProvider implements IDataProvider {
    private final Iterable<Path> inputFolders;

    public RecipeValidationProvider(Path inputFolder) {
        this.inputFolders = Collections.singletonList(inputFolder);
    }

    @Override
    public void run(DirectoryCache cache) throws IOException {
        int validated = 0;
        for (Path input : inputFolders) {
            Path recipes = input.resolve("data").resolve(DeepNullReforged.MODID).resolve("recipes");
            if (!Files.isDirectory(recipes)) continue;
            try (Stream<Path> files = Files.list(recipes)) {
                for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".json"))::iterator) {
                    String name = file.getFileName().toString();
                    ResourceLocation id = DeepNullReforged.id(name.substring(0, name.length() - 5));
                    try (Reader reader = Files.newBufferedReader(file)) {
                        JsonElement json = new JsonParser().parse(reader);
                        substituteKnownDataTags(json);
                        RecipeManager.fromJson(id, json.getAsJsonObject());
                        validated++;
                    } catch (RuntimeException exception) {
                        throw new IOException("Invalid 1.16.5 recipe " + id + " at " + file, exception);
                    }
                }
            }
        }
        if (validated == 0) throw new IOException("No DeepNull Reforged recipes were found for validation");
    }

    private void substituteKnownDataTags(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) substituteKnownDataTags(child);
            return;
        }
        if (!element.isJsonObject()) return;
        com.google.gson.JsonObject object = element.getAsJsonObject();
        if (object.has("tag") && "deepnullreforged:null_frames".equals(object.get("tag").getAsString())) {
            object.remove("tag");
            object.addProperty("item", "deepnullreforged:deep_null_panel_0");
        }
        for (java.util.Map.Entry<String, JsonElement> entry : object.entrySet()) {
            substituteKnownDataTags(entry.getValue());
        }
    }

    @Override
    public String getName() {
        return "DeepNull Reforged 1.16.5 recipe validation";
    }
}
