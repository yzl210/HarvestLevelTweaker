package cn.leomc.hltweaker.config;

import cn.leomc.hltweaker.HLTTier;
import cn.leomc.hltweaker.HarvestLevelTweaker;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HLTConfig {

    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final Builder COMMON_BUILDER = new Builder();
    private static final ConfigValue<List<? extends String>> HARVEST_LEVELS;

    private static Map<Integer, HLTTier> harvest_levels;
    private static List<HLTTier> harvest_levels_list;
    private static List<HLTTier> reversed_harvest_levels_list;

    static {
        HARVEST_LEVELS = COMMON_BUILDER
                .comment("Define new harvest levels here, <id>,<level> (level start from 5, 0-4 are vanilla levels), example: cobalt,5")
                .defineList("harvest_levels",
                        List.of(), s -> s instanceof String);
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

    public static Map<Integer, HLTTier> getHarvestLevels() {
        if (harvest_levels == null)
            loadLevels();
        return harvest_levels;
    }

    public static List<HLTTier> getLevelsList() {
        return harvest_levels_list;
    }

    public static List<HLTTier> getReversedLevelsList() {
        return reversed_harvest_levels_list;
    }

    public static void loadConfig() {
        CommentedFileConfig config = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(HarvestLevelTweaker.MOD_ID + "-common.toml")).build();
        config.load();
        COMMON_CONFIG.setConfig(config);
        loadLevels();
    }

}
