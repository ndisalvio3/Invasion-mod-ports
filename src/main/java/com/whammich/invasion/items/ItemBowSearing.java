package com.whammich.invasion.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;

public class ItemBowSearing extends BowItem {
    public ItemBowSearing(Properties properties) {
        super(properties);
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, LivingEntity target) {
        float charge = Math.min(1.0F, velocity / 3.0F);
        projectile.igniteForSeconds(2.0F + Math.round(charge * 4.0F));
        if (projectile instanceof AbstractArrow arrow) {
            arrow.setBaseDamage(2.0F + charge * 2.0F);
            if (charge >= 1.0F) {
                arrow.setCritArrow(true);
            }
        }
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
    }
}
