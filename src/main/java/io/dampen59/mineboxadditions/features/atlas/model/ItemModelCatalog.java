package io.dampen59.mineboxadditions.features.atlas.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ItemModelCatalog {

    public sealed interface Resolved permits NewModel, LegacyModel {}

    public record NewModel(Identifier itemModel, boolean oversizedInGui) implements Resolved {}

    public record LegacyModel(Identifier baseItem, int customModelData) implements Resolved {}

    private static final String NAMESPACE = "minebox";
    private static final String NEW_MODEL_ROOT = "items/items/";
    private static final String SPRITE_ROOT = "models/item/sprites/";
    private static final String OVERRIDE_ROOT = "models/item/";

    private static ResourceManager builtFrom;
    private static Map<String, Resolved> cache = Map.of();

    private ItemModelCatalog() {}

    public static synchronized @Nullable Resolved resolve(String itemId) {
        ensureBuilt();
        return cache.get(itemId);
    }

    public static ItemStack buildDisplayStack(String itemId) {
        Resolved resolved = resolve(itemId);
        if (resolved instanceof NewModel newModel) {
            ItemStack stack = new ItemStack(Items.PAPER);
            stack.set(DataComponents.ITEM_MODEL, newModel.itemModel());
            return stack;
        }
        if (resolved instanceof LegacyModel legacy) {
            Item base = BuiltInRegistries.ITEM.getOptional(legacy.baseItem()).orElse(Items.PAPER);
            ItemStack stack = new ItemStack(base);
            stack.set(DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of((float) legacy.customModelData()), List.of(), List.of(), List.of()));
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static void ensureBuilt() {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if (manager == builtFrom) return;
        builtFrom = manager;
        cache = build(manager);
    }

    private static Map<String, Resolved> build(ResourceManager manager) {
        Map<String, Resolved> result = new HashMap<>();

        Map<Identifier, Resource> newDefs = manager.listResources(
                NEW_MODEL_ROOT.substring(0, NEW_MODEL_ROOT.length() - 1),
                id -> id.getNamespace().equals(NAMESPACE) && id.getPath().endsWith(".json"));
        for (var entry : newDefs.entrySet()) {
            String path = entry.getKey().getPath();
            String rest = path.substring(NEW_MODEL_ROOT.length(), path.length() - ".json".length());
            String itemId = rest.substring(rest.lastIndexOf('/') + 1);
            Identifier itemModel = Identifier.fromNamespaceAndPath(NAMESPACE, "items/" + rest);
            boolean oversized = readOversizedFlag(entry.getValue());
            NewModel model = new NewModel(itemModel, oversized);
            result.put(itemId, model);

            if (itemId.startsWith("sprite_")) {
                String alias = itemId.substring("sprite_".length());
                result.putIfAbsent(alias, model);
            }
        }

        Set<String> spriteIds = new HashSet<>();
        Map<Identifier, Resource> sprites = manager.listResources(SPRITE_ROOT.substring(0, SPRITE_ROOT.length() - 1),
                id -> id.getNamespace().equals(NAMESPACE) && id.getPath().endsWith(".json"));
        for (Identifier id : sprites.keySet()) {
            String path = id.getPath();
            spriteIds.add(path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length()));
        }

        if (!spriteIds.isEmpty()) {
            Map<String, LegacyModel> reverse = buildLegacyOverrideReverseIndex(manager);
            for (String spriteId : spriteIds) {
                LegacyModel legacy = reverse.get(NAMESPACE + ":item/sprites/" + spriteId);
                if (legacy != null) result.putIfAbsent(spriteId, legacy);
            }
        }

        return result;
    }

    private static boolean readOversizedFlag(Resource resource) {
        try (var reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return json.has("oversized_in_gui") && json.get("oversized_in_gui").getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private static Map<String, LegacyModel> buildLegacyOverrideReverseIndex(ResourceManager manager) {
        Map<String, LegacyModel> reverse = new HashMap<>();
        Map<Identifier, Resource> overrideDefs = manager.listResources("models/item",
                id -> id.getNamespace().equals("minecraft")
                        && id.getPath().endsWith(".json")
                        && id.getPath().indexOf('/', OVERRIDE_ROOT.length()) < 0);

        for (var entry : overrideDefs.entrySet()) {
            String path = entry.getKey().getPath();
            String stem = path.substring(OVERRIDE_ROOT.length(), path.length() - ".json".length());
            Identifier baseItem = Identifier.fromNamespaceAndPath("minecraft", stem);

            try (var reader = entry.getValue().openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (!json.has("overrides")) continue;
                JsonArray overrides = json.getAsJsonArray("overrides");
                for (JsonElement element : overrides) {
                    JsonObject override = element.getAsJsonObject();
                    JsonObject predicate = override.has("predicate") ? override.getAsJsonObject("predicate") : null;
                    if (predicate == null || !predicate.has("custom_model_data") || !override.has("model")) continue;
                    int customModelData = predicate.get("custom_model_data").getAsInt();
                    String model = override.get("model").getAsString();
                    reverse.putIfAbsent(model, new LegacyModel(baseItem, customModelData));
                }
            } catch (Exception ignored) {
                // skip
            }
        }

        return reverse;
    }
}
