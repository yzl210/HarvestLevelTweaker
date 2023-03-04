package cn.leomc.hltweaker.config;

import cn.leomc.hltweaker.HLTTier;
import cn.leomc.hltweaker.HarvestLevelTweaker;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class HLTConfig {

    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final Builder COMMON_BUILDER = new Builder();
    private static final ConfigValue<List<? extends String>> HARVEST_LEVELS;
    private static final ConfigValue<List<? extends String>> ITEM_HARVEST_LEVEL_OVERRIDES;
    private static final ForgeConfigSpec.BooleanValue ENABLE_CUSTOM_VANILLA_LEVEL_NAMES;

    private static Map<Integer, HLTTier> harvest_levels;
    private static List<HLTTier> harvest_levels_list;
    private static List<HLTTier> reversed_harvest_levels_list;

    private static Map<ResourceLocation, Pair<List<TagKey<Block>>, Tier>> item_harvest_level_overrides;

    static {
        HARVEST_LEVELS = COMMON_BUILDER
                .comment("Define new harvest levels here, \"<id>,<level>\" (level start from 5, 0-4 are vanilla levels), example: \"cobalt,5")
                .defineList("harvest_levels", List.of(), s -> s instanceof String);
        ITEM_HARVEST_LEVEL_OVERRIDES = COMMON_BUILDER
                .comment("Override item harvest level and tool type here, \"<item id>,<mineable tag>+<another mineable tag>,<level id>\", example: \"minecraft:wooden_pickaxe,minecraft:mineable/pickaxe+minecraft:mineable/axe+minecraft:mineable/shovel,minecraft:netherite\"")
                .defineList("item_harvest_level_overrides", List.of(), s -> s instanceof String);
        ENABLE_CUSTOM_VANILLA_LEVEL_NAMES = COMMON_BUILDER
                .comment("Enable custom name support for vanilla levels")
                .define("enable_custom_vanilla_names", false);
        COMMON_CONFIG = COMMON_BUILDER.build();
    }


    public static void loadLevels() {
        harvest_levels = new Int2ObjectRBTreeMap<>();
        for (String s : HARVEST_LEVELS.get()) {
            String[] split = s.split(",");
            HLTTier tier = new HLTTier(split[0], Integer.parseInt(split[1]));
            harvest_levels.put(tier.getLevel(), tier);
        }
        HLTTier last = null;
        for (HLTTier tier : harvest_levels.values()) {
            TierSortingRegistry.registerTier(tier, tier.getId(), List.of(last == null ? Tiers.NETHERITE : last), Collections.emptyList());
            last = tier;
        }
        harvest_levels_list = new ArrayList<>(harvest_levels.values());
        reversed_harvest_levels_list = Lists.reverse(harvest_levels_list);
        HarvestLevelTweaker.LOGGER.info("Loaded Levels " + harvest_levels);
    }

    public static void loadOverrides() {
        item_harvest_level_overrides = new HashMap<>();
        for (String s : ITEM_HARVEST_LEVEL_OVERRIDES.get()) {
            String[] split = s.split(",");
            List<TagKey<Block>> tags = new ArrayList<>();
            for (String tag : split[1].split("\\+"))
                tags.add(TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(tag)));
            item_harvest_level_overrides.put(new ResourceLocation(split[0]),
                    ObjectObjectImmutablePair.of(tags, TierSortingRegistry.byName(new ResourceLocation(split[2]))));
        }
        HarvestLevelTweaker.LOGGER.info("Loaded overrides " + item_harvest_level_overrides);
    }

    public static Map<Integer, HLTTier> getHarvestLevels() {
        if (harvest_levels == null)
            loadLevels();
        return harvest_levels;
    }

    public static List<HLTTier> getLevelsList() {
        if (harvest_levels_list == null)
            loadLevels();
        return harvest_levels_list;
    }

    public static List<HLTTier> getReversedLevelsList() {
        if (reversed_harvest_levels_list == null)
            loadLevels();
        return reversed_harvest_levels_list;
    }

    public static Map<ResourceLocation, Pair<List<TagKey<Block>>, Tier>> getItemOverrides() {
        if (item_harvest_level_overrides == null)
            loadOverrides();
        return item_harvest_level_overrides;
    }

    public static boolean isCustomVanillaLevelNamesEnabled() {
        return ENABLE_CUSTOM_VANILLA_LEVEL_NAMES.get();
    }

    public static void loadConfig() {
        CommentedFileConfig config = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(HarvestLevelTweaker.MOD_ID + "-common.toml")).build();
        config.load();
        COMMON_CONFIG.setConfig(config);
        loadLevels();
    }

}
