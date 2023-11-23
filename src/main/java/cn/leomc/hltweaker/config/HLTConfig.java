package cn.leomc.hltweaker.config;

import cn.leomc.hltweaker.Utils;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class HLTConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;
    private static final Builder CLIENT_BUILDER = new Builder();
    private static final ForgeConfigSpec.BooleanValue SHOW_HARVEST_LEVEL_NAME;
    private static final ForgeConfigSpec.BooleanValue SHOW_TOOL_HARVEST_LEVEL;
    private static final ForgeConfigSpec.BooleanValue SHOW_BLOCK_HARVEST_LEVEL;
    private static final Map<String, ForgeConfigSpec.ConfigValue<String>> VANILLA_LEVEL_COLORS = new LinkedHashMap<>();

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
        SHOW_BLOCK_HARVEST_LEVEL = CLIENT_BUILDER
                .comment("Show block required harvest level in tooltips when shift is held or advanced tooltips are enabled")
                .define("show_block_harvest_levels", true);
        SHOW_TOOL_HARVEST_LEVEL = CLIENT_BUILDER
                .comment("Show tool harvest level in tooltips when shift is held or advanced tooltips are enabled")
                .define("show_tool_harvest_levels", true);

        CLIENT_BUILDER.comment("Colors for vanilla harvest levels, in hex format or color name");
        CLIENT_BUILDER.push("vanilla_level_colors");
        DEFAULT_COLORS.forEach((key, value) -> VANILLA_LEVEL_COLORS.put(key, CLIENT_BUILDER.define(key, value)));
        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static boolean showHarvestLevelName() {
        return SHOW_HARVEST_LEVEL_NAME.get();
    }

    public static boolean showToolHarvestLevel() {
        return SHOW_TOOL_HARVEST_LEVEL.get();
    }

    public static boolean showBlockHarvestLevel() {
        return SHOW_BLOCK_HARVEST_LEVEL.get();
    }

    public static TextColor getColor(Tiers tier) {
        return TextColor.parseColor(VANILLA_LEVEL_COLORS.get(tier.name()).get());
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
}
