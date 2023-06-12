package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.Utils;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.providers.HarvestInfoTools;
import mcjty.theoneprobe.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HarvestInfoTools.class)
public abstract class TOPHarvestInfoToolsMixin {

    @Shadow(remap = false)
    @Final
    private static ResourceLocation ICONS;

    @Shadow(remap = false)
    private static String getTools(BlockState state) {
        return "";
    }

    @Shadow(remap = false)
    private static String getLevels(BlockState state) {
        return "";
    }

    @Inject(
            at = @At("HEAD"),
            method = "showHarvestInfo",
            remap = false,
            cancellable = true
    )
    private static void showHarvestInfo(IProbeInfo probeInfo, Level world, BlockPos pos, Block block, BlockState blockState, Player player, CallbackInfo info) {
        info.cancel();
        ItemStack stack = player.getMainHandItem();
        boolean harvestable = Utils.canHarvestBlock(blockState, player);
        String tools = getTools(blockState);
        Tier tier = Utils.getHarvestLevel(blockState);
        String levels = getLevels(blockState);

        boolean v = Config.harvestStyleVanilla.get();
        int offs = v ? 16 : 0;
        int dim = v ? 13 : 16;

        ILayoutStyle alignment = probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER);
        IIconStyle iconStyle = probeInfo.defaultIconStyle().width(v ? 18 : 20).height(v ? 14 : 16).textureWidth(32).textureHeight(32);
        IProbeInfo horizontal = probeInfo.horizontal(alignment);
        if (harvestable) {
            horizontal.icon(ICONS, 0, offs, dim, dim, iconStyle)
                    .text(CompoundText.create().style(TextStyleClass.OK).text((tools.isEmpty() ? "No tool" : tools)));
        } else {
            if (levels.isEmpty() && tier == null)
                horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle)
                        .text(CompoundText.create().style(TextStyleClass.WARNING).text((tools.isEmpty() ? "No tool" : tools)));
            else {
                MutableComponent component = Component.literal(tools.isEmpty() ? "No tool" : tools);
                if(blockState.requiresCorrectToolForDrops()) {
                    component.append(" (");
                    component.append(Component.translatable("text.hltweaker.level"));
                    component.append(" ");
                    component.append(tier == null ? Component.literal(levels) : Utils.getTierName(tier));
                    component.append(")");
                }

                horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle)
                        .text(CompoundText.create().style(TextStyleClass.WARNING)
                                .text(component));
            }
        }
    }
}
