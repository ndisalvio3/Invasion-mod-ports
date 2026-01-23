package invmod.common.entity.ai;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.Goal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class EntityAIBirdFight<T extends LivingEntity> extends LegacyGoal {
    private final EntityIMBird theEntity;
    private final Class<? extends T> targetClass;
    private final int attackDelay;
    private final float retreatHealthLossPercent;
    private int nextAttack;

    public EntityAIBirdFight(EntityIMBird entity, Class<? extends T> targetClass, int attackDelay, float retreatHealthLossPercent) {
        this.theEntity = entity;
        this.targetClass = targetClass;
        this.attackDelay = attackDelay;
        this.retreatHealthLossPercent = retreatHealthLossPercent;
        this.nextAttack = 0;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        LivingEntity target = this.theEntity.getAttackTarget();
        return this.theEntity.getAIGoal() == Goal.MELEE_TARGET && target != null && targetClass.isAssignableFrom(target.getClass());
    }

    public boolean continueExecuting() {
        LivingEntity target = this.theEntity.getAttackTarget();
        return target != null && this.theEntity.getAIGoal() == Goal.MELEE_TARGET;
    }

    public void updateTask() {
        if (getAttackTime() == 0) {
            this.theEntity.setAttackingWithWings(isInStartMeleeRange());
        }
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
        this.theEntity.doMeleeSound();
        if (theEntity.level() instanceof ServerLevel serverLevel) {
            theEntity.doHurtTarget(serverLevel, target);
        }
        if (retreatHealthLossPercent > 0.0F) {
            doWingBuffetAttack(target);
        }
        setAttackTime(getAttackDelay());
    }

    protected boolean canAttackEntity(LivingEntity target) {
        if (getAttackTime() <= 0) {
            double d = this.theEntity.getBbWidth() + this.theEntity.getAttackRange() + 3.0D;
            return this.theEntity.getDistanceSq(target.getX(), target.getBoundingBox().minY, target.getZ()) < d * d;
        }
        return false;
    }

    protected boolean isInStartMeleeRange() {
        LivingEntity target = this.theEntity.getAttackTarget();
        if (target == null) {
            return false;
        }
        double d = this.theEntity.getBbWidth() + this.theEntity.getAttackRange() + 3.0D;
        return this.theEntity.getDistanceSq(target.getX(), target.getBoundingBox().minY, target.getZ()) < d * d;
    }

    protected void doWingBuffetAttack(LivingEntity target) {
        int knockback = 2;
        float yawRad = this.theEntity.getYRot() * Mth.DEG_TO_RAD;
        target.push(-Mth.sin(yawRad) * knockback * 0.5F, 0.4D, Mth.cos(yawRad) * knockback * 0.5F);
        target.level().playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_HURT, target.getSoundSource(), 1.0F, 1.0F);
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
