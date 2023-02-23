package cn.leomc.hltweaker.mixin;

import cn.leomc.hltweaker.HLTTier;
import cn.leomc.hltweaker.HLTTierRegistry;
import cn.leomc.hltweaker.config.HLTConfig;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.impl.ui.SubTextElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.TierSortingRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.addon.harvest.HarvestToolProvider;

import java.util.ArrayList;
import java.util.List;

@Mixin(HarvestToolProvider.class)
@OnlyIn(Dist.CLIENT)
public class JadeHarvestToolProviderMixin {

    private static final List<Pair<TagKey<Block>, ItemStack>> EFFECTIVE_TOOL = new ArrayList<>();
    @Shadow(remap = false)
    @Final
    private static Component CHECK;
    @Shadow(remap = false)
    @Final
    private static Component X;
    @Shadow(remap = false)
    @Final
    private static Vec2 ITEM_SIZE;

    static {
        EFFECTIVE_TOOL.add(new Pair<>(BlockTags.MINEABLE_WITH_AXE, Items.WOODEN_AXE.getDefaultInstance()));
        EFFECTIVE_TOOL.add(new Pair<>(BlockTags.MINEABLE_WITH_PICKAXE, Items.WOODEN_PICKAXE.getDefaultInstance()));
        EFFECTIVE_TOOL.add(new Pair<>(BlockTags.MINEABLE_WITH_HOE, Items.WOODEN_HOE.getDefaultInstance()));
        EFFECTIVE_TOOL.add(new Pair<>(BlockTags.MINEABLE_WITH_SHOVEL, Items.WOODEN_SHOVEL.getDefaultInstance()));
    }

    private static List<IElement> getElements(BlockAccessor accessor, IElementHelper helper, List<ItemStack> tools) {

        List<IElement> elements = Lists.newArrayList();

        for (ItemStack tool : tools)
            elements.add(helper.item(tool, 0.75F).translate(new Vec2(-1.0F, -3)).size(ITEM_SIZE));

        boolean canHarvest = canHarvestBlock(accessor.getBlockState(), accessor.getPlayer());

        if (!elements.isEmpty()) {
            elements.add(0, helper.spacer(5, 0));
            elements.add(new SubTextElement(canHarvest ? CHECK : X).translate(new Vec2(-2.0F, 4)));
        } else {
            elements.add(0, helper.spacer(5, 0));
            elements.add(helper.text(canHarvest ? CHECK : X));
        }

        elements.add(helper.spacer(5, 0));

        String unknownLevel = I18n.get("text.hltweaker.level");
        elements.add(new SubTextElement(new TextComponent(unknownLevel))
                .translate(new Vec2(-1, -2)));


        HLTTier tier = getHarvestLevel(accessor.getBlockState());
        String s = tier == null ? I18n.get("text.hltweaker.unknown_level") : HLTTierRegistry.formatTierName(tier).getString();
        elements.add(new SubTextElement(new TextComponent(s))
                .translate(new Vec2(-1, 4)));

        elements.add(helper.spacer((int) (Math.max(Minecraft.getInstance().font.width(s), Minecraft.getInstance().font.width(unknownLevel)) * 0.75), 0));

        return elements;
    }

    private static boolean canHarvestBlock(BlockState state, Player player) {
        return !state.requiresCorrectToolForDrops() || player.hasCorrectToolForDrops(state) || player.getMainHandItem().isCorrectToolForDrops(state);
    }

    private static HLTTier getHarvestLevel(BlockState state) {
        for (HLTTier tier : HLTConfig.getHarvestLevels().values())
            if (TierSortingRegistry.isCorrectTierForDrops(tier, state))
                return tier;
        return null;
    }

    private static List<ItemStack> getEffectiveTools(BlockState state) {
        return EFFECTIVE_TOOL.stream().filter(pair -> state.is(pair.getFirst())).map(Pair::getSecond).toList();
    }

    @Inject(
            at = @At("RETURN"),
            method = "getText",
            remap = false,
            cancellable = true)
    public void getText(BlockAccessor accessor, IPluginConfig config, IElementHelper helper, CallbackInfoReturnable<List<IElement>> info) {
        if (info.getReturnValue().isEmpty())
            info.setReturnValue(getElements(accessor, helper, getEffectiveTools(accessor.getBlockState())));
    }

}
