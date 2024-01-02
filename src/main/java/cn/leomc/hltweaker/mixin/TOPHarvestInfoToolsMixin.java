package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.Utils;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.providers.HarvestInfoTools;
import mcjty.theoneprobe.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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
                        .text(warning(tools.isEmpty() ? "No tool" : tools));
            else {
                horizontal.icon(ICONS, 16, offs, dim, dim, iconStyle);

                horizontal.text(warning(tools.isEmpty() ? "No tool" : tools));
                if (blockState.requiresCorrectToolForDrops()) {
                    horizontal.text(warning(" ("));
                    horizontal.text(CompoundText.create().warning("text.hltweaker.level"));
                    horizontal.text(" ");
                    if (tier == null)
                        horizontal.text(warning(levels));
                    else
                        horizontal.mcText(Utils.getTierName(tier));
                    horizontal.text(warning(")"));
                }

            }
        }
    }

    private static CompoundText warning(String text) {
        return CompoundText.create().style(TextStyleClass.WARNING).text(text);
    }
}
