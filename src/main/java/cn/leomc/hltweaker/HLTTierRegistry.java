package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.HLTConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.TierSortingRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class HLTTierRegistry {

    public static Tier registerTierAndSetHarvestLevel(ResourceLocation id, int level, int uses, float speed, float attackDamageBonus, int enchantmentValue, @Nonnull Supplier<Ingredient> repairIngredient) {
        HLTTier hltTier = HLTConfig.getHarvestLevels().get(level);
        if (hltTier == null)
            throw new IllegalArgumentException("Level not registered in Harvest Level Tweaker!");
        EquivalentTier tier = new EquivalentTier(hltTier, uses, speed, attackDamageBonus, enchantmentValue, repairIngredient);
        setTierHarvestLevel(hltTier, id, tier);
        return tier;
    }

    public static void setTierHarvestLevel(HLTTier hltTier, ResourceLocation id, Tier tier) {
        List<Integer> entries = HLTConfig.getHarvestLevels().keySet().stream().toList();
        int index = entries.indexOf(hltTier.getLevel());
        HarvestLevelTweaker.LOGGER.info("Registered harvest level " + hltTier + " for " + tier);
        TierSortingRegistry.registerTier(tier, id, List.of(hltTier),
                index < 0 || index + 1 >= entries.size() ? List.of() : List.of(HLTConfig.getHarvestLevels().values().toArray()[index + 1]));
    }

    public static void setTierHarvestLevel(int level, ResourceLocation id, Tier tier) {
        HLTTier hltTier = HLTConfig.getHarvestLevels().get(level);
        if (hltTier == null)
            throw new IllegalArgumentException("Level not registered in Harvest Level Tweaker!");
        setTierHarvestLevel(hltTier, id, tier);
    }


    private static MutableComponent replace(String original, List<Pair<String, Component>> replacements) {
        String[] split = null;
        Component replacement = null;
        for (Pair<String, Component> pair : replacements) {
            String[] sp = original.split(pair.getFirst(), 2);
            if (sp.length > 1) {
                split = sp;
                replacement = pair.getSecond();
                break;
            }
        }
        if (split == null)
            return Component.literal(original);

        MutableComponent left = replace(split[0], replacements);
        left.append(replacement);
        left.append(replace(split[1], replacements));

        return left;
    }
}
