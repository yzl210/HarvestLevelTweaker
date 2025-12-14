package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.Utils;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TieredItem.class)
public abstract class TieredItemMixin {
    @Inject(
            method = "getTier",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetTier(CallbackInfoReturnable<Tier> cir) {
        var object = (TieredItem) (Object) this;
        var highestTier = Utils.getHighestTier(object);
        if (highestTier != null) {
            cir.setReturnValue(highestTier);
        }
    }
}
