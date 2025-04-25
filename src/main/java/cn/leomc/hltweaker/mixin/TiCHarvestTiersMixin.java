package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.HLTTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Tier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.utils.HarvestTiers;

@Mixin(HarvestTiers.class)
public class TiCHarvestTiersMixin {

    @Inject(
            method = "getName",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onGetName(Tier tier, CallbackInfoReturnable<Component> cir) {
        if (tier instanceof HLTTier hltTier)
            cir.setReturnValue(hltTier.getName());
    }

}
