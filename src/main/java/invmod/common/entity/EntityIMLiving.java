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
import net.minecraft.world.level.Level;
import invmod.common.util.CoordsInt;

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
        return 1.0F;
    }

    @Override
    public void getPathOptionsFromNode(BlockGetter level, PathNode node, PathfinderIM pathfinder) {
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
