package invmod.common.entity;

import invmod.Invasion;
import invmod.common.IBlockAccessExtended;
import invmod.common.INotifyTask;
import invmod.common.entity.ai.*;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.CoordsInt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;

public class EntityIMPigEngy extends EntityIMMob implements ICanDig {
    private static final int MAX_LADDER_TOWER_HEIGHT = 4;
    private static final int META_ITEM_ID_HELD = 30;
    private static final int META_SWINGING = 31;
    private static final EntityDataAccessor<ItemStack> DATA_HELD_ITEM = SynchedEntityData.defineId(EntityIMPigEngy.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Byte> DATA_SWINGING = SynchedEntityData.defineId(EntityIMPigEngy.class, EntityDataSerializers.BYTE);
    private final NavigatorEngy bo;
    private final PathNavigateAdapter oldNavAdapter;
    private int swingTimer;
    private int planks;
    private int askForScaffoldTimer;
    private int tier;
    private float supportThisTick;
    private TerrainModifier terrainModifier;
    private TerrainDigger terrainDigger;
    private TerrainBuilder terrainBuilder;
    private ItemStack currentItem;

    public EntityIMPigEngy(EntityType<? extends EntityIMPigEngy> type, Level level) {
        this(type, level, null);
    }

    public EntityIMPigEngy(EntityType<? extends EntityIMPigEngy> type, Level level, INexusAccess nexus) {
        super(type, level);
        if (nexus != null) {
            acquiredByNexus(nexus);
        }
        IPathSource pathSource = getPathSource();
        pathSource.setSearchDepth(1500);
        pathSource.setQuickFailDepth(1500);
        this.bo = new NavigatorEngy(this, pathSource);
        this.oldNavAdapter = new PathNavigateAdapter(this.bo);
        pathSource.setSearchDepth(1200);

        this.terrainModifier = new TerrainModifier(this, 2.8F);
        this.terrainDigger = new TerrainDigger(this, this.terrainModifier, 1.0F);
        this.terrainBuilder = new TerrainBuilder(this, this.terrainModifier, 1.0F);

        setBaseMoveSpeedStat(0.23F);
        this.attackStrength = 2;
        this.selfDamage = 0;
        this.maxSelfDamage = 0;
        this.planks = 15;
        this.tier = 1;
        this.maxDestructiveness = 2;
        this.askForScaffoldTimer = 0;

        entityData.set(DATA_HELD_ITEM, new ItemStack(Items.IRON_PICKAXE, 1));
        entityData.set(DATA_SWINGING, (byte) 0);

        setMaxHealthAndHealth(Invasion.getMobHealth(this));
        setName("Pigman Engineer");
        setGender(1);
        setDestructiveness(2);
        setJumpHeight(1);
        setCanClimb(false);
        setAI();

        int r = this.random.nextInt(3);
        if (r == 0)
            setCurrentItem(new ItemStack(Blocks.LADDER, 1));
        else if (r == 1)
            setCurrentItem(new ItemStack(Items.IRON_PICKAXE, 1));
        else
            setCurrentItem(new ItemStack(Invasion.itemEngyHammer, 1));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HELD_ITEM, ItemStack.EMPTY);
        builder.define(DATA_SWINGING, (byte) 0);
    }

    public static boolean canPlaceLadderAt(BlockGetter map, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (EntityIMLiving.unDestructableBlocks.contains(map.getBlockState(pos).getBlock())) {
            if ((map.getBlockState(new BlockPos(x + 1, y, z)).isCollisionShapeFullBlock(map, new BlockPos(x + 1, y, z)))
                || (map.getBlockState(new BlockPos(x - 1, y, z)).isCollisionShapeFullBlock(map, new BlockPos(x - 1, y, z)))
                || (map.getBlockState(new BlockPos(x, y, z + 1)).isCollisionShapeFullBlock(map, new BlockPos(x, y, z + 1)))
                || (map.getBlockState(new BlockPos(x, y, z - 1)).isCollisionShapeFullBlock(map, new BlockPos(x, y, z - 1)))) {
                return true;
            }
        }
        return false;
    }

    protected void setAI() {
        this.tasks.addTask(0, new FloatGoal(this));
        this.tasks.addTask(1, new EntityAIKillEntity(this, Player.class, 60));
        this.tasks.addTask(1, new EntityAIKillEntity(this, ServerPlayer.class, 60));
        this.tasks.addTask(2, new EntityAIAttackNexus(this));
        this.tasks.addTask(3, new EntityAIGoToNexus(this));
        this.tasks.addTask(7, new EntityAIWanderIM(this));
        this.tasks.addTask(8, new LookAtPlayerGoal(this, Player.class, 7.0F));
        this.tasks.addTask(9, new LookAtPlayerGoal(this, EntityIMCreeper.class, 12.0F));
        this.tasks.addTask(9, new RandomLookAroundGoal(this));

        if (this.isNexusBound()) {
            this.targetTasks.addTask(1, new EntityAISimpleTarget(this, Player.class, 3.0F, true));
        } else {
            this.targetTasks.addTask(1, new EntityAISimpleTarget(this, Player.class, this.getSenseRange(), false));
            this.targetTasks.addTask(2, new EntityAISimpleTarget(this, Player.class, this.getAggroRange(), true));
        }
        this.targetTasks.addTask(3, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.terrainModifier.onUpdate();
        this.terrainBuilder.setBuildRate(1.0F + this.supportThisTick * 0.33F);
        this.supportThisTick = 0.0F;

        this.askForScaffoldTimer -= 1;
        INexusAccess targetNexus = getNexus();
        if (targetNexus != null) {
            int weight = Math.max(6000 / targetNexus.getYCoord() - getYCoord(), 1);
            if ((getAIGoal() == Goal.BREAK_NEXUS) && (((getNavigatorNew().getLastPathDistanceToTarget() > 2.0F) && (this.askForScaffoldTimer <= 0)) || (this.random.nextInt(weight) == 0))) {
                if (targetNexus.getAttackerAI().askGenerateScaffolds(this)) {
                    getNavigatorNew().clearPath();
                    this.askForScaffoldTimer = 60;
                } else {
                    this.askForScaffoldTimer = 140;
                }
            }
        }

        updateAnimation();
    }

    public void onPathSet() {
        this.terrainModifier.cancelTask();
    }

    public PathNavigateAdapter getNavigator() {
        return this.oldNavAdapter;
    }

    @Override
    public INavigation getNavigatorNew() {
        return this.bo;
    }

    @Override
    public BlockGetter getTerrain() {
        return level();
    }

    @Override
    public boolean onPathBlocked(Path path, INotifyTask notifee) {
        if (!path.isFinished()) {
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
            return this.terrainDigger.askClearPosition(node.xCoord, node.yCoord, node.zCoord, notifee, 1.0F);
        }
        return false;
    }

    protected ITerrainBuild getTerrainBuildEngy() {
        return this.terrainBuilder;
    }

    protected ITerrainDig getTerrainDig() {
        return this.terrainDigger;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    public String getSpecies() {
        return "Pigman";
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public float getBlockRemovalCost(int x, int y, int z) {
        return getBlockStrength(x, y, z) * 20.0F;
    }

    private float getBlockStrength(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level().getBlockState(pos);
        float hardness = state.getDestroySpeed(level(), pos);
        return hardness < 0.0F ? 0.0F : hardness;
    }

    private Block getBlock(BlockGetter map, int x, int y, int z) {
        return map.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    private boolean isNormalCube(BlockGetter map, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return map.getBlockState(pos).isCollisionShapeFullBlock(map, pos);
    }

    private boolean blocksMovement(BlockGetter map, int x, int y, int z) {
        return map.getBlockState(new BlockPos(x, y, z)).blocksMotion();
    }

    private boolean isBlockDestructible(BlockGetter map, int x, int y, int z, Block block) {
        BlockPos pos = new BlockPos(x, y, z);
        return isBlockDestructible(map.getBlockState(pos), pos, map);
    }

    @Override
    public invmod.common.util.IPosition[] getBlockRemovalOrder(int x, int y, int z) {
        return new invmod.common.util.IPosition[]{new invmod.common.util.CoordsInt(x, y, z)};
    }

    @Override
    public boolean canClearBlock(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level().getBlockState(pos);
        return state.isAir() || isBlockDestructible(state, pos, level());
    }

    @Override
    public void onBlockRemoved(int x, int y, int z, Block block) {
        // no-op for now
    }

    public boolean avoidsBlock(int id) {
        if ((id == 51) || (id == 7) || (id == 64) || (id == 8) || (id == 9) || (id == 10) || (id == 11)) {
            return true;
        }

        return false;
    }

    public void supportForTick(EntityIMLiving entity, float amount) {
        this.supportThisTick += amount;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockGetter terrainMap) {
        if ((node.xCoord == -21) && (node.zCoord == 180)) {
            this.planks = 10;
        }
        BlockState state = terrainMap.getBlockState(new BlockPos(node.xCoord, node.yCoord, node.zCoord));
        Block block = state.getBlock();
        float materialMultiplier = (block != Blocks.AIR) && (isBlockDestructible(terrainMap, node.xCoord, node.yCoord, node.zCoord, block)) ? 3.2F : 1.0F;

        if (node.action == PathAction.BRIDGE)
            return prevNode.distanceTo(node) * 1.7F * materialMultiplier;
        if (node.action == PathAction.SCAFFOLD_UP)
            return prevNode.distanceTo(node) * 0.5F;
        if ((node.action == PathAction.LADDER_UP_NX) || (node.action == PathAction.LADDER_UP_NZ) || (node.action == PathAction.LADDER_UP_PX) || (node.action == PathAction.LADDER_UP_PZ))
            return prevNode.distanceTo(node) * 1.3F * materialMultiplier;
        if ((node.action == PathAction.LADDER_TOWER_UP_PX) || (node.action == PathAction.LADDER_TOWER_UP_NX) || (node.action == PathAction.LADDER_TOWER_UP_PZ) || (node.action == PathAction.LADDER_TOWER_UP_NZ)) {
            return prevNode.distanceTo(node) * 1.4F;
        }

        float multiplier = 1.0F;
        if ((terrainMap instanceof IBlockAccessExtended)) {
            int mobDensity = ((IBlockAccessExtended) terrainMap).getLayeredData(node.xCoord, node.yCoord, node.zCoord) & 0x7;
            multiplier += mobDensity;
        }
        if (block == Blocks.AIR) {
            return prevNode.distanceTo(node) * 1.0F * multiplier;
        }
        if (block == Blocks.SNOW) {
            return prevNode.distanceTo(node) * 1.0F * multiplier;
        }
        if (block == Blocks.LADDER) {
            return prevNode.distanceTo(node) * 1.0F * 0.7F * multiplier;
        }
        if ((!blocksMovement(terrainMap, node.xCoord, node.yCoord, node.zCoord)) && (block != Invasion.blockNexus)) {
            return prevNode.distanceTo(node) * 3.2F;
        }

        return super.getBlockPathCost(prevNode, node, terrainMap);
    }

    @Override
    public void getPathOptionsFromNode(BlockGetter terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        super.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
        if (this.planks <= 0) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (getCollide(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i]) > 0) {
                for (int yOffset = 0; yOffset > -4; yOffset--) {
                    Block block = getBlock(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord - 1 + yOffset, currentNode.zCoord + CoordsInt.offsetAdjZ[i]);
                    if (block != Blocks.AIR)
                        break;
                    pathFinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord + yOffset, currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.BRIDGE);
                }
            }
        }
    }

    @Override
    protected void calcPathOptionsVertical(BlockGetter terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if ((currentNode.xCoord == -11) && (currentNode.zCoord == 177)) {
            this.planks = 10;
        }
        super.calcPathOptionsVertical(terrainMap, currentNode, pathFinder);
        if (this.planks <= 0) {
            return;
        }

        if (getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) > 0) {
            if (getBlock(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) == Blocks.AIR) {
                if (currentNode.action == PathAction.NONE) {
                    addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                } else if (!continueLadder(terrainMap, currentNode, pathFinder)) {
                    addAnyLadderPoint(terrainMap, currentNode, pathFinder);
                }

            }

            if ((currentNode.action == PathAction.NONE) || (currentNode.action == PathAction.BRIDGE)) {
                int maxHeight = 4;
                for (int i = getCollideSize().getYCoord(); i < 4; i++) {
                    Block block = getBlock(terrainMap, currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord);
                    if ((block != Blocks.AIR) && (!blocksMovement(terrainMap, currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord))) {
                        maxHeight = i - getCollideSize().getYCoord();
                        break;
                    }

                }

                for (int i = 0; i < 4; i++) {
                    Block block = getBlock(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord - 1, currentNode.zCoord + CoordsInt.offsetAdjZ[i]);
                    if (isNormalCube(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord - 1, currentNode.zCoord + CoordsInt.offsetAdjZ[i])) {
                        for (int height = 0; height < maxHeight; height++) {
                            block = getBlock(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord + height, currentNode.zCoord + CoordsInt.offsetAdjZ[i]);
                            if (block != Blocks.AIR) {
                                if (!isNormalCube(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord + height, currentNode.zCoord + CoordsInt.offsetAdjZ[i]))
                                    break;
                                pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.ladderTowerIndexOrient[i]);
                                break;
                            }
                        }
                    }
                }
            }

        }

        if ((terrainMap instanceof IBlockAccessExtended)) {
            int data = ((IBlockAccessExtended) terrainMap).getLayeredData(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
            if (data == 16384) {
                pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.SCAFFOLD_UP);
            }
        }
    }

    protected void addAnyLadderPoint(BlockGetter terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        for (int i = 0; i < 4; i++) {
            if (isNormalCube(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord + 1, currentNode.zCoord + CoordsInt.offsetAdjZ[i]))
                pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.ladderIndexOrient[i]);
        }
    }

    //NOOB HAUS: possible cases?  LADDER_UP_PX, LADDER_UP_NX, LADDER_UP_PZ, LADDER_UP_NZ, LADDER_TOWER_UP_PX,
//	  LADDER_TOWER_UP_NX, LADDER_TOWER_UP_PZ, LADDER_TOWER_UP_NZ, SCAFFOLD_UP
    protected boolean continueLadder(BlockGetter terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        switch (currentNode.action) {
            case LADDER_TOWER_UP_PX:
                if (isNormalCube(terrainMap, currentNode.xCoord + 1, currentNode.yCoord + 1, currentNode.zCoord)) {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_PX);
                }
                return true;
            case LADDER_TOWER_UP_NX:
                if (isNormalCube(terrainMap, currentNode.xCoord - 1, currentNode.yCoord + 1, currentNode.zCoord)) {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_NX);
                }
                return true;
            case LADDER_TOWER_UP_PZ:
                if (isNormalCube(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord + 1)) {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_PZ);
                }
                return true;
            case LADDER_TOWER_UP_NZ:
                if (isNormalCube(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord - 1)) {
                    pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_NZ);
                }
                return true;
        }

        return false;
    }

    public ItemStack getHeldItem() {
        return getCurrentItem();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        if (this.random.nextInt(2) == 0) {
            spawnAtLocation(level, new ItemStack(Items.LEATHER, 1));
        } else if (isOnFire()) {
            spawnAtLocation(level, new ItemStack(Items.COOKED_PORKCHOP, 1));
        } else {
            spawnAtLocation(level, new ItemStack(Items.PORKCHOP, 1));
        }
    }

    protected void updateAnimation() {
        if ((!this.worldObj.isRemote) && (this.terrainModifier.isBusy())) {
            setSwinging(true);
            PathAction currentAction = getNavigatorNew().getCurrentWorkingAction();
            if (currentAction == PathAction.NONE)
                setCurrentItem(new ItemStack(Items.IRON_PICKAXE, 1));
            else {
                setCurrentItem(new ItemStack(Invasion.itemEngyHammer, 1));
            }
        }
        int swingSpeed = getSwingSpeed();
        if (isSwinging()) {
            this.swingTimer += 1;
            if (this.swingTimer >= swingSpeed) {
                this.swingTimer = 0;
                setSwinging(false);
            }
        } else {
            this.swingTimer = 0;
        }

        // swingProgress handled by vanilla animation
    }

    protected boolean isSwinging() {
        return entityData.get(DATA_SWINGING) != 0;
    }

    protected void setSwinging(boolean flag) {
        entityData.set(DATA_SWINGING, (byte) (flag ? 1 : 0));
    }

    protected int getSwingSpeed() {
        return 10;
    }

    protected ItemStack getCurrentItem() {
        return entityData.get(DATA_HELD_ITEM);
    }

    protected void setCurrentItem(ItemStack item) {
        this.currentItem = item;
        entityData.set(DATA_HELD_ITEM, item);
    }

    @Override
    public String toString() {
        return "IMPigManEngineer-T" + this.getTier();
    }

}
