package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EntityAICharge<T extends LivingEntity> extends EntityAIMoveToEntity<T> {
    protected EntityIMLiving charger;
    protected LivingEntity chargeTarget;
    protected double chargeX;
    protected double chargeY;
    protected double chargeZ;
    protected float speed;
    protected int windup;
    protected boolean hasAttacked;
    protected int chargeDelay;
    protected int runTime;

    public EntityAICharge(EntityIMLiving entity, Class<? extends T> targetClass, float f) {
        super(entity, targetClass);
        this.charger = entity;
        this.speed = f;
        this.windup = 0;
        this.hasAttacked = false;
        this.chargeDelay = 100;
        this.runTime = 15;
    }


    @Override
    public boolean shouldExecute() {

        if (chargeDelay > 0) {
            this.chargeDelay--;
            return false;
        }

        this.chargeTarget = this.charger.getAttackTarget();
        if (this.chargeTarget == null) {
            return false;

        }
        double distance = Math.sqrt(this.charger.getDistanceSqToEntity(this.chargeTarget));
        if ((distance < 5.0D) || (distance > 20.0D)) {
            return false;
        }
        if (!this.charger.onGround()) {
            return false;
        }
        Vec3 chargePos = findChargePoint(this.charger, this.chargeTarget, 6.0D);
        if (chargePos == null) {
            return false;
        }


        this.chargeX = chargePos.x;
        this.chargeY = chargePos.y;
        this.chargeZ = chargePos.z;

        return this.charger.getRNG().nextInt(1) == 0;
    }

    @Override
    public void startExecuting() {

        this.windup = (15 + this.charger.getRNG().nextInt(25));
    }

    @Override
    public boolean continueExecuting() {
        if (this.windup == 0 && this.runTime > 0) {
            this.runTime--;
        }

        return (this.windup > 0) || (this.runTime > 0);
    }

    @Override
    public void updateTask() {
        this.charger.getLookControl().setLookAt(this.chargeX, this.chargeY - 1.0D, this.chargeZ);
        if (this.windup > 0) {
            if (--this.windup == 0) {
                this.charger.getNavigation().moveTo(this.chargeX, this.chargeY, this.chargeZ, this.speed);
            } else {
                // charging animation handled by pigman-specific logic elsewhere
            }
        }
        double var1 = this.charger.getBbWidth() * 2.1F * this.charger.getBbWidth() * 2.1F;
        if (this.charger.getDistanceSq(this.chargeTarget.getX(), this.chargeTarget.getBoundingBox().minY, this.chargeTarget.getZ()) <= var1) {
            if (!this.hasAttacked) {
                this.hasAttacked = true;
                this.charger.attackEntityAsMob(this.chargeTarget);
            }
        }
    }

    @Override
    public void resetTask() {
        this.windup = 0;
        this.chargeTarget = null;
        this.hasAttacked = false;
        this.chargeDelay = 100;
        this.runTime = 15;
        // charging animation handled by pigman-specific logic elsewhere
    }

    protected Vec3 findChargePoint(Entity attacker, Entity target, double overshoot) {
        double vecx = target.getX() - attacker.getX();
        double vecz = target.getZ() - attacker.getZ();
        float rangle = (float) Math.atan2(vecz, vecx);

        double distance = Mth.sqrt((float) (vecx * vecx + vecz * vecz));

        double dx = Mth.cos(rangle) * (distance + overshoot);
        double dz = Mth.sin(rangle) * (distance + overshoot);

        return new Vec3((attacker.getX() + dx), target.getY(), (attacker.getZ() + dz));
    }
}
