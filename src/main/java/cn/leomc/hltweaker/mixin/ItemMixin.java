package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.Utils;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(
            method = "isCorrectToolForDrops",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCheckHarvest(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isItemOverridden((Item) (Object) this))
            cir.setReturnValue(Utils.checkItemOverrides((Item) (Object) this, state));
    }
}
