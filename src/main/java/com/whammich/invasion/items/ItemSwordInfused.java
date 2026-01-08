package com.whammich.invasion.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;

public class ItemSwordInfused extends Item {
    public ItemSwordInfused(Item.Properties properties) {
        super(properties.sword(ModToolMaterials.INFUSED, 3.0F, -2.4F));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            float bonus = Math.min(6.0F, 1.0F + target.getMaxHealth() * 0.05F);
            target.hurt(attacker.damageSources().magic(), bonus);
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0));
        }
        super.hurtEnemy(stack, target, attacker);
    }
}
