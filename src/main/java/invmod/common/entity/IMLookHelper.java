package invmod.common.entity;

import invmod.common.util.MathUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.LookControl;

public class IMLookHelper extends LookControl {
    private final EntityIMLiving mob;
    private float yawSpeed;
    private float pitchSpeed;
    private boolean hasTarget;
    private double targetX;
    private double targetY;
    private double targetZ;

    public IMLookHelper(EntityIMLiving entity) {
        super(entity);
        this.mob = entity;
    }

    public void setLookPositionWithEntity(Entity entity, float yawSpeed, float pitchSpeed) {
        this.targetX = entity.getX();
        this.targetY = entity instanceof LivingEntity
            ? entity.getEyeY()
            : (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D;
        this.targetZ = entity.getZ();
        this.yawSpeed = yawSpeed;
        this.pitchSpeed = pitchSpeed;
        this.hasTarget = true;
    }

    public void setLookPosition(double x, double y, double z, float yawSpeed, float pitchSpeed) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.yawSpeed = yawSpeed;
        this.pitchSpeed = pitchSpeed;
        this.hasTarget = true;
    }

    @Override
    public void tick() {
        if (!hasTarget) {
            return;
        }

        hasTarget = false;
        double dX = targetX - mob.getX();
        double dY = targetY - mob.getEyeY();
        double dZ = targetZ - mob.getZ();
        double dXZ = Math.sqrt(dX * dX + dZ * dZ);
        float yaw = (float) MathUtil.boundAngle180Deg(mob.getYRot());
        float pitch = (float) MathUtil.boundAngle180Deg(mob.getXRot());
        float yawHeadOffset = (float) (Math.atan2(dZ, dX) * 180.0D / Math.PI) - 90.0F - yaw;
        float pitchHeadOffset = (float) (Math.atan2(dY, dXZ) * 180.0D / Math.PI + 40.0D - pitch);
        float boundedYaw = (float) MathUtil.boundAngle180Deg(yawHeadOffset);
        float yawFinal = (boundedYaw > 100.0F || boundedYaw < -100.0F) ? 0.0F : boundedYaw / 6.0F;

        mob.setXRot(updateRotation(mob.getXRot(), pitchHeadOffset, pitchSpeed));
        mob.setYHeadRot(updateRotation(mob.getYHeadRot(), yawFinal, yawSpeed));
    }

    private float updateRotation(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees(target - current);
        if (delta > maxDelta) {
            delta = maxDelta;
        }
        if (delta < -maxDelta) {
            delta = -maxDelta;
        }
        return current + delta;
    }
}
