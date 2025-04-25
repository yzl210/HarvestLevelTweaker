package cn.leomc.hltweaker.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.TierSortingRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = TierSortingRegistry.class, remap = false)
public interface TierSortingRegistryAccessor {

    @Accessor("edges")
    static Multimap<ResourceLocation, ResourceLocation> getEdges() {
        return null;
    }

    @Accessor("vanillaEdges")
    static Multimap<ResourceLocation, ResourceLocation> getVanillaEdges() {
        return null;
    }

    @Invoker("processTier")
    static void processTier(Tier tier, ResourceLocation name, List<Object> afters, List<Object> befores) {
    }

    @Invoker("recalculateItemTiers")
    static void recalculateItemTiers() {
    }
}
