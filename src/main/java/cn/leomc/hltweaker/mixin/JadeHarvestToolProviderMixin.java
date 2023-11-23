package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.HLTTier;
import cn.leomc.hltweaker.Utils;
import cn.leomc.hltweaker.config.HLTConfig;
import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.SubTextElement;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Mixin(HarvestToolProvider.class)
@OnlyIn(Dist.CLIENT)
public abstract class JadeHarvestToolProviderMixin {

    @Shadow(remap = false)
    @Final
    private static Component CHECK;
    @Shadow(remap = false)
    @Final
    private static Component X;
    @Shadow(remap = false)
    @Final
    private static Vec2 ITEM_SIZE;

    @Shadow(remap = false)
    @Final
    public static Cache<BlockState, ImmutableList<ItemStack>> resultCache;

    @Unique
    private static List<IElement> harvestleveltweaker$getElements(BlockAccessor accessor, IElementHelper helper, List<ItemStack> tools) {
        List<IElement> elements = Lists.newArrayList();

        for (ItemStack tool : tools)
            elements.add(helper.item(tool, 0.75F).translate(new Vec2(-1.0F, -3)).size(ITEM_SIZE));

        boolean canHarvest = Utils.canHarvestBlock(accessor.getBlockState(), accessor.getPlayer());

        if (!elements.isEmpty()) {
            elements.add(0, helper.spacer(5, 0));
            elements.add(new SubTextElement(canHarvest ? CHECK : X).translate(new Vec2(-2.0F, 4)));
        } else {
            elements.add(0, helper.spacer(5, 0));
            elements.add(helper.text(canHarvest ? CHECK : X));
        }

        if (!accessor.getBlockState().requiresCorrectToolForDrops() || !HLTConfig.showHarvestLevelName())
            return elements;

        elements.add(helper.spacer(5, 0));

        String levelText = I18n.get("text.hltweaker.level");
        elements.add(new SubTextElement(Component.literal(levelText))
                .translate(new Vec2(-1, -2)));


        Tier tier = Utils.getHarvestLevel(accessor.getBlockState());
        Component name = tier == null ? Component.translatable("text.hltweaker.unknown_level") : Utils.getTierName(tier);
        elements.add(new SubTextElement(name)
                .translate(new Vec2(-1, 4)));

        elements.add(helper.spacer((int) (Math.max(Minecraft.getInstance().font.width(name), Minecraft.getInstance().font.width(levelText)) * 0.75), 0));

        return elements;
    }


    @Unique
    private static List<ItemStack> harvestleveltweaker$getEffectiveTools(BlockState state, Level level, BlockPos pos) throws ExecutionException {
        return resultCache.get(state, () -> {
            Tier tier = Utils.getHarvestLevel(state);
            if (tier instanceof HLTTier hltTier) {
                ImmutableList<ItemStack> list = ImmutableList.copyOf(hltTier.getIcons(state));
                if (!list.isEmpty())
                    return list;
            }
            return HarvestToolProvider.getTool(state, level, pos);
        });
    }

    @Inject(
            at = @At("HEAD"),
            method = "getText",
            remap = false,
            cancellable = true)
    public void getText(BlockAccessor accessor, IPluginConfig config, CallbackInfoReturnable<List<IElement>> info) throws ExecutionException {
        IThemeHelper.get().success(CHECK);
        IThemeHelper.get().danger(X);
        info.setReturnValue(harvestleveltweaker$getElements(accessor, IElementHelper.get(), harvestleveltweaker$getEffectiveTools(accessor.getBlockState(), accessor.getLevel(), accessor.getPosition())));
    }
}
