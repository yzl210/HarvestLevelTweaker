package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.Utils;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import squeek.wthitharvestability.WailaHandler;

import java.util.ArrayList;
import java.util.List;

@Mixin(WailaHandler.class)
public abstract class WTHITWailaHandlerMixin {

    @Shadow(remap = false)
    @Final
    private static ResourceLocation LEVEL;

    @Shadow(remap = false)
    @Final
    private static ResourceLocation LEVEL_SNEAK;

    @Shadow(remap = false)
    @Final
    private static ResourceLocation LEVEL_NUM;

    @Shadow(remap = false)
    @Final
    private static ResourceLocation LEVEL_NUM_SNEAK;

    @Shadow(remap = false)
    @Final
    private static ResourceLocation MINIMAL;

    @Shadow(remap = false)
    @Final
    private static ResourceLocation MINIMAL_SEPARATOR;

    @Shadow(remap = false)
    public abstract void getHarvestability(List<Component> stringList, Player player, BlockState state, BlockPos pos, IPluginConfig config, boolean minimalLayout);


    @Inject(
            at = @At("RETURN"),
            method = "getHarvestability",
            remap = false
    )
    public void onGetHarvestability(List<Component> stringList, Player player, BlockState state, BlockPos pos, IPluginConfig config, boolean minimalLayout, CallbackInfo ci) {
        if (stringList.isEmpty())
            return;

        boolean isSneaking = player.isShiftKeyDown();
        boolean showHarvestLevel = config.getBoolean(LEVEL) && (!config.getBoolean(LEVEL_SNEAK) || isSneaking);
        boolean showHarvestLevelNum = config.getBoolean(LEVEL_NUM) && (!config.getBoolean(LEVEL_NUM_SNEAK) || isSneaking);

        Tier tier = Utils.getHarvestLevel(state);

        if (tier != null && (showHarvestLevel || showHarvestLevelNum)) {
            stringList.remove(stringList.size() - 1);
            TextColor color = Utils.getTierColor(tier);

            MutableComponent harvestLevelString = Component.empty();
            Component harvestLevelName = Utils.getTierName(tier);
            Component harvestLevelNum = Component.literal(String.valueOf(tier.getLevel())).withStyle(style -> style.withColor(color));

            if (showHarvestLevel) {
                harvestLevelString.append(harvestLevelName);
                if (showHarvestLevelNum)
                    harvestLevelString.append(" (").append(harvestLevelNum).append(")");
            } else
                harvestLevelString.append(harvestLevelNum);

            stringList.add(Component.translatable(!minimalLayout ? "waila.h12y.harvestlevel" : "").append(" ").append(harvestLevelString));
        }
    }

    @Inject(
            at = @At("HEAD"),
            method = "appendBody",
            remap = false,
            cancellable = true)
    public void onAppendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config, CallbackInfo ci) {
        ci.cancel();
        BlockState state = accessor.getBlockState();
        ItemStack stack = accessor.getStack();
        Player player = accessor.getPlayer();

        if (accessor.getBlock() instanceof InfestedBlock) {
            Block stackBlock = Block.byItem(stack.getItem());
            if (stackBlock != accessor.getBlock())
                state = stackBlock.defaultBlockState();
        }

        boolean minimalLayout = config.getBoolean(MINIMAL);

        List<Component> stringParts = new ArrayList<>();
        try {
            getHarvestability(stringParts, player, state, accessor.getPosition(), config, minimalLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!stringParts.isEmpty()) {
            if (minimalLayout)
                tooltip.addLine(ComponentUtils.formatList(stringParts, Component.literal(config.getString(MINIMAL_SEPARATOR))));
            else
                stringParts.forEach(tooltip::addLine);
        }
    }
}
