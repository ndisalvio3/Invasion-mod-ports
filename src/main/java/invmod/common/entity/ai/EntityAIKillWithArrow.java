package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class EntityAIKillWithArrow<T extends LivingEntity> extends EntityAIKillEntity<T> {
    private float attackRangeSq;

    public EntityAIKillWithArrow(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay, float attackRange) {
        super(entity, targetClass, attackDelay);
        this.attackRangeSq = (attackRange * attackRange);
    }

    public void updateTask() {
        super.updateTask();
        LivingEntity target = getTarget();
        if ((getEntity().distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) < 36.0D) && (getEntity().getEntitySenses().hasLineOfSight(target)))
            getEntity().getNavigatorNew().haltForTick();
    }

    protected void attackEntity(Entity target) {
        setAttackTime(getAttackDelay());
        LivingEntity entity = getEntity();
        Level level = entity.level();
        Arrow arrow = new Arrow(net.minecraft.world.entity.EntityType.ARROW, level);
        arrow.setOwner(entity);
        arrow.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.1F * 1.6F, 12.0F);
        level.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.ARROW_SHOOT, entity.getSoundSource(), 1.0F, 1.0F / (entity.getRandom().nextFloat() * 0.4F + 0.8F));
        level.addFreshEntity(arrow);
    }

    protected boolean canAttackEntity(Entity target) {
        return (getAttackTime() <= 0) && (getEntity().distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) < this.attackRangeSq) && (getEntity().getEntitySenses().hasLineOfSight(target));
    }
}
