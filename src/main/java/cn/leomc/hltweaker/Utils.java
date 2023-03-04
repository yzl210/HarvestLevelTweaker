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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public final static ListView<Tier> TIERS = new ListView<>(List.of(Tiers.values()), HLTConfig.getLevelsList());

    public static Tier getHarvestLevel(BlockState state) {
        for (Tier tier : TIERS) {
            if (TierSortingRegistry.isCorrectTierForDrops(tier, state))
                return tier;
        }
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
            return new TranslatableComponent("text.hltweaker.level." + rl.getNamespace() + "." + rl.getPath());
        }
    }

    private static class ListView<T> extends AbstractList<T> {

        private final List<? extends T>[] lists;
        private final int size;

        @SafeVarargs
        public ListView(List<? extends T>... lists) {
            this.lists = lists;
            this.size = Arrays.stream(lists).mapToInt(List::size).sum();
        }

        @Override
        public T get(int index) {
            int i = index;
            for (List<? extends T> list : lists) {
                if (i >= list.size())
                    i -= list.size();
                else
                    return list.get(index);
            }
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        @Override
        public int size() {
            return size;
        }
    }

}
