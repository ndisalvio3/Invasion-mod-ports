package invmod.common.entity.ai;

import invmod.common.entity.EntityIMBird;
import invmod.common.entity.Goal;
import invmod.common.entity.INavigationFlying;
import invmod.common.entity.MoveState;
import invmod.common.util.MathUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EntityAISwoop extends LegacyGoal {
    private static final int INITIAL_LINEUP_TIME = 25;
    private EntityIMBird theEntity;
    private float minDiveClearanceY;
    private LivingEntity swoopTarget;
    private float diveAngle;
    private float diveHeight;
    private float strikeDistance;
    private float minHeight;
    private float minXZDistance;
    private float maxSteepness;
    private float finalRunLength;
    private float finalRunArcLimit;
    private int time;
    private boolean isCommittedToFinalRun;
    private boolean endSwoop;
    private boolean usingClaws;

    public EntityAISwoop(EntityIMBird entity) {
        this.theEntity = entity;
        this.minDiveClearanceY = 0.0F;
        this.swoopTarget = null;
        this.diveAngle = 0.0F;
        this.diveHeight = 0.0F;
        this.maxSteepness = 40.0F;
        this.strikeDistance = (entity.getBbWidth() + 1.5F);
        this.minHeight = 6.0F;
        this.minXZDistance = 10.0F;
        this.finalRunLength = 4.0F;
        this.finalRunArcLimit = 15.0F;
        this.time = 0;
        this.isCommittedToFinalRun = false;
        this.endSwoop = false;
        this.usingClaws = false;
        setMutexBits(1);
    }

    public boolean shouldExecute() {
        if ((this.theEntity.getAIGoal() == Goal.FIND_ATTACK_OPPORTUNITY) && (this.theEntity.getAttackTarget() != null)) {
            this.swoopTarget = this.theEntity.getAttackTarget();
            double dX = this.swoopTarget.getX() - this.theEntity.getX();
            double dY = this.swoopTarget.getY() - this.theEntity.getY();
            double dZ = this.swoopTarget.getZ() - this.theEntity.getZ();
            double dXZ = Math.sqrt(dX * dX + dZ * dZ);
            if ((-dY < this.minHeight) || (dXZ < this.minXZDistance)) {
                return false;
            }
            double pitchToTarget = Math.atan(dY / dXZ) * 180.0D / 3.141592653589793D;
            if (pitchToTarget > this.maxSteepness) {
                return false;
            }
            this.finalRunLength = ((float) (dXZ * 0.42D));
            if (this.finalRunLength > 18.0F)
                this.finalRunLength = 18.0F;
            else if (this.finalRunLength < 4.0F) {
                this.finalRunLength = 4.0F;
            }
            this.diveAngle = ((float) (Math.atan((dXZ - this.finalRunLength) / dY) * 180.0D / 3.141592653589793D));
            if ((this.swoopTarget != null) && (isSwoopPathClear(this.swoopTarget, this.diveAngle))) {
                this.diveHeight = ((float) -dY);
                return true;
            }
        }
        return false;
    }

    public boolean continueExecuting() {
        return (this.theEntity.getAttackTarget() == this.swoopTarget) && (!this.endSwoop) && (this.theEntity.getMoveState() == MoveState.FLYING);
    }

    public void startExecuting() {
        this.time = 0;
        this.theEntity.transitionAIGoal(Goal.SWOOP);
        this.theEntity.getNavigatorNew().setMovementType(INavigationFlying.MoveType.PREFER_FLYING);
        this.theEntity.getNavigatorNew().tryMoveToEntity(this.swoopTarget, 0.0F, this.theEntity.getMaxPoweredFlightSpeed());

        this.theEntity.doScreech();
    }

    public void resetTask() {
        this.endSwoop = false;
        this.isCommittedToFinalRun = false;
        this.theEntity.getNavigatorNew().enableDirectTarget(false);
        if (this.theEntity.getAIGoal() == Goal.SWOOP) {
            this.theEntity.transitionAIGoal(Goal.NONE);
            this.theEntity.setClawsForward(false);
        }
    }

    public void updateTask() {
        this.time += 1;
        if (!this.isCommittedToFinalRun) {
            if (this.theEntity.getDistanceToEntity(this.swoopTarget) < this.finalRunLength) {
                this.theEntity.getNavigatorNew().setPitchBias(0.0F, 1.0F);
                if (isFinalRunLinedUp()) {
                    this.usingClaws = (this.theEntity.worldObj.rand.nextFloat() > 0.6F);

                    this.theEntity.setClawsForward(true);

                    this.theEntity.getNavigatorNew().enableDirectTarget(true);
                    this.isCommittedToFinalRun = true;
                } else {
                    this.theEntity.transitionAIGoal(Goal.NONE);
                    this.endSwoop = true;
                }
            } else if (this.time > 25) {
                double dYp = -(this.swoopTarget.getY() - this.theEntity.getY());
                if (dYp < 2.9D) {
                    dYp = 0.0D;
                }
                this.theEntity.getNavigatorNew().setPitchBias(this.diveAngle * (float) (dYp / this.diveHeight), (float) (0.6D * (dYp / this.diveHeight)));
            }

        } else if (this.theEntity.getDistanceToEntity(this.swoopTarget) < this.strikeDistance) {
            this.theEntity.transitionAIGoal(Goal.FLYING_STRIKE);

            this.theEntity.getNavigatorNew().enableDirectTarget(false);
            this.endSwoop = true;
        } else {
            double dX = this.swoopTarget.getX() - this.theEntity.getX();
            double dZ = this.swoopTarget.getZ() - this.theEntity.getZ();
            double yawToTarget = Math.atan2(dZ, dX) * 180.0D / 3.141592653589793D - 90.0D;
            if (Math.abs(MathUtil.boundAngle180Deg(yawToTarget - this.theEntity.getYRot())) > 90.0D) {
                this.theEntity.transitionAIGoal(Goal.NONE);
                this.theEntity.getNavigatorNew().enableDirectTarget(false);
                this.theEntity.setClawsForward(false);
                this.endSwoop = true;
            }
        }
    }

    private boolean isSwoopPathClear(LivingEntity target, float diveAngle) {
        double dX = target.getX() - this.theEntity.getX();
        double dY = target.getY() - this.theEntity.getY();
        double dZ = target.getZ() - this.theEntity.getZ();
        double dXZ = Math.sqrt(dX * dX + dZ * dZ);
        double dRayY = 2.0D;
        int hitCount = 0;
        double lowestCollide = this.theEntity.getY();
        for (double y = this.theEntity.getY() - dRayY; y > target.getY(); y -= dRayY) {
            double dist = Math.tan(90.0F + diveAngle) * (this.theEntity.getY() - y);
            double x = -Math.sin(this.theEntity.getYRot() / 180.0F * 3.141592653589793D) * dist;
            double z = Math.cos(this.theEntity.getYRot() / 180.0F * 3.141592653589793D) * dist;
            Vec3 source = new Vec3(x, y, z);
            HitResult collide = this.theEntity.worldObj.rayTraceBlocks(source, target.position());
            if (collide != null) {
                if (hitCount == 0) {
                    lowestCollide = y;
                }
                hitCount++;
            }
        }

        if (isAcceptableDiveSpace(this.theEntity.getY(), lowestCollide, hitCount)) {
            return true;
        }

        return false;
    }

    private boolean isFinalRunLinedUp() {
        double dX = this.swoopTarget.getX() - this.theEntity.getX();
        double dY = this.swoopTarget.getY() - this.theEntity.getY();
        double dZ = this.swoopTarget.getZ() - this.theEntity.getZ();
        double dXZ = Math.sqrt(dX * dX + dZ * dZ);
        double yawToTarget = Math.atan2(dZ, dX) * 180.0D / 3.141592653589793D - 90.0D;
        double dYaw = MathUtil.boundAngle180Deg(yawToTarget - this.theEntity.getYRot());
        if ((dYaw < -this.finalRunArcLimit) || (dYaw > this.finalRunArcLimit)) {
            return false;
        }
        double dPitch = Math.atan(dY / dXZ) * 180.0D / 3.141592653589793D - this.theEntity.getXRot();
        if ((dPitch < -this.finalRunArcLimit) || (dPitch > this.finalRunArcLimit)) {
            return false;
        }
        return true;
    }

    protected boolean isAcceptableDiveSpace(double entityPosY, double lowestCollideY, int hitCount) {
        double clearanceY = entityPosY - lowestCollideY;
        if (clearanceY < this.minDiveClearanceY) {
            return false;
        }
        return true;
    }
}
