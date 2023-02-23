package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.HLTConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.slf4j.Logger;

import java.util.List;

@Mod(HarvestLevelTweaker.MOD_ID)
public class HarvestLevelTweaker {

    public static final String MOD_ID = "hltweaker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HarvestLevelTweaker() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HLTConfig.COMMON_CONFIG);
        HLTConfig.loadConfig();
        MinecraftForge.EVENT_BUS.addListener(HarvestLevelTweaker::onRegisterCommands);
        if (FMLLoader.getDist().isClient()) {
            MinecraftForge.EVENT_BUS.addListener(HarvestLevelTweaker::onItemTooltip);
            if (ModList.get().isLoaded("wthitharvestability"))
                WTHITCompat.apply();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onItemTooltip(ItemTooltipEvent event) {
        if ((event.getPlayer() == null || event.getFlags().isAdvanced() || Screen.hasShiftDown())) {
            HLTTier tier = null;
            if (event.getItemStack().getItem() instanceof TieredItem item) {
                List<Tier> tiers = TierSortingRegistry.getTiersLowerThan(item.getTier()).stream().filter(t -> t instanceof HLTTier).toList();
                if (!tiers.isEmpty())
                    tier = (HLTTier) tiers.get(tiers.size() - 1);
            }
            if (event.getItemStack().getItem() instanceof BlockItem item) {
                BlockState state = item.getBlock().defaultBlockState();
                for (HLTTier t : HLTConfig.getLevelsList())
                    if (state.is(t.getTag()))
                        tier = t;
            }
            if (tier != null)
                event.getToolTip().add(new TranslatableComponent("tooltip.hltweaker.harvest_level", HLTTierRegistry.formatTierName(tier))
                        .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        HLTCommand.register(event.getDispatcher());
    }

}