package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.HLTConfig;
import cn.leomc.hltweaker.config.HarvestLevelManager;
import cn.leomc.hltweaker.config.ItemHarvestLevelOverride;
import cn.leomc.hltweaker.mixin.DiggerItemAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(HarvestLevelTweaker.MOD_ID)
public class HarvestLevelTweaker {

    public static final String MOD_ID = "hltweaker";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static HarvestLevelTweaker INSTANCE;

    private final HarvestLevelManager manager;

    public HarvestLevelTweaker() {
        INSTANCE = this;
        Path configFolder = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
        HLTConfig.loadConfig(configFolder);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HLTConfig.CLIENT_CONFIG, MOD_ID + "/client.toml");

        manager = new HarvestLevelManager(configFolder);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onFMLCommonSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
        if (FMLLoader.getDist().isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterClientReloadListeners);
            MinecraftForge.EVENT_BUS.addListener(this::onJoinServer);
            MinecraftForge.EVENT_BUS.addListener(this::onItemTooltip);
        }
    }

    private void onFMLCommonSetup(FMLCommonSetupEvent event) {
        manager.load();
    }

    @OnlyIn(Dist.CLIENT)
    private void onItemTooltip(ItemTooltipEvent event) {
        boolean advanced = event.getFlags().isAdvanced();
        boolean populating = event.getEntity() == null;

        if (HLTConfig.toolHarvestLevelDisplayMode().shouldShow(advanced, populating)) {
            Map<TagKey<Block>, Tier> map = new HashMap<>();
            if (event.getItemStack().getItem() instanceof DiggerItem item && item instanceof DiggerItemAccessor accessor) {
                Tier tier = item.getTier();

                if (tier instanceof EquivalentTier t)
                    tier = t.getHLTTier();
                else if (!(tier instanceof HLTTier || tier instanceof Tiers)) {
                    List<Tier> tiers = TierSortingRegistry.getTiersLowerThan(item.getTier())
                            .stream()
                            .filter(t -> t instanceof HLTTier || t instanceof Tiers)
                            .toList();
                    if (!tiers.isEmpty())
                        tier = tiers.get(tiers.size() - 1);
                }

                map.put(accessor.getBlocks(), tier);
            }

            ItemHarvestLevelOverride override = manager.getOverride(ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem()));
            if (override != null)
                override.mineableTags().forEach(tag -> map.put(tag, override.getTier(tag)));


            if (!map.isEmpty()) {
                List<MutableComponent> components = map.entrySet().stream()
                        .map(entry -> new TranslatableComponent("tooltip.hltweaker.tool_type_level",
                                Utils.getMineableName(entry.getKey()),
                                Utils.getTierName(entry.getValue())).withStyle(ChatFormatting.YELLOW))
                        .toList();
                event.getToolTip().addAll(components);
            }
        }

        if (HLTConfig.blockHarvestLevelDisplayMode().shouldShow(advanced, populating) && event.getItemStack().getItem() instanceof BlockItem item) {
            BlockState state = item.getBlock().defaultBlockState();
            if (state.requiresCorrectToolForDrops())
                event.getToolTip().add(new TranslatableComponent("tooltip.hltweaker.block_harvest_level", Utils.getTierName(Utils.getHarvestLevel(state)))
                        .withStyle(ChatFormatting.YELLOW));
        }
    }

    private final PreparableReloadListener cacheClearer = new SimplePreparableReloadListener<Void>() {
        @Override
        protected Void prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
            return null;
        }

        @Override
        protected void apply(Void pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
            Utils.clearCache();
        }
    };

    private void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(cacheClearer);
    }

    private void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(cacheClearer);
    }

    private void onJoinServer(ClientPlayerNetworkEvent.LoggedInEvent event) {
        Utils.clearCache();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        HLTCommand.register(event.getDispatcher());
    }

    public static HarvestLevelManager getManager() {
        return INSTANCE.manager;
    }
}