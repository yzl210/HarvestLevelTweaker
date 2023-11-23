package cn.leomc.hltweaker;

import cn.leomc.hltweaker.config.ItemHarvestLevelOverride;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HLTCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hltweaker")
                .requires(stack -> stack.hasPermission(4))
                .then(Commands.literal("levels")
                        .executes(context -> showLevels(context, false))
                        .then(Commands.literal("all")
                                .executes(context -> showLevels(context, true))))
                .then(Commands.literal("overrides")
                        .executes(HLTCommand::showOverrides))
        );
    }

    private static int showLevels(CommandContext<CommandSourceStack> context, boolean all) {
        List<Tier> tiers = Utils.getTiers();

        List<Component> components = new ArrayList<>();

        for (Tier tier : TierSortingRegistry.getSortedTiers()) {
            if (!all && !tiers.contains(tier))
                continue;
            MutableComponent levelInfo = Component.literal(tier.getLevel() + " -> " + TierSortingRegistry.getName(tier));
            if (!all || tiers.contains(tier))
                levelInfo.append(" -> ").append(Utils.getTierName(tier));
            components.add(levelInfo);
            if (tier instanceof HLTTier hltTier) {
                hltTier.getIcons().forEach((tag, stack) -> {
                    MutableComponent iconInfo = Component.literal("  ");
                    iconInfo.append(getMineableTagInfo(tag));
                    iconInfo.append(" -> ");
                    iconInfo.append(stack.getDisplayName());
                    components.add(iconInfo);
                });
            }
        }

        context.getSource().sendSystemMessage(Component.literal("Harvest levels: \n")
                .append(CommonComponents.joinLines(components)));
        return Command.SINGLE_SUCCESS;
    }


    private static int showOverrides(CommandContext<CommandSourceStack> context) {
        List<Component> components = new ArrayList<>();
        for (ItemHarvestLevelOverride override : HarvestLevelTweaker.getManager().getOverrides()) {
            Component itemName = Optional.ofNullable(ForgeRegistries.ITEMS.getValue(override.item()))
                    .map(Item::getDefaultInstance)
                    .map(ItemStack::getDisplayName)
                    .orElseGet(() -> Component.literal(override.item().toString()));
            components.add(itemName);

            override.getOverrides().forEach((tag, tier) -> {
                MutableComponent overrideInfo = Component.literal("  ");
                overrideInfo.append(getMineableTagInfo(tag));
                overrideInfo.append(" -> ");
                MutableComponent tierLocation = Component.literal(tier.toString());
                Component tierName = Optional.ofNullable(TierSortingRegistry.byName(tier))
                        .map(Utils::getTierName)
                        .map(c -> c.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tierLocation))))
                        .orElse(tierLocation);
                overrideInfo.append(tierName);
                components.add(overrideInfo);
            });
        }

        context.getSource().sendSystemMessage(Component.literal("Item harvest level overrides: \n")
                .append(CommonComponents.joinLines(components)));
        return Command.SINGLE_SUCCESS;
    }

    private static Component getMineableTagInfo(TagKey<Block> tag) {
        return Utils.getMineableName(tag).withStyle(style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tag.location().toString()))));
    }
}
