package cn.leomc.hltweaker;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.TierSortingRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class HLTTierRegistry {

    public static Tier registerTierAndSetHarvestLevel(ResourceLocation id, int level, int uses, float speed, float attackDamageBonus, int enchantmentValue, @Nonnull Supplier<Ingredient> repairIngredient) {
        HLTTier hltTier = HarvestLevelTweaker.getManager().getTier(level);
        if (hltTier == null)
            throw new IllegalArgumentException("Level not registered in Harvest Level Tweaker!");
        EquivalentTier tier = new EquivalentTier(hltTier, uses, speed, attackDamageBonus, enchantmentValue, repairIngredient);
        setTierHarvestLevel(hltTier, id, tier);
        return tier;
    }

    public static void setTierHarvestLevel(HLTTier hltTier, ResourceLocation id, Tier tier) {
        IntList levels = IntList.of(HarvestLevelTweaker.getManager().getLevels().toIntArray());
        int index = levels.indexOf(hltTier.getLevel());

        TierSortingRegistry.registerTier(tier, id, List.of(hltTier),
                index < 0 || index + 1 >= levels.size() ? List.of() : List.of(HarvestLevelTweaker.getManager().getTiers().toArray()[index + 1]));
    }

    public static void setTierHarvestLevel(int level, ResourceLocation id, Tier tier) {
        HLTTier hltTier = HarvestLevelTweaker.getManager().getTier(level);
        if (hltTier == null)
            throw new IllegalArgumentException("Level not registered in Harvest Level Tweaker!");
        setTierHarvestLevel(hltTier, id, tier);
    }
}
