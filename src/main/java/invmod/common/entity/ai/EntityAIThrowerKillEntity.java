package invmod.common.entity.ai;

import invmod.common.entity.EntityIMThrower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class EntityAIThrowerKillEntity<T extends LivingEntity> extends LegacyGoal {
    private final EntityIMThrower theEntity;
    private final Class<? extends T> targetClass;
    private boolean melee;
    private float attackRangeSq;
    private float launchSpeed;
    private final Random rand;
    private int maxBoulderAmount;
    private int attackDelay;
    private int nextAttack;

    public EntityAIThrowerKillEntity(EntityIMThrower entity, Class<? extends T> targetClass, int attackDelay, float throwRange, float launchSpeed) {
        this.theEntity = entity;
        this.targetClass = targetClass;
        this.attackDelay = attackDelay;
        this.attackRangeSq = (throwRange * throwRange);
        this.launchSpeed = launchSpeed;
        this.rand = new Random();
        this.maxBoulderAmount = 3;
        this.nextAttack = 0;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        LivingEntity target = this.theEntity.getTarget();
        return target != null && this.targetClass.isAssignableFrom(target.getClass());
    }

    public boolean continueExecuting() {
        LivingEntity target = this.theEntity.getTarget();
        return target != null && this.targetClass.isAssignableFrom(target.getClass());
    }

    public void updateTask() {
        setAttackTime(getAttackTime() - 1);
        LivingEntity target = this.theEntity.getTarget();
        if (target != null && canAttackEntity(target)) {
            attackEntity(target);
        }
    }

    protected void attackEntity(Entity target) {
        if (this.melee) {
            setAttackTime(getAttackDelay());
            if (theEntity.level() instanceof ServerLevel serverLevel) {
                theEntity.doHurtTarget(serverLevel, target);
            }
        } else {
            setAttackTime(getAttackDelay() * 2);
            int distance = Math.round((float) this.theEntity.getDistance(target.getX(), target.getY(), target.getZ()));
            int missDistance = Math.round((float) Math.ceil(distance / 10));

            for (int i = 1; i <= this.rand.nextInt(maxBoulderAmount); i++) {
                double x = (target.getX() - missDistance) + (double) this.rand.nextInt((missDistance + 1) * 2);
                double y = (target.getY() - missDistance + 1) + (double) this.rand.nextInt((missDistance + 1) * 2);
                double z = (target.getZ() - missDistance) + (double) this.rand.nextInt((missDistance + 1) * 2);

                if (this.theEntity.getTier() == 1) {
                    this.theEntity.throwBoulder(x, y, z);
                } else {
                    this.theEntity.throwTNT(x, y, z);
                }
            }
        }
    }

    protected boolean canAttackEntity(Entity target) {
        this.melee = isInMeleeRange(target);
        if (this.melee) {
            return true;
        }
        if (!this.theEntity.canThrow()) {
            return false;
        }

        double dX = this.theEntity.getX() - target.getX();
        double dZ = this.theEntity.getZ() - target.getZ();
        double dXY = Mth.sqrt((float) (dX * dX + dZ * dZ));
        return (getAttackTime() <= 0)
            && this.theEntity.getEntitySenses().hasLineOfSight(target)
            && (0.025D * dXY / (this.launchSpeed * this.launchSpeed) <= 1.0D);
    }

    private boolean isInMeleeRange(Entity target) {
        double d = (theEntity.getBbWidth() + 1.0F) * (theEntity.getBbWidth() + 1.0F);
        return theEntity.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) < d;
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
