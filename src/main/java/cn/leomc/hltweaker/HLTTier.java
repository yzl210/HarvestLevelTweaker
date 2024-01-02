package cn.leomc.hltweaker;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HLTTier implements Tier {

    private final ResourceLocation id;
    private final int level;
    private final TextColor color;
    private final TagKey<Block> tag;
    private final MutableComponent name;
    private final Map<TagKey<Block>, ItemStack> icons;

    public HLTTier(String id, int level, @Nullable TextColor color) {
        this.id = new ResourceLocation(HarvestLevelTweaker.MOD_ID, id);
        this.level = level;
        this.color = color == null ? TextColor.fromLegacyFormat(ChatFormatting.WHITE) : color;
        this.tag = BlockTags.create(new ResourceLocation(HarvestLevelTweaker.MOD_ID, "needs_" + id + "_tool"));
        this.name = new TranslatableComponent("text.hltweaker.level." + id).withStyle(style -> style.withColor(color));
        this.icons = new HashMap<>();
    }

    @Override
    public int getUses() {
        return 0;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public float getAttackDamageBonus() {
        return 0;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    @NotNull
    public TagKey<Block> getTag() {
        return tag;
    }

    public TextColor getColor() {
        return color;
    }

    public MutableComponent getName() {
        return name.copy();
    }

    public ResourceLocation getId() {
        return id;
    }

    public void setIcon(TagKey<Block> mineableTag, ItemStack icon) {
        icons.put(mineableTag, icon);
    }

    public ItemStack getIcon(TagKey<Block> mineableTag) {
        return icons.get(mineableTag);
    }

    public List<ItemStack> getIcons(BlockState state) {
        return icons.entrySet().stream()
                .filter(e -> state.is(e.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    public Map<TagKey<Block>, ItemStack> getIcons() {
        return Collections.unmodifiableMap(icons);
    }

    @Override
    public String toString() {
        return id.getPath().toUpperCase();
    }

}
