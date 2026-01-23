package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.Goal;
import net.minecraft.world.entity.LivingEntity;

public class EntityAIMeleeAttack<T extends LivingEntity> extends LegacyGoal {
    private EntityIMLiving theEntity;
    private Class<? extends T> targetClass;
    private float attackRange;
    private int attackDelay;
    private int nextAttack;

    public EntityAIMeleeAttack(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay) {
        this.theEntity = entity;
        this.targetClass = targetClass;
        this.attackDelay = attackDelay;
        this.attackRange = 0.6F;
        this.nextAttack = 0;
    }

    public boolean shouldExecute() {
        LivingEntity target = this.theEntity.getAttackTarget();
        return (target != null) && (this.theEntity.getAIGoal() == Goal.MELEE_TARGET) && (this.theEntity.getDistanceToEntity(target) < (this.attackRange + this.theEntity.getBbWidth() + target.getBbWidth()) * 4.0F) && (target.getClass().isAssignableFrom(this.targetClass));
    }

    public void updateTask() {
        LivingEntity target = this.theEntity.getAttackTarget();
        if (canAttackEntity(target)) {
            attackEntity(target);
        }
        setAttackTime(getAttackTime() - 1);
    }

    public Class<? extends T> getTargetClass() {
        return this.targetClass;
    }

    protected void attackEntity(LivingEntity target) {
        this.theEntity.attackEntityAsMob(target);
        setAttackTime(getAttackDelay());
    }

    protected boolean canAttackEntity(LivingEntity target) {
        if (getAttackTime() <= 0) {
            double d = this.theEntity.getBbWidth() + this.attackRange;
            return this.theEntity.getDistanceSq(target.getX(), target.getBoundingBox().minY, target.getZ()) < d * d;
        }
        return false;
    }

    protected int getAttackTime() {
        return this.nextAttack;
    }

    protected void setAttackTime(int time) {
        this.nextAttack = time;
    }

    protected int getAttackDelay() {
        return this.attackDelay;
    }

    protected void setAttackDelay(int time) {
        this.attackDelay = time;
    }
}