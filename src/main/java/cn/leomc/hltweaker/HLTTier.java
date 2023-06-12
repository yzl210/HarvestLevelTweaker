package cn.leomc.hltweaker;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class HLTTier implements Tier {

    private final ResourceLocation id;
    private final int level;
    private final String romanLevel;
    private final TagKey<Block> tag;
    private final Component name;

    public HLTTier(String id, int level) {
        this.id = new ResourceLocation(HarvestLevelTweaker.MOD_ID, id);
        this.level = level;
        this.romanLevel = toRoman(level);
        this.tag = BlockTags.create(new ResourceLocation(HarvestLevelTweaker.MOD_ID, "needs_" + id + "_tool"));
        this.name = Component.translatable("text.hltweaker.level." + id);
    }

    private static String toRoman(int number) {
        return "I".repeat(number)
                .replace("IIIII", "V")
                .replace("IIII", "IV")
                .replace("VV", "X")
                .replace("VIV", "IX")
                .replace("XXXXX", "L")
                .replace("XXXX", "XL")
                .replace("LL", "C")
                .replace("LXL", "XC")
                .replace("CCCCC", "D")
                .replace("CCCC", "CD")
                .replace("DD", "M")
                .replace("DCD", "CM");
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

    public String getRomanLevel() {
        return romanLevel;
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

    @OnlyIn(Dist.CLIENT)
    public String getName() {
        return getNameComponent().getString();
    }

    public Component getNameComponent() {
        return name;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public String toString() {
        return id.getPath().toUpperCase();
    }

}
