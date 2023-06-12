package cn.leomc.hltweaker;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.Arrays;

public class HLTCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("harvestleveltweaker")
                .then(Commands.literal("levels")
                        .requires(stack -> stack.hasPermission(2))
                        .executes(HLTCommand::showLevels)));
    }

    private static int showLevels(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(Component.literal(Arrays.toString(TierSortingRegistry.getSortedTiers().toArray())), false);
        if (context.getSource().getEntity() instanceof Player player)
            player.sendSystemMessage(Component.literal(player.getMainHandItem().getItem().getClass().getName()));
        return Command.SINGLE_SUCCESS;
    }
}
