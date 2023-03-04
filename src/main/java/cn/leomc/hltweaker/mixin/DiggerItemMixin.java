package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.Utils;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiggerItem.class)
public class DiggerItemMixin {

    @Inject(
            method = "isCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCheckHarvest(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isItemOverridden((Item) (Object) this))
            cir.setReturnValue(Utils.checkItemOverrides((Item) (Object) this, state));
    }

    @Inject(
            method = "isCorrectToolForDrops(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onCheckHarvest(ItemStack stack, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isItemOverridden(stack.getItem()))
            cir.setReturnValue(Utils.checkItemOverrides(stack.getItem(), state));
    }

}
