package invmod.common.entity.ai;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.Goal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class EntityAIWingAttack extends LegacyGoal {
    private final EntityIMBird theEntity;
    private final Class<? extends LivingEntity> targetClass;
    private final int attackDelay;
    private int nextAttack;

    public EntityAIWingAttack(EntityIMBird entity, Class<? extends LivingEntity> targetClass, int attackDelay) {
        this.theEntity = entity;
        this.targetClass = targetClass;
        this.attackDelay = attackDelay;
        this.nextAttack = 0;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        LivingEntity target = this.theEntity.getAttackTarget();
        return target != null
            && this.theEntity.getAIGoal() == Goal.MELEE_TARGET
            && targetClass.isAssignableFrom(target.getClass());
    }

    public void updateTask() {
        LivingEntity target = this.theEntity.getAttackTarget();
        if (target != null && canAttackEntity(target)) {
            attackEntity(target);
        }
        setAttackTime(getAttackTime() - 1);
    }

    public void resetTask() {
        this.theEntity.setAttackingWithWings(false);
    }

    protected void attackEntity(LivingEntity target) {
        if (theEntity.level() instanceof ServerLevel serverLevel) {
            theEntity.doHurtTarget(serverLevel, target);
        }
        setAttackTime(getAttackDelay());
    }

    protected boolean canAttackEntity(LivingEntity target) {
        if (getAttackTime() <= 0) {
            double d = theEntity.getBbWidth() + theEntity.getAttackRange() + 3.0D;
            return theEntity.getDistanceSq(target.getX(), target.getBoundingBox().minY, target.getZ()) < d * d;
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
}
