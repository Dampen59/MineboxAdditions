package io.dampen59.mineboxadditions.features.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class MineboxItem {
    @JsonProperty("id")
    private String id;

    @JsonProperty("level")
    private int level;

    @JsonProperty("category")
    private String category;

    @JsonProperty("rarity")
    private String rarity;

    @JsonProperty("texture")
    private String texture;

    @JsonProperty("mbxStats")
    private Map<String, MineboxStat> mbxStats;

    @JsonProperty("recipe")
    private Recipe recipe;

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public String getCategory() {
        return category;
    }

    public String getRarity() {
        return rarity;
    }

    public String getTexture() {
        return texture;
    }

    public Map<String, MineboxStat> getMbxStats() {
        return mbxStats;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public static class Recipe {
        @JsonProperty("job")
        private String job;

        @JsonProperty("ingredients")
        private List<Ingredient> ingredients;

        public String getJob() {
            return job;
        }

        public List<Ingredient> getIngredients() {
            return ingredients;
        }
    }

    public static class Ingredient {
        @JsonProperty("type")
        private String type;

        @JsonProperty("id")
        private String id;

        @JsonProperty("amount")
        private int amount;

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public int getAmount() {
            return amount;
        }

        public boolean isVanilla() {
            return "vanilla".equalsIgnoreCase(type);
        }

        public Text getDisplayName() {
            if (isVanilla()) {
                Identifier vanillaId = Identifier.of("minecraft", id);
                if (net.minecraft.registry.Registries.ITEM.containsId(vanillaId)) {
                    Item vanillaItem = net.minecraft.registry.Registries.ITEM.get(vanillaId);
                    return vanillaItem.getName();
                } else {
                    return Text.translatable("item.minecraft." + id);
                }
            } else {
                MineboxItem item = MineboxAdditions.INSTANCE.state.getItemById(id);
                return item != null ? MineboxItem.getDisplayName(item) : Text.literal(id);
            }
        }


        public String getTranslationKey() {
            return "mbx.items." + id + ".name";
        }

        public Identifier getTexture() {
            if (isVanilla()) {
                Identifier vanillaId = Identifier.of("minecraft", id);
                if (net.minecraft.registry.Registries.ITEM.containsId(vanillaId)) {
                    return Identifier.of("minecraft", "textures/item/" + id + ".png");
                }
                return Identifier.of("minecraft", "textures/item/" + id + ".png");
            } else {
                MineboxItem item = MineboxAdditions.INSTANCE.state.getItemById(id);
                return item != null
                        ? ItemListWidget.ItemEntry.getTexture(item.getId())
                        : null;
            }
        }


        public MineboxItem getCustomItem() {
            return isVanilla() ? null : MineboxAdditions.INSTANCE.state.getItemById(id);
        }


        public ItemStack getVanillaStack() {
            if (!isVanilla()) return ItemStack.EMPTY;
            try {
                Item item = Registries.ITEM.get(Identifier.of("minecraft", id));
                return new ItemStack(item);
            } catch (Exception e) {
                return ItemStack.EMPTY;
            }
        }

    }

    public static String getLoreText(String itemId) {
        StringBuilder loreBuilder = new StringBuilder();
        Language language = Language.getInstance();

        int index = 0;
        while (true) {
            String key = "mbx.items." + itemId + ".lore." + index;
            if (!language.hasTranslation(key)) break;

            String raw = Text.translatable(key).getString();
            String cleaned = raw.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").replaceAll("[\\r\\n\\t]", " ");
            if (index > 0) loreBuilder.append(" ");
            loreBuilder.append(cleaned.trim());
            index++;
        }

        return loreBuilder.toString();
    }

    public static Text getDisplayName(MineboxItem item) {
        String id = item.getId();
        String rarity = item.getRarity().toLowerCase();
        Language lang = Language.getInstance();

        // Style (mbx rarity)
        Function<Text, Text> styled = base ->
                base.copy().styled(style ->
                        style.withColor(RaritiesUtils.getRarityColor(rarity).getRGB())
                                .withBold(true));

        // Fallback
        String nameKey = "mbx.items." + id + ".name";
        if (lang.hasTranslation(nameKey)) {
            return styled(Text.translatable(nameKey), item.rarity.toLowerCase());
        }

        // Stats
        if (id.matches("candy_enchanted_.*")) return formatStatName("mbx.items.candies_enchanted.name", id, styled);
        if (id.matches("candy_.*")) return formatStatName("mbx.items.candies_big.name", id, styled);
        if (id.startsWith("rune_small")) return formatStatName("mbx.items.runes_small.name", id, styled);
        if (id.startsWith("rune_big")) return formatStatName("mbx.items.runes_big.name", id, styled);
        if (id.startsWith("rune_enchanted")) return formatStatName("mbx.items.runes_enchanted.name", id, styled);
        if (id.startsWith("scroll_small")) return formatStatName("mbx.items.scrolls_small.name", id, styled);
        if (id.startsWith("scroll_big")) return formatStatName("mbx.items.scrolls_big.name", id, styled);
        if (id.startsWith("scroll_enchanted")) return formatStatName("mbx.items.scrolls_enchanted.name", id, styled);

        // Containers
        Map<String, String> containers = Map.of(
                "transformed_", "mbx.items.container.transformed",
                "bag_", "mbx.items.container.bag",
                "crate_", "mbx.items.container.crate",
                "barrel_", "mbx.items.container.barrel",
                "enchanted_", "mbx.items.container.enchanted"
        );

        for (var entry : containers.entrySet()) {
            if (id.startsWith(entry.getKey())) {
                String resource = id.substring(entry.getKey().length());
                return styled(Text.translatable(entry.getValue(), resolveResource(resource)), item.rarity.toLowerCase());
            }
        }

        // Harvesters
        if (id.startsWith("harvester_")) {
            if (id.endsWith("_reaper")) {
                String resource = id.substring("harvester_".length(), id.length() - "_reaper".length());
                return styled(Text.translatable("mbx.items.reaper.name", resolveResource(resource)), item.rarity.toLowerCase());
            } else if (id.endsWith("_chopper")) {
                String resource = id.substring("harvester_".length(), id.length() - "_chopper".length());
                return styled(Text.translatable("mbx.items.chopper.name", resolveResource(resource)), item.rarity.toLowerCase());
            } else {
                String resource = id.substring("harvester_".length());
                return styled(Text.translatable("mbx.items.harvester.name", resolveResource(resource)), item.rarity.toLowerCase());
            }
        }

        // Owner Removers
        if (id.startsWith("owner_remover_")) {
            String rarityKey = id.substring("owner_remover_".length());
            Text rarityText = Text.translatable("mbx.rarities." + rarityKey + ".title");
            return styled(Text.translatable("mbx.items.owner_remover.name", rarityText.getString()), item.rarity.toLowerCase());
        }

        // Haversacks
        if (id.startsWith("haversack_small_")) {
            String resource = id.substring("haversack_small_".length());
            return styled(Text.translatable("mbx.items.haversack_small.name", resolveResource(resource)), item.rarity.toLowerCase());
        }

        if (id.startsWith("haversack_")) {
            String resource = id.substring("haversack_".length());
            return styled(Text.translatable("mbx.items.haversack.name", resolveResource(resource)), item.rarity.toLowerCase());
        }

        // Spawners and souls
        if (id.startsWith("spawner_")) {
            String mob = id.substring("spawner_".length());
            return styled(Text.translatable("mbx.items.spawner.name", resolveEntity(mob)), item.rarity.toLowerCase());
        }

        if (id.startsWith("soul_")) {
            String mob = id.substring("soul_".length());
            return styled(Text.translatable("mbx.items.soul.name", resolveEntity(mob)), item.rarity.toLowerCase());
        }

        // Default
        return Text.literal(id).copy().styled(style -> style.withColor(0xFFFFFFFF));
    }

    private static Text statName(String formatKey, String id, Function<Text, Text> styleFunc) {
        return statName(formatKey, id, styleFunc, "mbx.stats.");
    }

    private static Text statName(String formatKey, String id, Function<Text, Text> styleFunc, String statPrefix) {
        String stat = id.substring(id.lastIndexOf('_') + 1);
        Text translatedStat = Text.translatable(statPrefix + stat);
        return styleFunc.apply(Text.translatable(formatKey, translatedStat.getString()));
    }

    private static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private static Text formatStatName(String key, String id, Function<Text, Text> styledFunc) {
        return formatStatName(key, id, styledFunc, "mbx.stats.");
    }

    private static Text formatStatName(String key, String id, Function<Text, Text> styledFunc, String statPrefix) {
        String stat = id.substring(id.lastIndexOf('_') + 1);
        Text translatedStat = Text.translatable(statPrefix + stat);
        return styledFunc.apply(Text.translatable(key, translatedStat.getString()));
    }


    private static Text resolveResource(String target) {
        Language lang = Language.getInstance();
        if (lang.hasTranslation("block.minecraft." + target))
            return Text.translatable("block.minecraft." + target);
        if (lang.hasTranslation("item.minecraft." + target))
            return Text.translatable("item.minecraft." + target);
        return Text.of(target);
    }

    private static Text resolveEntity(String name) {
        Language lang = Language.getInstance();
        if (lang.hasTranslation("entity.minecraft." + name))
            return Text.translatable("entity.minecraft." + name);
        return Text.of(name);
    }


    public Optional<MineboxStat> getStat(String statName) {
        return mbxStats == null
                ? Optional.empty()
                : Optional.ofNullable(mbxStats.get(statName));
    }

    private static Text styled(Text base, String rarity) {
        return base.copy().styled(style ->
                style.withColor(RaritiesUtils.getRarityColor(rarity.toLowerCase()).getRGB())
                        .withBold(true)
        );
    }

}
