package invmod.common.entity;

import invmod.common.util.IPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public class IMMoveHelper extends MoveControl {
    protected EntityIMLiving a;
    protected double b;
    protected double c;
    protected double d;
    protected double setSpeed;
    protected double targetSpeed;
    protected boolean needsUpdate;
    protected boolean isRunning;

    public IMMoveHelper(EntityIMLiving par1EntityLiving) {
        super(par1EntityLiving);
        this.needsUpdate = false;
        this.a = par1EntityLiving;
        this.b = par1EntityLiving.getX();
        this.c = par1EntityLiving.getY();
        this.d = par1EntityLiving.getZ();
        this.setSpeed = (this.targetSpeed = 0.0D);
    }

    public boolean isUpdating() {
        return this.needsUpdate;
    }

    public double getSpeed() {
        return this.setSpeed;
    }

    public void setMoveSpeed(float speed) {
        this.setSpeed = speed;
    }

    public void setMoveTo(IPosition pos, float speed) {
        setMoveTo(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), speed);
    }

    public void setMoveTo(double x, double y, double z, double speed) {
        this.b = x;
        this.c = y;
        this.d = z;
        this.setSpeed = speed;
        this.needsUpdate = true;
    }

    public void onUpdateMoveHelper() {
        if (!this.needsUpdate) {
            this.a.setZza(0.0F);
            this.a.setMoveState(MoveState.STANDING);
            return;
        }

        MoveState result = doGroundMovement();
        this.a.setMoveState(result);
    }

    @Override
    public void tick() {
        onUpdateMoveHelper();
    }

    protected MoveState doGroundMovement() {
        this.needsUpdate = false;
        this.targetSpeed = this.setSpeed;
        boolean isInLiquid = this.a.isInWater() || this.a.isInLava();
        double dX = this.b - this.a.getX();
        double dZ = this.d - this.a.getZ();
        double dY = this.c - (!isInLiquid ? Mth.floor(this.a.getBoundingBox().minY + 0.5D) : this.a.getY());

        float newYaw = (float) (Math.atan2(dZ, dX) * 180.0D / 3.141592653589793D) - 90.0F;
        int ladderPos = -1;
        if ((Math.abs(dX) < 0.8D) && (Math.abs(dZ) < 0.8D) && ((dY > 0.0D) || (this.a.isHoldingOntoLadder()))) {
            ladderPos = getClimbFace(this.a.getX(), this.a.getY(), this.a.getZ());
            if (ladderPos == -1) {
                ladderPos = getClimbFace(this.a.getX(), this.a.getY() + 1.0D, this.a.getZ());
            }

            switch (ladderPos) {
                case 0:
                    newYaw = (float) (Math.atan2(dZ, dX + 1.0D) * 180.0D / 3.141592653589793D) - 90.0F;
                    break;
                case 1:
                    newYaw = (float) (Math.atan2(dZ, dX - 1.0D) * 180.0D / 3.141592653589793D) - 90.0F;
                    break;
                case 2:
                    newYaw = (float) (Math.atan2(dZ + 1.0D, dX) * 180.0D / 3.141592653589793D) - 90.0F;
                    break;
                case 3:
                    newYaw = (float) (Math.atan2(dZ - 1.0D, dX) * 180.0D / 3.141592653589793D) - 90.0F;
            }
        }

        double dXZSq = dX * dX + dZ * dZ;
        double distanceSquared = dXZSq + dY * dY;
        if ((distanceSquared < 0.01D) && (ladderPos == -1)) {
            return MoveState.STANDING;
        }

        if ((dXZSq > 0.04D) || (ladderPos != -1)) {
            this.a.setYRot(correctRotation(this.a.getYRot(), newYaw, this.a.getTurnRate()));
            double moveSpeed;
            if ((distanceSquared >= 0.064D) || (this.a.isSprinting()))
                moveSpeed = this.targetSpeed;
            else {
                moveSpeed = this.targetSpeed * 0.5D;
            }
            if ((this.a.isInWater()) && (moveSpeed < 0.6D)) {
                moveSpeed = 0.6000000238418579D;
            }
            this.a.setSpeed((float) moveSpeed);
        }

        double w = Math.max(this.a.getBbWidth() * 0.5F + 1.0F, 1.0D);
        w = this.a.getBbWidth() * 0.5F + 1.0F;
        if ((dY > 0.0D) && ((dX * dX + dZ * dZ <= w * w) || (isInLiquid))) {
            this.a.getJumpHelper().setJumping();
            if (ladderPos != -1)
                return MoveState.CLIMBING;
        }
        return MoveState.RUNNING;
    }

    protected float correctRotation(float currentYaw, float newYaw, float turnSpeed) {
        float dYaw = newYaw - currentYaw;
        while (dYaw < -180.0F)
            dYaw += 360.0F;
        while (dYaw >= 180.0F)
            dYaw -= 360.0F;
        if (dYaw > turnSpeed)
            dYaw = turnSpeed;
        if (dYaw < -turnSpeed) {
            dYaw = -turnSpeed;
        }
        return currentYaw + dYaw;
    }

    protected int getClimbFace(double x, double y, double z) {
        int mobX = Mth.floor(x);
        int mobY = Mth.floor(y);
        int mobZ = Mth.floor(z);

        BlockPos pos = new BlockPos(mobX, mobY, mobZ);
        BlockState state = this.a.level().getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.LADDER) {
            Direction facing = state.getValue(LadderBlock.FACING);
            if (facing == Direction.NORTH)
                return 2;
            if (facing == Direction.SOUTH)
                return 3;
            if (facing == Direction.WEST)
                return 0;
            if (facing == Direction.EAST)
                return 1;
        } else if (block == Blocks.VINE) {
            if (state.getValue(VineBlock.NORTH))
                return 2;
            if (state.getValue(VineBlock.SOUTH))
                return 3;
            if (state.getValue(VineBlock.WEST))
                return 0;
            if (state.getValue(VineBlock.EAST))
                return 1;
        }
        return -1;
    }
}
