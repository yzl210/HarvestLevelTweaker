package cn.leomc.hltweaker.config;

import cn.leomc.hltweaker.HLTTier;
import cn.leomc.hltweaker.HarvestLevelTweaker;
import cn.leomc.hltweaker.Utils;
import cn.leomc.hltweaker.mixin.TierSortingRegistryAccessor;
import com.google.gson.*;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class HarvestLevelManager {

    private static final List<Tier> VANILLA_TIERS = List.of(Tiers.WOOD, Tiers.GOLD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE);

    private final Path path;
    private final Map<ResourceLocation, HLTTier> harvestLevels = new HashMap<>();
    private final Map<ResourceLocation, ItemHarvestLevelOverride> overrides = new HashMap<>();

    public HarvestLevelManager(Path path) {
        this.path = path;
    }

    public void load() {
        if (!harvestLevels.isEmpty())
            return;

        try {
            loadHarvestLevels(path.resolve("levels"));
            loadOverrides(path.resolve("item_harvest_level_overrides.json"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load harvest levels, please check your configuration files!", e);
        }

        harvestLevels.forEach((id, tier) -> TierSortingRegistry.registerTier(tier, id, List.of(), List.of()));

        Utils.clearCache();
    }

    public void loadOrdering() {
        List<Pair<ResourceLocation, Tier>> tierOrdering = new ArrayList<>();
        List<Tier> missingTiers = new ArrayList<>(VANILLA_TIERS);
        missingTiers.addAll(harvestLevels.values());

        Path orderingFile = path.resolve("ordering.json");
        if (Files.exists(orderingFile)) {
            JsonArray array;
            try {
                array = JsonParser.parseString(Files.readString(orderingFile)).getAsJsonArray();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read tier ordering", e);
            }

            for (JsonElement element : array) {
                ResourceLocation id = new ResourceLocation(element.getAsString());
                Tier tier = TierSortingRegistry.byName(id);
                if (tier == null)
                    throw new IllegalArgumentException("Failed to load tier ordering! Tier not found: " + element.getAsString());
                missingTiers.remove(tier);
                tierOrdering.add(new ObjectObjectImmutablePair<>(id, tier));
            }
        }

        if (!missingTiers.isEmpty()) {
            missingTiers.forEach(tier -> tierOrdering.add(new ObjectObjectImmutablePair<>(TierSortingRegistry.getName(tier), tier)));
            JsonArray array = new JsonArray();
            tierOrdering.stream().map(Pair::left).forEach(id -> array.add(id.toString()));

            try {
                Files.writeString(orderingFile, new GsonBuilder().setPrettyPrinting().create().toJson(array), StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write tier ordering", e);
            }
        }

        TierSortingRegistryAccessor.getVanillaEdges().forEach((v1, v2) -> TierSortingRegistryAccessor.getEdges().remove(v1, v2));
        for (int i = 0; i < tierOrdering.size(); i++) {
            Pair<ResourceLocation, Tier> pair = tierOrdering.get(i);
            List<Object> before = i == 0 ? List.of() : List.of(tierOrdering.get(i - 1).left());
            List<Object> after = i == tierOrdering.size() - 1 ? List.of() : List.of(tierOrdering.get(i + 1).left());
            TierSortingRegistryAccessor.processTier(pair.right(), pair.left(), before, after);
        }
        TierSortingRegistryAccessor.recalculateItemTiers();
        Utils.clearCache();
    }

    private void loadHarvestLevels(Path folder) throws IOException {
        Files.createDirectories(folder);
        try (Stream<Path> stream = Files.walk(folder, 2)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .forEach(this::loadHarvestLevel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHarvestLevel(Path file) {
        try {
            JsonObject object = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            ResourceLocation id = new ResourceLocation(HarvestLevelTweaker.MOD_ID, file.getFileName().toString().replace(".json", ""));
            if (harvestLevels.containsKey(id))
                throw new IllegalStateException("Duplicate harvest level: " + id);

            Integer level = Optional.ofNullable(object.get("level"))
                    .map(JsonElement::getAsInt)
                    .orElse(null);

            TextColor color = Optional.ofNullable(object.get("color"))
                    .map(e -> TextColor.parseColor(e.getAsString()))
                    .orElse(null);

            HLTTier tier = new HLTTier(id.getPath(), level, color);

            if (object.has("icons")) {
                object.getAsJsonObject("icons").entrySet().forEach(entry -> {
                    TagKey<Block> mineableTag = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(entry.getKey()));
                    ItemStack icon = Utils.getItemStack(entry.getValue().getAsString());
                    tier.setIcon(mineableTag, icon);
                });
            }

            harvestLevels.put(id, tier);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load harvest level from " + file, e);
        }
    }

    private void loadOverrides(Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(file, "{}");
            return;
        }

        JsonObject object = JsonParser.parseString(Files.readString(file)).getAsJsonObject();

        object.entrySet().forEach(entry -> {
            ResourceLocation item = new ResourceLocation(entry.getKey());

            ItemHarvestLevelOverride override = new ItemHarvestLevelOverride(item);
            entry.getValue().getAsJsonObject().entrySet().forEach(e -> {
                TagKey<Block> mineableTag = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(e.getKey()));
                override.add(mineableTag, tierId(e.getValue().getAsString()));
            });

            overrides.put(item, override);
        });
    }

    private ResourceLocation tierId(String tierString) {
        if (!tierString.contains(":"))
            tierString = HarvestLevelTweaker.MOD_ID + ":" + tierString;
        return new ResourceLocation(tierString);
    }

    public Collection<HLTTier> getTiers() {
        return Collections.unmodifiableCollection(harvestLevels.values());
    }

    public ItemHarvestLevelOverride getOverride(ResourceLocation item) {
        return overrides.get(item);
    }

    public Collection<ItemHarvestLevelOverride> getOverrides() {
        return Collections.unmodifiableCollection(overrides.values());
    }
}
