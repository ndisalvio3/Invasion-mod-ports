package invmod.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class IMMoveHelperFlying extends IMMoveHelper {
    private final EntityIMFlying a;
    private double targetFlySpeed;
    private boolean wantsToBeFlying;

    public IMMoveHelperFlying(EntityIMFlying entity) {
        super(entity);
        this.a = entity;
        this.wantsToBeFlying = false;
    }

    public void setHeading(float yaw, float pitch, float idealSpeed, int time) {
        double x = this.a.getX() + Math.sin(Math.toRadians(yaw)) * idealSpeed * time;
        double y = this.a.getY() + Math.sin(Math.toRadians(pitch)) * idealSpeed * time;
        double z = this.a.getZ() + Math.cos(Math.toRadians(yaw)) * idealSpeed * time;
        setMoveTo(x, y, z, idealSpeed);
    }

    public void setWantsToBeFlying(boolean flag) {
        this.wantsToBeFlying = flag;
    }

    public void onUpdateMoveHelper() {
        this.a.setZza(0.0F);
        this.a.setFlightAccelerationVector(0.0F, 0.0F, 0.0F);
        if (!this.needsUpdate && this.a.getMoveState() != MoveState.FLYING) {
            this.a.setMoveState(MoveState.STANDING);
            this.a.setFlyState(FlyState.GROUNDED);
            this.a.setXRot(correctRotation(this.a.getXRot(), 50.0F, 4.0F));
            return;
        }
        this.needsUpdate = false;

        if (this.wantsToBeFlying) {
            if (this.a.getFlyState() == FlyState.GROUNDED) {
                this.a.setMoveState(MoveState.RUNNING);
                this.a.setFlyState(FlyState.TAKEOFF);
            } else if (this.a.getFlyState() == FlyState.FLYING) {
                this.a.setMoveState(MoveState.FLYING);
            }
        } else if (this.a.getFlyState() == FlyState.FLYING) {
            this.a.setFlyState(FlyState.LANDING);
        }

        if (this.a.getFlyState() == FlyState.FLYING) {
            FlyState result = doFlying();
            if (result == FlyState.GROUNDED) {
                this.a.setMoveState(MoveState.STANDING);
            } else if (result == FlyState.FLYING) {
                this.a.setMoveState(MoveState.FLYING);
            }
            this.a.setFlyState(result);
        } else if (this.a.getFlyState() == FlyState.TAKEOFF) {
            FlyState result = doTakeOff();
            if (result == FlyState.GROUNDED) {
                this.a.setMoveState(MoveState.STANDING);
            } else if (result == FlyState.TAKEOFF) {
                this.a.setMoveState(MoveState.RUNNING);
            } else if (result == FlyState.FLYING) {
                this.a.setMoveState(MoveState.FLYING);
            }
            this.a.setFlyState(result);
        } else if (this.a.getFlyState() == FlyState.LANDING || this.a.getFlyState() == FlyState.TOUCHDOWN) {
            FlyState result = doLanding();
            if (result == FlyState.GROUNDED || result == FlyState.TOUCHDOWN) {
                this.a.setMoveState(MoveState.RUNNING);
            }
            this.a.setFlyState(result);
        } else {
            MoveState result = doGroundMovement();
            this.a.setMoveState(result);
        }
    }

    protected MoveState doGroundMovement() {
        this.a.setGroundFriction(0.6F);
        this.a.setRotationRoll(correctRotation(this.a.getRotationRoll(), 0.0F, 6.0F));
        this.targetSpeed = this.a.getMoveSpeedStat();
        this.a.setXRot(correctRotation(this.a.getXRot(), 50.0F, 4.0F));
        return super.doGroundMovement();
    }

    protected FlyState doFlying() {
        this.targetFlySpeed = this.setSpeed;
        return fly();
    }

    protected FlyState fly() {
        this.a.setGroundFriction(1.0F);
        boolean isInLiquid = this.a.isInWater() || this.a.isInLava();
        double dX = this.b - this.a.getX();
        double dZ = this.d - this.a.getZ();
        double dY = this.c - this.a.getY();

        double dXZSq = dX * dX + dZ * dZ;
        double dXZ = Math.sqrt(dXZSq);
        double distanceSquared = dXZSq + dY * dY;

        if (distanceSquared > 0.04D) {
            int timeToTurn = 10;
            float gravity = this.a.getGravity();
            float liftConstant = gravity;
            double xAccel = 0.0D;
            double yAccel = 0.0D;
            double zAccel = 0.0D;
            Vec3 velocity = this.a.getDeltaMovement();
            double velX = velocity.x;
            double velY = velocity.y;
            double velZ = velocity.z;
            double hSpeedSq = velX * velX + velZ * velZ;
            if (hSpeedSq == 0.0D) {
                hSpeedSq = 1.0E-8D;
            }
            double horizontalSpeed = Math.sqrt(hSpeedSq);
            double flySpeed = Math.sqrt(hSpeedSq + velY * velY);

            double desiredYVelocity = dY / timeToTurn;
            double dVelY = desiredYVelocity - (velY - gravity);

            float minFlightSpeed = 0.05F;
            if (flySpeed < minFlightSpeed) {
                float newYaw = (float) (Math.atan2(dZ, dX) * 180.0D / Math.PI - 90.0D);
                newYaw = correctRotation(this.a.getYRot(), newYaw, this.a.getTurnRate());
                this.a.setYRot(newYaw);
                if (this.a.onGround()) {
                    return FlyState.GROUNDED;
                }
            } else {
                double liftForce = flySpeed / (this.a.getMaxPoweredFlightSpeed() * this.a.getLiftFactor()) * liftConstant;
                double climbForce = liftForce * horizontalSpeed / (Math.abs(velY) + horizontalSpeed);
                double forwardForce = liftForce * Math.abs(velY) / (Math.abs(velY) + horizontalSpeed);
                double turnForce = liftForce;
                double climbAccel;
                if (dVelY < 0.0D) {
                    double maxDiveForce = this.a.getMaxTurnForce() - gravity;
                    climbAccel = -Math.min(Math.min(climbForce, maxDiveForce), -dVelY);
                } else {
                    double maxClimbForce = this.a.getMaxTurnForce() + gravity;
                    climbAccel = Math.min(Math.min(climbForce, maxClimbForce), dVelY);
                }

                float minBankForce = 0.01F;
                if (turnForce < minBankForce) {
                    turnForce = minBankForce;
                }

                double desiredXZHeading = Math.atan2(dZ, dX) - 1.570796326794897D;
                double currXZHeading = Math.atan2(velZ, velX) - 1.570796326794897D;
                double dXZHeading = desiredXZHeading - currXZHeading;
                while (dXZHeading >= Math.PI) {
                    dXZHeading -= Math.PI * 2.0D;
                }
                while (dXZHeading < -Math.PI) {
                    dXZHeading += Math.PI * 2.0D;
                }
                double bankForce = horizontalSpeed * dXZHeading / timeToTurn;
                double maxBankForce = Math.min(turnForce, this.a.getMaxTurnForce());
                if (bankForce > maxBankForce) {
                    bankForce = maxBankForce;
                } else if (bankForce < -maxBankForce) {
                    bankForce = -maxBankForce;
                }

                double bankXAccel = bankForce * -velZ / horizontalSpeed;
                double bankZAccel = bankForce * velX / horizontalSpeed;

                double totalForce = xAccel + yAccel + zAccel;
                double r = liftForce / totalForce;
                xAccel += bankXAccel;
                yAccel += climbAccel;
                zAccel += bankZAccel;
                velX += bankXAccel;
                velY += climbAccel;
                velZ += bankZAccel;

                double middlePitch = 15.0D;
                double newPitch;
                if (velY - gravity < 0.0D) {
                    double climbForceRatio = yAccel / climbForce;
                    if (climbForceRatio > 1.0D) {
                        climbForceRatio = 1.0D;
                    } else if (climbForceRatio < -1.0D) {
                        climbForceRatio = -1.0D;
                    }
                    double xzSpeed = Math.sqrt(velX * velX + velZ * velZ);
                    double velPitch = xzSpeed > 0.0D ? Math.atan(velY / xzSpeed) / Math.PI * 180.0D : -180.0D;
                    double pitchInfluence = (this.a.getMaxPoweredFlightSpeed() - Math.abs(velY)) / this.a.getMaxPoweredFlightSpeed();
                    if (pitchInfluence < 0.0D) {
                        pitchInfluence = 0.0D;
                    }
                    newPitch = velPitch + 15.0D * climbForceRatio * pitchInfluence;
                } else {
                    double pitchLimit = this.a.getMaxPitch();
                    double climbForceRatio = Math.min(yAccel / climbForce, 1.0D);
                    newPitch = middlePitch + (pitchLimit - middlePitch) * climbForceRatio;
                }
                newPitch = correctRotation(this.a.getXRot(), (float) newPitch, 1.5F);
                double newYaw = Math.atan2(velZ, velX) * 180.0D / Math.PI - 90.0D;
                newYaw = correctRotation(this.a.getYRot(), (float) newYaw, this.a.getTurnRate());
                this.a.setYRot((float) newYaw);
                this.a.setXRot((float) newPitch);
                double newRoll = 60.0D * bankForce / turnForce;
                this.a.setRotationRoll(correctRotation(this.a.getRotationRoll(), (float) newRoll, 6.0F));
                double horizontalForce = velY > 0.0D ? -climbAccel : forwardForce;
                int xDirection = velX > 0.0D ? 1 : -1;
                int zDirection = velZ > 0.0D ? 1 : -1;
                double hComponentX = xDirection * velX / (xDirection * velX + zDirection * velZ);

                double xLiftAccel = xDirection * horizontalForce * hComponentX;
                double zLiftAccel = zDirection * horizontalForce * (1.0D - hComponentX);

                double loss = 0.4D;
                xLiftAccel += xDirection * -Math.abs(bankForce * loss) * hComponentX;
                zLiftAccel += zDirection * -Math.abs(bankForce * loss) * (1.0D - hComponentX);

                xAccel += xLiftAccel;
                zAccel += zLiftAccel;
            }

            if (flySpeed < this.targetFlySpeed) {
                this.a.setThrustEffort(0.6F);
                if (!this.a.isThrustOn()) {
                    this.a.setThrustOn(true);
                }
                double desiredVThrustRatio = (dVelY - yAccel) / this.a.getThrust();
                Vec3 thrust = calcThrust(desiredVThrustRatio);
                xAccel += thrust.x;
                yAccel += thrust.y;
                zAccel += thrust.z;
            } else if (flySpeed > this.targetFlySpeed * 1.8D) {
                this.a.setThrustEffort(1.0F);
                if (!this.a.isThrustOn()) {
                    this.a.setThrustOn(true);
                }
                double desiredVThrustRatio = (dVelY - yAccel) / (this.a.getThrust() * 10.0F);
                Vec3 thrust = calcThrust(desiredVThrustRatio);
                xAccel += -thrust.x;
                yAccel += thrust.y;
                zAccel += -thrust.z;
            } else if (this.a.isThrustOn()) {
                this.a.setThrustOn(false);
            }

            this.a.setFlightAccelerationVector((float) xAccel, (float) yAccel, (float) zAccel);
        }
        return FlyState.FLYING;
    }

    protected FlyState doTakeOff() {
        this.a.setGroundFriction(0.98F);
        this.a.setThrustOn(true);
        this.a.setThrustEffort(1.0F);
        this.targetSpeed = this.a.getMoveSpeedStat();

        MoveState result = doGroundMovement();
        if (result == MoveState.STANDING) {
            return FlyState.GROUNDED;
        }
        if (this.a.horizontalCollision) {
            this.a.getJumpHelper().setJumping();
        }
        Vec3 thrust = calcThrust(0.0D);
        this.a.setFlightAccelerationVector((float) thrust.x, (float) thrust.y, (float) thrust.z);
        Vec3 velocity = this.a.getDeltaMovement();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z);

        this.a.setXRot(correctRotation(this.a.getXRot(), 40.0F, 4.0F));

        float gravity = this.a.getGravity();
        float liftConstant = gravity;
        double liftForce = speed / (this.a.getMaxPoweredFlightSpeed() * this.a.getLiftFactor()) * liftConstant;

        if (liftForce > gravity) {
            return FlyState.FLYING;
        }
        return FlyState.TAKEOFF;
    }

    protected FlyState doLanding() {
        this.a.setGroundFriction(0.3F);
        BlockPos pos = BlockPos.containing(this.a.getX(), this.a.getY(), this.a.getZ());

        for (int i = 1; i < 5; i++) {
            if (!this.a.level().getBlockState(pos.below(i)).isAir()) {
                break;
            }
            this.targetFlySpeed = this.setSpeed * (0.66F - (0.4F - (i - 1) * 0.133F));
        }

        FlyState result = fly();
        this.a.setThrustOn(true);
        if (result == FlyState.FLYING) {
            Vec3 velocity = this.a.getDeltaMovement();
            double speed = Math.sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z);
            if (this.a.onGround()) {
                if (speed < this.a.getLandingSpeedThreshold()) {
                    return FlyState.GROUNDED;
                }

                this.a.setRotationRoll(correctRotation(this.a.getRotationRoll(), 40.0F, 6.0F));
                return FlyState.TOUCHDOWN;
            }
        }

        return FlyState.LANDING;
    }

    protected Vec3 calcThrust(double desiredVThrustRatio) {
        float thrust = this.a.getThrust();
        float rMin = this.a.getThrustComponentRatioMin();
        float rMax = this.a.getThrustComponentRatioMax();
        double vThrustRatio = desiredVThrustRatio;
        if (vThrustRatio > rMax) {
            vThrustRatio = rMax;
        } else if (vThrustRatio < rMin) {
            vThrustRatio = rMin;
        }
        double hThrust = (1.0D - vThrustRatio) * thrust;
        double vThrust = vThrustRatio * thrust;
        double yawRad = Math.toRadians(this.a.getYRot());
        double xAccel = hThrust * -Math.sin(yawRad);
        double yAccel = vThrust;
        double zAccel = hThrust * Math.cos(yawRad);
        return new Vec3(xAccel, yAccel, zAccel);
    }
}
