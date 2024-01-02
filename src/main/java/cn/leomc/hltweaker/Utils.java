package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.HLTConfig;
import cn.leomc.hltweaker.config.ItemHarvestLevelOverride;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final Map<BlockState, Tier> HARVEST_LEVEL_CACHE = new HashMap<>();
    private static final Table<Item, BlockState, Boolean> ITEM_OVERRIDE_CACHE = HashBasedTable.create();
    private static final Map<Tier, MutableComponent> TIER_NAME_CACHE = new HashMap<>();

    public static void clearCache() {
        TIER_NAME_CACHE.clear();
        ITEM_OVERRIDE_CACHE.clear();
        HARVEST_LEVEL_CACHE.clear();
    }

    public static List<Tier> getTiers() {
        return new ImmutableList.Builder<Tier>()
                .add(Tiers.values())
                .addAll(HarvestLevelTweaker.getManager().getTiers())
                .build();
    }

    public static Tier getHarvestLevel(BlockState state) {
        return HARVEST_LEVEL_CACHE.computeIfAbsent(state, s -> {
            for (Tier tier : getTiers())
                if (TierSortingRegistry.isCorrectTierForDrops(tier, s))
                    return tier;
            return null;
        });
    }

    public static boolean isItemOverridden(Item item) {
        return HarvestLevelTweaker.getManager().getOverride(ForgeRegistries.ITEMS.getKey(item)) != null;
    }

    public static boolean checkItemOverrides(Item item, BlockState state) {
        Boolean b = ITEM_OVERRIDE_CACHE.get(item, state);
        if (b != null)
            return b;

        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        if (!isItemOverridden(item))
            return false;

        ItemHarvestLevelOverride override = HarvestLevelTweaker.getManager().getOverride(rl);

        for (TagKey<Block> tag : override.mineableTags())
            if (state.is(tag) && TierSortingRegistry.isCorrectTierForDrops(override.getTier(tag), state)) {
                ITEM_OVERRIDE_CACHE.put(item, state, true);
                return true;
            }
        ITEM_OVERRIDE_CACHE.put(item, state, false);
        return false;
    }

    public static boolean canHarvestBlock(BlockState state, Player player) {
        return !state.requiresCorrectToolForDrops()
                || Utils.checkItemOverrides(player.getMainHandItem().getItem(), state)
                || player.hasCorrectToolForDrops(state)
                || player.getMainHandItem().isCorrectToolForDrops(state);
    }

    public static MutableComponent getTierName(Tier tier) {
        return TIER_NAME_CACHE.computeIfAbsent(tier, t -> {
            if (tier instanceof HLTTier hltTier)
                return hltTier.getName();
            ResourceLocation rl = TierSortingRegistry.getName(tier);
            if (rl == null)
                return Component.literal(String.valueOf(tier.getLevel()));

            return Component.translatable("text.hltweaker.level." + rl.getNamespace() + "." + rl.getPath())
                    .withStyle(style -> style.withColor(getTierColor(tier)));
        }).copy();
    }

    public static TextColor getTierColor(Tier tier) {
        if (tier instanceof HLTTier hltTier)
            return hltTier.getColor();
        if (tier instanceof Tiers tiers)
            return HLTConfig.getColor(tiers);
        return TextColor.fromLegacyFormat(ChatFormatting.WHITE);
    }

    public static MutableComponent getMineableName(TagKey<Block> mineableTag) {
        ResourceLocation rl = mineableTag.location();
        String key = "text.hltweaker.tool." + rl.getNamespace() + "." + rl.getPath().replace("mineable/", "");
        return Component.translatable(key);
    }

    public static ItemStack getItemStack(String input) {
        try {
            ItemParser.ItemResult result = ItemParser.parseForItem(HolderLookup.forRegistry(Registry.ITEM), new StringReader(input));
            return new ItemStack(result.item().value(), 1, result.nbt());
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException("Could not parse ItemStack: " + input, e);
        }
    }

}
