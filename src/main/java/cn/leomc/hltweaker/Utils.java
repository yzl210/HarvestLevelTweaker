package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.HLTConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public final static List<Tier> TIERS = new ArrayList<>();
    static {
        TIERS.addAll(List.of(Tiers.values()));
        TIERS.addAll(HLTConfig.getLevelsList());
    }

    public static Tier getHarvestLevel(BlockState state) {
        if(!state.requiresCorrectToolForDrops())
            return null;
        for (Tier tier : TIERS)
            if (TierSortingRegistry.isCorrectTierForDrops(tier, state))
                return tier;
        return null;
    }

    public static boolean isItemOverridden(Item item) {
        return HLTConfig.getItemOverrides().containsKey(item.getRegistryName());
    }

    public static boolean checkItemOverrides(Item item, BlockState state) {
        ResourceLocation rl = item.getRegistryName();
        return isItemOverridden(item)
                && HLTConfig.getItemOverrides().get(rl).left().stream().anyMatch(state::is)
                && TierSortingRegistry
                .isCorrectTierForDrops(HLTConfig.getItemOverrides().get(rl).right(), state);
    }

    public static boolean canHarvestBlock(BlockState state, Player player) {
        return !state.requiresCorrectToolForDrops()
                || Utils.checkItemOverrides(player.getMainHandItem().getItem(), state)
                || player.hasCorrectToolForDrops(state)
                || player.getMainHandItem().isCorrectToolForDrops(state);
    }
    public static Component getTierName(Tier tier) {
        if (tier instanceof HLTTier hltTier)
            return hltTier.getNameComponent();
        else {
            ResourceLocation rl = TierSortingRegistry.getName(tier);
            if (rl == null)
                return new TextComponent(String.valueOf(tier.getLevel()));
            if(!HLTConfig.isCustomVanillaLevelNamesEnabled() && rl.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                return new TranslatableComponent("text.hltweaker.level." + "default_" + rl.getNamespace() + "." + rl.getPath());
            return new TranslatableComponent("text.hltweaker.level." + rl.getNamespace() + "." + rl.getPath());
        }
    }
}
