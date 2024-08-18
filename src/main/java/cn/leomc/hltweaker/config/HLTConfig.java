package cn.leomc.hltweaker.config;

import cn.leomc.hltweaker.Utils;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HLTConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;
    private static final Builder CLIENT_BUILDER = new Builder();
    private static final ForgeConfigSpec.BooleanValue SHOW_HARVEST_LEVEL_NAME;
    private static final ForgeConfigSpec.BooleanValue SHOW_CLOSEST_LOWER_VANILLA_HLT_LEVEL;
    private static final ForgeConfigSpec.EnumValue<DisplayMode> TOOL_HARVEST_LEVEL_DISPLAY_MODE;
    private static final ForgeConfigSpec.EnumValue<DisplayMode> BLOCK_HARVEST_LEVEL_DISPLAY_MODE;
    private static final Map<String, ForgeConfigSpec.ConfigValue<String>> VANILLA_LEVEL_COLORS = new LinkedHashMap<>();
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOD_LEVEL_COLORS;

    private static final Map<String, String> DEFAULT_COLORS = Map.of(
            "WOOD", "#8b5a2b",
            "STONE", "#808080",
            "IRON", "#cecaca",
            "DIAMOND", "#2cbaa8",
            "GOLD", "#ecd93f",
            "NETHERITE", "#443a3b"
    );

    static {
        SHOW_HARVEST_LEVEL_NAME = CLIENT_BUILDER
                .comment("Show harvest level name next to the tool icon")
                .define("show_harvest_level_name", true);
        SHOW_CLOSEST_LOWER_VANILLA_HLT_LEVEL = CLIENT_BUILDER
                .comment("Show the closest lower vanilla or HLT harvest level instead of the original level")
                .define("show_closest_lower_vanilla_hlt_level", true);
        BLOCK_HARVEST_LEVEL_DISPLAY_MODE = CLIENT_BUILDER
                .comment("Show block required harvest level in tooltips. NEVER = never shows, ADVANCED = shows when shift is held or advanced tooltips are enabled, ALWAYS = always shows.")
                .defineEnum("block_harvest_levels_display_mode", DisplayMode.ADVANCED);
        TOOL_HARVEST_LEVEL_DISPLAY_MODE = CLIENT_BUILDER
                .comment("Show tool harvest level in tooltips. NEVER = never shows, ADVANCED = shows when shift is held or advanced tooltips are enabled, ALWAYS = always shows.")
                .defineEnum("tool_harvest_levels_display_mode", DisplayMode.ADVANCED);

        CLIENT_BUILDER.comment("Colors for vanilla harvest levels, in hex format or color name");
        CLIENT_BUILDER.push("vanilla_level_colors");
        DEFAULT_COLORS.forEach((key, value) -> VANILLA_LEVEL_COLORS.put(key, CLIENT_BUILDER.define(key, value)));
        CLIENT_BUILDER.pop();
        MOD_LEVEL_COLORS = CLIENT_BUILDER
                .comment("Colors for harvest levels from other mods. Format: modid:level=color. Example: examplemod:custom_level=#ffffff")
                .defineList("mod_level_colors", List.of(), o -> {
                    if (!(o instanceof String s))
                        return false;
                    String[] split = s.split("=");
                    if (split.length != 2)
                        return false;
                    return ResourceLocation.tryParse(split[0]) != null && split[1].startsWith("#") && split[1].length() == 7;
                });

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static boolean showHarvestLevelName() {
        return SHOW_HARVEST_LEVEL_NAME.get();
    }

    public static boolean showClosestLowerVanillaHLTLevel() {
        return SHOW_CLOSEST_LOWER_VANILLA_HLT_LEVEL.get();
    }


    public static DisplayMode toolHarvestLevelDisplayMode() {
        return TOOL_HARVEST_LEVEL_DISPLAY_MODE.get();
    }

    public static DisplayMode blockHarvestLevelDisplayMode() {
        return BLOCK_HARVEST_LEVEL_DISPLAY_MODE.get();
    }

    public static TextColor getColor(Tiers tier) {
        return TextColor.parseColor(VANILLA_LEVEL_COLORS.get(tier.name()).get());
    }

    public static TextColor getColor(ResourceLocation id) {
        for (String s : MOD_LEVEL_COLORS.get()) {
            String[] split = s.split("=");
            if (Objects.equals(ResourceLocation.tryParse(split[0]), id))
                return TextColor.parseColor(split[1]);
        }
        return null;
    }

    public static void loadConfig(Path path) {
        try {
            if (Files.exists(path) && !Files.isDirectory(path))
                Files.delete(path);
            if (!Files.exists(path))
                Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create config file", e);
        }
        CommentedFileConfig clientConfig = CommentedFileConfig.builder(path.resolve("client.toml")).build();
        clientConfig.load();
        CLIENT_CONFIG.setConfig(clientConfig);
        Utils.clearCache();
    }

    public enum DisplayMode {
        NEVER,
        ADVANCED,
        ALWAYS;

        public boolean shouldShow(boolean isAdvanced, boolean isPopulating) {
            return this == ALWAYS || (this == ADVANCED && (isAdvanced || Screen.hasShiftDown() || isPopulating));
        }
    }
}
