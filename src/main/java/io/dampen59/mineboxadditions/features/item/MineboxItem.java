package io.dampen59.mineboxadditions.features.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dampen59.mineboxadditions.MineboxAdditions;
import io.dampen59.mineboxadditions.features.atlas.widgets.ItemListWidget;
import io.dampen59.mineboxadditions.utils.RaritiesUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.locale.Language;

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

        public Component getDisplayName() {
            if (isVanilla()) {
                Identifier vanillaId = Identifier.fromNamespaceAndPath("minecraft", id);
                if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(vanillaId)) {
                    Item vanillaItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(vanillaId).orElse(null);
                    return Component.translatable(vanillaItem.getDescriptionId());
                } else {
                    return Component.translatable("item.minecraft." + id);
                }
            } else {
                MineboxItem item = MineboxAdditions.INSTANCE.state.getItemById(id);
                return item != null ? MineboxItem.getDisplayName(item) : Component.literal(id);
            }
        }


        public String getTranslationKey() {
            return "mbx.items." + id + ".name";
        }

        public Identifier getTexture() {
            if (isVanilla()) {
                Identifier vanillaId = Identifier.fromNamespaceAndPath("minecraft", id);
                if (net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(vanillaId)) {
                    return Identifier.fromNamespaceAndPath("minecraft", "textures/item/" + id + ".png");
                }
                return Identifier.fromNamespaceAndPath("minecraft", "textures/item/" + id + ".png");
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
                Item item = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("minecraft", id)).orElse(null);
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
            if (!language.has(key)) break;

            String raw = Component.translatable(key).getString();
            String cleaned = raw.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").replaceAll("[\\r\\n\\t]", " ");
            if (index > 0) loreBuilder.append(" ");
            loreBuilder.append(cleaned.trim());
            index++;
        }

        return loreBuilder.toString();
    }

    public static Component getDisplayName(MineboxItem item) {
        String id = item.getId();
        String rarity = item.getRarity().toLowerCase();
        Language lang = Language.getInstance();

        // Style (mbx rarity)
        Function<Component, Component> styled = base ->
                base.copy().withStyle(style ->
                        style.withColor(RaritiesUtils.getRarityColor(rarity).getRGB())
                                .withBold(true));

        // Fallback
        String nameKey = "mbx.items." + id + ".name";
        if (lang.has(nameKey)) {
            return styled(Component.translatable(nameKey), item.rarity.toLowerCase());
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
                return styled(Component.translatable(entry.getValue(), resolveResource(resource)), item.rarity.toLowerCase());
            }
        }

        // Harvesters
        if (id.startsWith("harvester_")) {
            if (id.endsWith("_reaper")) {
                String resource = id.substring("harvester_".length(), id.length() - "_reaper".length());
                return styled(Component.translatable("mbx.items.reaper.name", resolveResource(resource)), item.rarity.toLowerCase());
            } else if (id.endsWith("_chopper")) {
                String resource = id.substring("harvester_".length(), id.length() - "_chopper".length());
                return styled(Component.translatable("mbx.items.chopper.name", resolveResource(resource)), item.rarity.toLowerCase());
            } else {
                String resource = id.substring("harvester_".length());
                return styled(Component.translatable("mbx.items.harvester.name", resolveResource(resource)), item.rarity.toLowerCase());
            }
        }

        // Owner Removers
        if (id.startsWith("owner_remover_")) {
            String rarityKey = id.substring("owner_remover_".length());
            Component rarityText = Component.translatable("mbx.rarities." + rarityKey + ".title");
            return styled(Component.translatable("mbx.items.owner_remover.name", rarityText.getString()), item.rarity.toLowerCase());
        }

        // Haversacks
        if (id.startsWith("haversack_small_")) {
            String resource = id.substring("haversack_small_".length());
            return styled(Component.translatable("mbx.items.haversack_small.name", resolveResource(resource)), item.rarity.toLowerCase());
        }

        if (id.startsWith("haversack_")) {
            String resource = id.substring("haversack_".length());
            return styled(Component.translatable("mbx.items.haversack.name", resolveResource(resource)), item.rarity.toLowerCase());
        }

        // Spawners and souls
        if (id.startsWith("spawner_")) {
            String mob = id.substring("spawner_".length());
            return styled(Component.translatable("mbx.items.spawner.name", resolveEntity(mob)), item.rarity.toLowerCase());
        }

        if (id.startsWith("soul_")) {
            String mob = id.substring("soul_".length());
            return styled(Component.translatable("mbx.items.soul.name", resolveEntity(mob)), item.rarity.toLowerCase());
        }

        // Event items
        if (id.startsWith("xmas_present_small_")) {
            String colorKey = id.substring("xmas_present_small_".length());
            return styled(Component.translatable("mbx.items.xmas_present_small.name", resolveColor(colorKey)), item.rarity.toLowerCase());
        }

        if (id.startsWith("xmas_present_medium_")) {
            String colorKey = id.substring("xmas_present_medium_".length());
            return styled(Component.translatable("mbx.items.xmas_present_medium.name", resolveColor(colorKey)), item.rarity.toLowerCase());
        }

        if (id.startsWith("xmas_present_big_")) {
            String colorKey = id.substring("xmas_present_big_".length());
            return styled(Component.translatable("mbx.items.xmas_present_big.name", resolveColor(colorKey)), item.rarity.toLowerCase());
        }

        if (id.startsWith("lny_envelope_")) {
            String colorKey = id.substring("lny_envelope_".length());
            return styled(Component.translatable("mbx.items.lny_envelope.name", resolveColor(colorKey)), item.rarity.toLowerCase());
        }

        // Ornaments
        if (id.startsWith("nameplate_")) {
            String nameplateKey = id.substring("nameplate_".length());
            return styled(Component.translatable("mbx.attributes.nameplate." + nameplateKey + ".title"), item.rarity.toLowerCase());
        }

        if (id.startsWith("emote_")) {
            String emoteKey = id.substring("emote_".length());
            return styled(Component.translatable("mbx.attributes.emote." + emoteKey + ".title"), item.rarity.toLowerCase());
        }


        // Default
        return Component.literal(id).copy().withStyle(style -> style.withColor(0xFFFFFFFF));
    }

    public static Component getStatName(String statsKey) {
        String cleanedKey = statsKey.replace("_", ".").toUpperCase();
        return getColoredStatName(cleanedKey);
    }


    private static Component statName(String formatKey, String id, Function<Component, Component> styleFunc, String statPrefix) {
        String stat = id.substring(id.lastIndexOf('_') + 1);
        Component translatedStat = Component.translatable(statPrefix + stat);
        return styleFunc.apply(Component.translatable(formatKey, translatedStat.getString()));
    }

    private static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private static Component formatStatName(String key, String id, Function<Component, Component> styledFunc) {
        return formatStatName(key, id, styledFunc, "mbx.stats.");
    }

    private static Component formatStatName(String key, String id, Function<Component, Component> styledFunc, String statPrefix) {
        String stat = id.substring(id.lastIndexOf('_') + 1);
        Component translatedStat = Component.translatable(statPrefix + stat);
        return styledFunc.apply(Component.translatable(key, translatedStat.getString()));
    }


    private static Component resolveResource(String target) {
        Language lang = Language.getInstance();
        if (lang.has("block.minecraft." + target))
            return Component.translatable("block.minecraft." + target);
        if (lang.has("item.minecraft." + target))
            return Component.translatable("item.minecraft." + target);
        return Component.literal(target);
    }

    private static Component resolveEntity(String name) {
        Language lang = Language.getInstance();
        if (lang.has("entity.minecraft." + name))
            return Component.translatable("entity.minecraft." + name);
        return Component.literal(name);
    }

    private static Component resolveColor(String key) {
        Language lang = Language.getInstance();
        if (lang.has("color.minecraft." + key))
            return Component.translatable("color.minecraft." + key);
        return Component.literal(key);
    }


    public Optional<MineboxStat> getStat(String statName) {
        return mbxStats == null
                ? Optional.empty()
                : Optional.ofNullable(mbxStats.get(statName));
    }

    private static Component styled(Component base, String rarity) {
        return base.copy().withStyle(style ->
                style.withColor(RaritiesUtils.getRarityColor(rarity.toLowerCase()).getRGB())
                        .withBold(true)
        );
    }

    public static Component getColoredStatName(String stat) {
        String key = stat.toLowerCase();
        MutableComponent text = switch (key) {
            case "mbx.stats.health" -> Component.literal("❤ ").append(Component.translatable("mbx.stats.health"));
            case "mbx.stats.strength" -> Component.literal("₪ ").append(Component.translatable("mbx.stats.strength"));
            case "mbx.stats.agility" -> Component.literal("☄ ").append(Component.translatable("mbx.stats.agility"));
            case "mbx.stats.intelligence" -> Component.literal("🔥 ").append(Component.translatable("mbx.stats.intelligence"));
            case "mbx.stats.wisdom" -> Component.literal("☽ ").append(Component.translatable("mbx.stats.wisdom"));
            case "mbx.stats.luck" -> Component.literal("🌊 ").append(Component.translatable("mbx.stats.luck"));
            case "mbx.stats.fortune" -> Component.literal("🔱 ").append(Component.translatable("mbx.stats.fortune"));
            case "mbx.stats.defense" -> Component.literal("🛡 ").append(Component.translatable("mbx.stats.defense"));
            default -> Component.literal(stat);
        };

        return switch (key) {
            case "mbx.stats.health" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE24A63)));
            case "mbx.stats.strength" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xA55F26)));
            case "mbx.stats.agility" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x6BC047)));
            case "mbx.stats.intelligence" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE24A2E)));
            case "mbx.stats.defense" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x1F8ECD)));
            case "mbx.stats.wisdom" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9457D3)));
            case "mbx.stats.luck" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x3D84A8)));
            case "mbx.stats.fortune" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xEC8C2E)));
            default -> text.setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
        };
    }


}
