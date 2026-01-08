package com.whammich.invasion.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;

public class ItemBowSearing extends BowItem {
    public ItemBowSearing(Properties properties) {
        super(properties);
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, LivingEntity target) {
        projectile.igniteForSeconds(4.0F);
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
    }
}
