package cn.leomc.hltweaker;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.function.Supplier;


public final class EquivalentTier implements Tier {
    private final HLTTier hltTier;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    @Nonnull
    private final Supplier<Ingredient> repairIngredient;

    public EquivalentTier(HLTTier hltTier, int uses, float speed, float attackDamageBonus, int enchantmentValue, @Nonnull Supplier<Ingredient> repairIngredient) {
        this.hltTier = hltTier;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return hltTier.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Nonnull
    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    public HLTTier getHLTTier() {
        return hltTier;
    }

    @Override
    public String toString() {
        return "EquivalentTier[" + hltTier + "]";
    }

}
