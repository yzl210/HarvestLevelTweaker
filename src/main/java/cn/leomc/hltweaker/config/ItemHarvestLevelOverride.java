package cn.leomc.hltweaker.config;

import cn.leomc.hltweaker.HarvestLevelTweaker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ItemHarvestLevelOverride {
    private final ResourceLocation item;
    private final Map<TagKey<Block>, ResourceLocation> overrides = new HashMap<>();
    private final Map<TagKey<Block>, Tier> cachedTiers = new HashMap<>();


    public ItemHarvestLevelOverride(ResourceLocation item) {
        this.item = item;
    }

    public void add(TagKey<Block> mineableTag, ResourceLocation tier) {
        overrides.put(mineableTag, tier);
    }

    public ResourceLocation item() {
        return item;
    }

    public Set<TagKey<Block>> mineableTags() {
        return overrides.keySet();
    }

    public Map<TagKey<Block>, ResourceLocation> getOverrides() {
        return overrides;
    }

    public Tier getTier(TagKey<Block> tag) {
        if (!overrides.containsKey(tag))
            return null;

        return cachedTiers.computeIfAbsent(tag, t -> {
            ResourceLocation tierId = overrides.get(t);
            Tier tier = TierSortingRegistry.byName(tierId);
            if (tier == null)
                HarvestLevelTweaker.LOGGER.warn("Tier {} not found in item override {}", tierId, item);
            return tier;
        });
    }
}