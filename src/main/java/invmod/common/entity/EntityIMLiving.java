package invmod.common.entity;

import invmod.common.IPathfindable;
import invmod.common.SparrowAPI;
import invmod.common.nexus.INexusAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import invmod.common.util.CoordsInt;
import net.minecraft.world.phys.shapes.CollisionContext;

public abstract class EntityIMLiving extends PathfinderMob implements IHasNexus, IPathfindable, SparrowAPI {
    private INexusAccess nexus;
    private MoveState moveState = MoveState.NONE;

    protected EntityIMLiving(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public INexusAccess getNexus() {
        return nexus;
    }

    @Override
    public void acquiredByNexus(INexusAccess nexus) {
        this.nexus = nexus;
    }

    @Override
    public float getBlockPathCost(PathNode from, PathNode to, BlockGetter level) {
        float multiplier = 1.0F;
        if (level instanceof invmod.common.IBlockAccessExtended extended) {
            int mobDensity = extended.getLayeredData(to.xCoord, to.yCoord, to.zCoord) & 7;
            multiplier += mobDensity * 3.0F;
        }
        if (to.action == PathAction.DIG) {
            multiplier += 2.0F;
        }
        if (blockHasLadder(level, to.xCoord, to.yCoord, to.zCoord)) {
            multiplier += 5.0F;
        }
        if (to.action == PathAction.SWIM) {
            BlockPos above = new BlockPos(to.xCoord, to.yCoord + 1, to.zCoord);
            boolean enclosed = !level.getBlockState(above).isAir();
            multiplier *= (to.yCoord <= from.yCoord && enclosed) ? 3.0F : 1.0F;
            return from.distanceTo(to) * 1.3F * multiplier;
        }
        BlockPos pos = new BlockPos(to.xCoord, to.yCoord, to.zCoord);
        BlockState state = level.getBlockState(pos);
        if (!state.getCollisionShape(level, pos, CollisionContext.of(this)).isEmpty()) {
            return from.distanceTo(to) * 3.2F * multiplier;
        }
        return from.distanceTo(to) * multiplier;
    }

    @Override
    public void getPathOptionsFromNode(BlockGetter level, PathNode node, PathfinderIM pathfinder) {
        calcPathOptions(level, node, pathfinder);
    }

    protected void calcPathOptions(BlockGetter level, PathNode currentNode, PathfinderIM pathfinder) {
        calcPathOptionsVertical(level, currentNode, pathfinder);
        if (currentNode.action == PathAction.DIG && !canStandAt(level, currentNode.xCoord, currentNode.yCoord, currentNode.zCoord)) {
            return;
        }

        int height = getJumpHeight();
        for (int i = 1; i <= height; i++) {
            if (getCollide(level, currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord) == 0) {
                height = i - 1;
            }
        }

        int maxFall = 8;
        for (int i = 0; i < 4; i++) {
            if (currentNode.action != PathAction.NONE) {
                if (i == 0 && currentNode.action == PathAction.LADDER_UP_NX) {
                    height = 0;
                }
                if (i == 1 && currentNode.action == PathAction.LADDER_UP_PX) {
                    height = 0;
                }
                if (i == 2 && currentNode.action == PathAction.LADDER_UP_NZ) {
                    height = 0;
                }
                if (i == 3 && currentNode.action == PathAction.LADDER_UP_PZ) {
                    height = 0;
                }
            }
            int currentY = currentNode.yCoord + height;
            boolean passedLevel = false;
            do {
                int yOffset = getNextLowestSafeYOffset(level, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentY, currentNode.zCoord + CoordsInt.offsetAdjZ[i], maxFall + currentY - currentNode.yCoord);
                if (yOffset > 0) {
                    break;
                }
                if (yOffset > -maxFall) {
                    pathfinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentY + yOffset, currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.NONE);
                }
                currentY += yOffset - 1;
                if (!passedLevel && currentY <= currentNode.yCoord) {
                    passedLevel = true;
                    if (currentY != currentNode.yCoord) {
                        addAdjacent(level, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i], currentNode, pathfinder);
                    }
                }
            } while (currentY >= currentNode.yCoord);
        }

        if (canSwimHorizontal()) {
            for (int i = 0; i < 4; i++) {
                if (getCollide(level, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i]) == -1) {
                    pathfinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.SWIM);
                }
            }
        }
    }

    protected void calcPathOptionsVertical(BlockGetter level, PathNode currentNode, PathfinderIM pathfinder) {
        int collideUp = getCollide(level, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
        if (collideUp > 0) {
            BlockPos abovePos = new BlockPos(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
            BlockState aboveState = level.getBlockState(abovePos);
            if (aboveState.is(Blocks.LADDER)) {
                PathAction action = PathAction.NONE;
                if (aboveState.hasProperty(net.minecraft.world.level.block.LadderBlock.FACING)) {
                    switch (aboveState.getValue(net.minecraft.world.level.block.LadderBlock.FACING)) {
                        case EAST -> action = PathAction.LADDER_UP_PX;
                        case WEST -> action = PathAction.LADDER_UP_NX;
                        case SOUTH -> action = PathAction.LADDER_UP_PZ;
                        case NORTH -> action = PathAction.LADDER_UP_NZ;
                        default -> action = PathAction.NONE;
                    }
                }
                if (currentNode.action == PathAction.NONE) {
                    pathfinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
                } else if (currentNode.action == PathAction.LADDER_UP_PX || currentNode.action == PathAction.LADDER_UP_NX
                    || currentNode.action == PathAction.LADDER_UP_PZ || currentNode.action == PathAction.LADDER_UP_NZ) {
                    if (action == currentNode.action) {
                        pathfinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
                    }
                } else {
                    pathfinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
                }
            } else if (getCanClimb()) {
                if (isAdjacentSolidBlock(level, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord)) {
                    pathfinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.NONE);
                }
            }
        }

        int below = getCollide(level, currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord);
        int above = getCollide(level, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
        if (getCanDigDown()) {
            if (below == 2) {
                pathfinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.DIG);
            } else if (below == 1) {
                int maxFall = 5;
                int yOffset = getNextLowestSafeYOffset(level, currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, maxFall);
                if (yOffset <= 0) {
                    pathfinder.addNode(currentNode.xCoord, currentNode.yCoord - 1 + yOffset, currentNode.zCoord, PathAction.NONE);
                }
            }
        }

        if (canSwimVertical()) {
            if (below == -1) {
                pathfinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.SWIM);
            }
            if (above == -1) {
                pathfinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.SWIM);
            }
        }
    }

    protected void addAdjacent(BlockGetter level, int x, int y, int z, PathNode currentNode, PathfinderIM pathfinder) {
        if (getCollide(level, x, y, z) <= 0) {
            return;
        }
        if (getCanClimb()) {
            if (isAdjacentSolidBlock(level, x, y, z)) {
                pathfinder.addNode(x, y, z, PathAction.NONE);
            }
        } else if (level.getBlockState(new BlockPos(x, y, z)).is(Blocks.LADDER)) {
            pathfinder.addNode(x, y, z, PathAction.NONE);
        }
    }

    protected int getNextLowestSafeYOffset(BlockGetter level, int x, int y, int z, int maxOffsetMagnitude) {
        for (int i = 0; (i + y > level.getMinY()) && (i < maxOffsetMagnitude); i--) {
            if (canStandAtAndIsValid(level, x, y + i, z) || (canSwimHorizontal() && getCollide(level, x, y + i, z) == -1)) {
                return i;
            }
        }
        return 1;
    }

    protected boolean canStandAt(BlockGetter level, int x, int y, int z) {
        boolean isSolidBlock = false;
        int sizeX = getCollideSizeX();
        int sizeZ = getCollideSizeZ();
        for (int xOffset = x; xOffset < x + sizeX; xOffset++) {
            for (int zOffset = z; zOffset < z + sizeZ; zOffset++) {
                BlockPos pos = new BlockPos(xOffset, y - 1, zOffset);
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        if (!state.getCollisionShape(level, pos, CollisionContext.of(this)).isEmpty()) {
                            isSolidBlock = true;
                        } else if (avoidsBlock(state)) {
                            return false;
                        }
                }
            }
        }
        return isSolidBlock;
    }

    protected boolean canStandAtAndIsValid(BlockGetter level, int x, int y, int z) {
        return getCollide(level, x, y, z) > 0 && canStandAt(level, x, y, z);
    }

    protected int getCollide(BlockGetter level, int x, int y, int z) {
        boolean destructibleFlag = false;
        boolean liquidFlag = false;
        int sizeX = getCollideSizeX();
        int sizeY = getCollideSizeY();
        int sizeZ = getCollideSizeZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int xOffset = x; xOffset < x + sizeX; xOffset++) {
            for (int yOffset = y; yOffset < y + sizeY; yOffset++) {
                for (int zOffset = z; zOffset < z + sizeZ; zOffset++) {
                    pos.set(xOffset, yOffset, zOffset);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    if (!level.getFluidState(pos).isEmpty()) {
                        liquidFlag = true;
                        continue;
                    }
                    if (!state.getCollisionShape(level, pos, CollisionContext.of(this)).isEmpty()) {
                        if (isBlockDestructible(state, pos, level)) {
                            destructibleFlag = true;
                        } else {
                            return 0;
                        }
                    }
                    if (avoidsBlock(state)) {
                        return -2;
                    }
                }
            }
        }
        if (destructibleFlag) {
            return 2;
        }
        if (liquidFlag) {
            return -1;
        }
        return 1;
    }

    protected boolean isAdjacentSolidBlock(BlockGetter level, int x, int y, int z) {
        int sizeX = getCollideSizeX();
        int sizeZ = getCollideSizeZ();
        if (sizeX == 1 && sizeZ == 1) {
            for (int i = 0; i < 4; i++) {
                BlockPos pos = new BlockPos(x + CoordsInt.offsetAdjX[i], y, z + CoordsInt.offsetAdjZ[i]);
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.isCollisionShapeFullBlock(level, pos)) {
                    return true;
                }
            }
        } else if (sizeX == 2 && sizeZ == 2) {
            for (int i = 0; i < 8; i++) {
                BlockPos pos = new BlockPos(x + CoordsInt.offsetAdj2X[i], y, z + CoordsInt.offsetAdj2Z[i]);
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.isCollisionShapeFullBlock(level, pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean blockHasLadder(BlockGetter level, int x, int y, int z) {
        for (int i = 0; i < 4; i++) {
            BlockPos pos = new BlockPos(x + CoordsInt.offsetAdjX[i], y, z + CoordsInt.offsetAdjZ[i]);
            if (level.getBlockState(pos).is(Blocks.LADDER)) {
                return true;
            }
        }
        return false;
    }

    protected boolean avoidsBlock(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.FIRE || block == Blocks.LAVA || block == Blocks.BEDROCK || block == invmod.Invasion.blockNexus;
    }

    protected boolean isBlockDestructible(BlockState state, BlockPos pos, BlockGetter level) {
        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.LADDER || block == invmod.Invasion.blockNexus) {
            return false;
        }
        if (state.is(net.minecraft.tags.BlockTags.DOORS) || state.is(net.minecraft.tags.BlockTags.TRAPDOORS)) {
            return true;
        }
        if (!state.blocksMotion()) {
            return false;
        }
        return state.getDestroySpeed(level, pos) >= 0.0F;
    }

    protected int getJumpHeight() {
        return 1;
    }

    protected int getCollideSizeX() {
        return Math.max(1, Mth.floor(getBbWidth() + 1.0F));
    }

    protected int getCollideSizeY() {
        return Math.max(1, Mth.floor(getBbHeight() + 1.0F));
    }

    protected int getCollideSizeZ() {
        return Math.max(1, Mth.floor(getBbWidth() + 1.0F));
    }

    protected boolean getCanClimb() {
        return false;
    }

    protected boolean getCanDigDown() {
        return canDig();
    }

    protected boolean canDig() {
        return false;
    }

    protected boolean canSwimHorizontal() {
        return true;
    }

    protected boolean canSwimVertical() {
        return true;
    }

    @Override
    public boolean isStupidToAttack() {
        return false;
    }

    @Override
    public boolean doNotVaporize() {
        return false;
    }

    @Override
    public boolean isPredator() {
        return false;
    }

    @Override
    public boolean isHostile() {
        return true;
    }

    @Override
    public boolean isPeaceful() {
        return false;
    }

    @Override
    public boolean isPrey() {
        return false;
    }

    @Override
    public boolean isNeutral() {
        return false;
    }

    @Override
    public boolean isUnkillable() {
        return false;
    }

    @Override
    public boolean isThreatTo(Entity entity) {
        return false;
    }

    @Override
    public boolean isFriendOf(Entity entity) {
        return false;
    }

    @Override
    public boolean isNPC() {
        return false;
    }

    @Override
    public int isPet() {
        return 0;
    }

    @Override
    public Entity getPetOwner() {
        return null;
    }

    @Override
    public Component getName() {
        return getDisplayName();
    }

    @Override
    public Entity getAttackingTarget() {
        return null;
    }

    @Override
    public float getSize() {
        return getBbWidth();
    }

    @Override
    public String getSpecies() {
        return "invasion";
    }

    @Override
    public int getTier() {
        return 0;
    }

    @Override
    public int getGender() {
        return 0;
    }

    @Override
    public String customStringAndResponse(String input) {
        return "";
    }

    @Override
    public String getSimplyID() {
        return "";
    }

    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        setPos(x, y, z);
        setYRot(yaw);
        setXRot(pitch);
    }

    public boolean getCanSpawnHere() {
        return checkSpawnRules(level(), EntitySpawnReason.NATURAL) && checkSpawnObstruction(level());
    }

    public MoveState getMoveState() {
        return moveState;
    }

    public void setMoveState(MoveState moveState) {
        this.moveState = moveState;
    }

    public float getTurnRate() {
        return 30.0F;
    }

    public boolean isHoldingOntoLadder() {
        return onClimbable();
    }

    public int getXCoord() {
        return blockPosition().getX();
    }

    public int getYCoord() {
        return blockPosition().getY();
    }

    public int getZCoord() {
        return blockPosition().getZ();
    }

    public CoordsInt getCollideSize() {
        return new CoordsInt((int) Math.ceil(getBbWidth()), (int) Math.ceil(getBbHeight()), (int) Math.ceil(getBbWidth()));
    }
}
