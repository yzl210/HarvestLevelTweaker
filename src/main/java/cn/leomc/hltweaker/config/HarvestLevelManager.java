package cn.leomc.hltweaker.config;

import cn.leomc.hltweaker.HLTTier;
import cn.leomc.hltweaker.HarvestLevelTweaker;
import cn.leomc.hltweaker.Utils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class HarvestLevelManager {

    private final Path path;

    public HarvestLevelManager(Path path) {
        this.path = path;
    }

    private final Int2ObjectMap<HLTTier> harvestLevels = new Int2ObjectAVLTreeMap<>();
    private final Map<ResourceLocation, ItemHarvestLevelOverride> overrides = new HashMap<>();

    public void load() {
        if (!harvestLevels.isEmpty())
            return;

        try {
            loadHarvestLevels(path.resolve("levels"));
            loadOverrides(path.resolve("item_harvest_level_overrides.json"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load harvest levels, please check your configuration files!", e);
        }

        HLTTier last = null;
        for (HLTTier tier : harvestLevels.values()) {
            TierSortingRegistry.registerTier(tier, tier.getId(), List.of(last == null ? Tiers.NETHERITE : last), Collections.emptyList());
            last = tier;
        }

        Utils.clearCache();
    }

    private void loadHarvestLevels(Path folder) throws IOException {
        Files.createDirectories(folder);
        try (Stream<Path> stream = Files.walk(folder, 2)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(this::loadHarvestLevel)
                    .forEach(tier -> harvestLevels.put(tier.getLevel(), tier));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HLTTier loadHarvestLevel(Path file) {
        try {
            JsonObject object = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            String id = file.getFileName().toString().replace(".json", "");
            int level = object.get("level").getAsInt();
            if (harvestLevels.containsKey(level))
                throw new IllegalStateException("Duplicate harvest level: " + level + ". Same as " + harvestLevels.get(level).getId());
            TextColor color = Optional.ofNullable(object.get("color"))
                    .map(e -> TextColor.parseColor(e.getAsString()))
                    .orElse(null);

            HLTTier tier = new HLTTier(id, level, color);

            if (object.has("icons"))
                object.getAsJsonObject("icons").entrySet().forEach(entry -> {
                    TagKey<Block> mineableTag = TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(entry.getKey()));
                    ItemStack icon = Utils.getItemStack(entry.getValue().getAsString());
                    tier.setIcon(mineableTag, icon);
                });

            return tier;
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

                String tierString = e.getValue().getAsString();
                if (!tierString.contains(":"))
                    tierString = HarvestLevelTweaker.MOD_ID + ":" + tierString;

                ResourceLocation tier = new ResourceLocation(tierString);
                override.add(mineableTag, tier);
            });

            overrides.put(item, override);
        });
    }

    public HLTTier getTier(int level) {
        return harvestLevels.get(level);
    }

    public Collection<HLTTier> getTiers() {
        return Collections.unmodifiableCollection(harvestLevels.values());
    }

    public IntSet getLevels() {
        return IntSets.unmodifiable(harvestLevels.keySet());
    }

    public Int2ObjectMap<HLTTier> getMap() {
        return Int2ObjectMaps.unmodifiable(harvestLevels);
    }

    public ItemHarvestLevelOverride getOverride(ResourceLocation item) {
        return overrides.get(item);
    }

    public Collection<ItemHarvestLevelOverride> getOverrides() {
        return Collections.unmodifiableCollection(overrides.values());
    }
}
