package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.HLTConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Mod(HarvestLevelTweaker.MOD_ID)
public class HarvestLevelTweaker {

    public static final String MOD_ID = "hltweaker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HarvestLevelTweaker() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HLTConfig.COMMON_CONFIG);
        HLTConfig.loadConfig();
        MinecraftForge.EVENT_BUS.addListener(HarvestLevelTweaker::onRegisterCommands);
        //MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, HarvestLevelTweaker::onHarvest);
        if (FMLLoader.getDist().isClient()) {
            MinecraftForge.EVENT_BUS.addListener(HarvestLevelTweaker::onItemTooltip);
            if (ModList.get().isLoaded("wthitharvestability"))
                WTHITCompat.apply();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getPlayer() == null || event.getFlags().isAdvanced() || Screen.hasShiftDown()) {
            Tier tier = null;

            if (event.getItemStack().getItem() instanceof TieredItem item) {
                List<Tier> tiers = TierSortingRegistry.getTiersLowerThan(item.getTier()).stream().filter(t -> t instanceof HLTTier).toList();
                if (!tiers.isEmpty())
                    tier = tiers.get(tiers.size() - 1);
            }

            ResourceLocation rl = event.getItemStack().getItem().getRegistryName();
            if (HLTConfig.getItemOverrides().containsKey(rl))
                tier = HLTConfig.getItemOverrides().get(rl).right();

            if (tier != null)
                event.getToolTip().add(new TranslatableComponent("tooltip.hltweaker.harvest_level", Utils.getTierName(tier))
                        .withStyle(ChatFormatting.YELLOW));

            if (Utils.isItemOverridden(event.getItemStack().getItem())) {
                String s = HLTConfig.getItemOverrides().get(event.getItemStack().getItem().getRegistryName()).left().stream()
                        .map(TagKey::location)
                        .map(id -> {
                            String key = "text.hltweaker.tool." + id.getNamespace() + "." + id.getPath().replace("mineable/", "");
                            if(!I18n.exists(key))
                                return id.getPath().replace("mineable/", "");
                            return I18n.get(key);
                        })
                        .collect(Collectors.joining(", "));
                event.getToolTip().add(new TranslatableComponent("tooltip.hltweaker.tool_type", s)
                        .withStyle(ChatFormatting.YELLOW));
            }

            if (event.getItemStack().getItem() instanceof BlockItem item) {
                BlockState state = item.getBlock().defaultBlockState();
                for (HLTTier t : HLTConfig.getLevelsList())
                    if (state.is(t.getTag()))
                        event.getToolTip().add(new TranslatableComponent("tooltip.hltweaker.block_harvest_level", Utils.getTierName(t))
                                .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        HLTCommand.register(event.getDispatcher());
    }

}