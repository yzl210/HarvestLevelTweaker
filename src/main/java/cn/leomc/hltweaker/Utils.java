package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.HLTConfig;
import com.google.common.collect.Lists;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

public class Utils {
    public static List<Tier> getTiersHigherThan(Tier tier) {
        if (!TierSortingRegistry.isTierSorted(tier))
            return List.of();
        return Lists.reverse(TierSortingRegistry.getSortedTiers()).stream().takeWhile(t -> t != tier).toList();
    }

    public static HLTTier getFirstTier() {
        return HLTConfig.getHarvestLevels().get(0);
    }

}
