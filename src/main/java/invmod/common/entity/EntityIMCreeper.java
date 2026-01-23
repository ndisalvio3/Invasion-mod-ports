package invmod.common.entity;

import invmod.Invasion;
import invmod.common.INotifyTask;
import invmod.common.entity.ai.*;
import invmod.common.nexus.INexusAccess;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

//NOOB HAUS  this.c.addTask(0, new EntityAISwimming(this));   c = this.tasks...
//Noob HAUS  this.d.addTask(2, new EntityAIHurtByTarget(this, false));   d = this.targetTasks

//public class EntityIMCreeper extends EntityIMMob implements ILeader
public class EntityIMCreeper extends EntityIMMob {
    private static final EntityDataAccessor<Byte> DATA_CREEPER_STATE = SynchedEntityData.defineId(EntityIMCreeper.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_POWERED = SynchedEntityData.defineId(EntityIMCreeper.class, EntityDataSerializers.BYTE);
    private int timeSinceIgnited;
    private int lastActiveTime;
    private boolean explosionDeath;
    private boolean commitToExplode;
    private int explodeDirection;
    private int tier;

    public EntityIMCreeper(EntityType<? extends EntityIMCreeper> type, Level level) {
        this(type, level, null);
    }

    public EntityIMCreeper(EntityType<? extends EntityIMCreeper> type, Level level, INexusAccess nexus) {
        super(type, level);
        if (nexus != null) {
            acquiredByNexus(nexus);
        }
        this.setName("Creeper");
        this.setGender(0);
        this.tier = 1;
        setBaseMoveSpeedStat(0.21F);
        setMaxHealthAndHealth(Invasion.getMobHealth(this));
        setAI();
    }

    private void setAI() {
        this.tasks.addTask(0, new FloatGoal(this));
        this.tasks.addTask(1, new EntityAICreeperIMSwell(this));
        this.tasks.addTask(2, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 0.25D, 0.300000011920929D));
        this.tasks.addTask(3, new EntityAIKillEntity(this, Player.class, 40));
        this.tasks.addTask(3, new EntityAIKillEntity(this, ServerPlayer.class, 40));
        this.tasks.addTask(4, new EntityAIAttackNexus(this));
        this.tasks.addTask(5, new EntityAIWaitForEngy(this, 4.0F, true));
        this.tasks.addTask(6, new EntityAIKillEntity(this, LivingEntity.class, 40));
        this.tasks.addTask(7, new EntityAIGoToNexus(this));
        this.tasks.addTask(8, new EntityAIWanderIM(this));
        this.tasks.addTask(9, new LookAtPlayerGoal(this, Player.class, 4.8F));
        this.tasks.addTask(9, new RandomLookAroundGoal(this));
        this.targetTasks.addTask(0, new EntityAITargetRetaliate(this, LivingEntity.class, 12.0F));
        if (this.isNexusBound()) {
            this.targetTasks.addTask(1, new EntityAISimpleTarget(this, Player.class, 20.0F, true));
        } else {
            this.targetTasks.addTask(1, new EntityAISimpleTarget(this, Player.class, this.getSenseRange(), false));
            this.targetTasks.addTask(2, new EntityAISimpleTarget(this, Player.class, this.getAggroRange(), true));
        }
        this.targetTasks.addTask(3, new HurtByTargetGoal(this));
    }

    @Override
    public boolean onPathBlocked(Path path, INotifyTask notifee) {
        if (!path.isFinished()) {
            PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
            double dX = node.xCoord + 0.5D - this.getX();
            double dZ = node.zCoord + 0.5D - this.getZ();
            float facing = (float) (Math.atan2(dZ, dX) * 180.0D / 3.141592653589793D) - 90.0F;
            if (facing < 0.0F) {
                facing += 360.0F;
            }
            facing %= 360.0F;

            if ((facing >= 45.0F) && (facing < 135.0F))
                this.explodeDirection = 1;
            else if ((facing >= 135.0F) && (facing < 225.0F))
                this.explodeDirection = 3;
            else if ((facing >= 225.0F) && (facing < 315.0F))
                this.explodeDirection = 0;
            else {
                this.explodeDirection = 2;
            }
            setCreeperState(1);
            this.commitToExplode = true;
        }
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CREEPER_STATE, (byte) -1);
        builder.define(DATA_POWERED, (byte) 0);
    }

    @Override
    public void tick() {
        if (this.explosionDeath) {
            doExplosion();
            discard();
        } else if (isAlive()) {
            this.lastActiveTime = this.timeSinceIgnited;
            int state = getCreeperState();

            if (state > 0) {
                if (this.commitToExplode) {
                    getMoveHelper().setMoveTo(this.getX() + invmod.common.util.CoordsInt.offsetAdjX[this.explodeDirection], this.getY(), this.getZ() + invmod.common.util.CoordsInt.offsetAdjZ[this.explodeDirection], 0.0D);
                }
                if (this.timeSinceIgnited == 0) {
                    this.worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
                }
            }
            this.timeSinceIgnited += state;
            if (this.timeSinceIgnited < 0) {
                this.timeSinceIgnited = 0;
            }
            if (this.timeSinceIgnited >= 30) {
                this.timeSinceIgnited = 30;
                this.explosionDeath = true;
            }
        }

        super.tick();
    }

//  @Override
//  public boolean isMartyr()
//  {
//    return this.explosionDeath;
//  }

    @Override
    public String getSpecies() {
        return "Creeper";
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (source.getEntity() instanceof Skeleton) {
            if (level() instanceof ServerLevel serverLevel) {
                spawnAtLocation(serverLevel, new ItemStack(Items.MUSIC_DISC_13));
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity par1Entity) {
        return true;
    }

    public float setCreeperFlashTime(float par1) {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * par1) / 28.0F;
    }

    @Override
    public float getBlockPathCost(PathNode prevNode, PathNode node, BlockGetter terrainMap) {
        BlockState state = terrainMap.getBlockState(new BlockPos(node.xCoord, node.yCoord, node.zCoord));
        Block block = state.getBlock();
        BlockPos pos = new BlockPos(node.xCoord, node.yCoord, node.zCoord);
        if (!state.isAir() && !state.isCollisionShapeFullBlock(terrainMap, pos) && (block != Invasion.blockNexus)) {
            return prevNode.distanceTo(node) * 12.0F;
        }

        return super.getBlockPathCost(prevNode, node, terrainMap);
    }

    @Override
    public String toString() {
        return "IMCreeper-T" + this.getTier();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        spawnAtLocation(level, new ItemStack(Items.GUNPOWDER, 1));
    }

    protected void doExplosion() {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 2.1F, Level.ExplosionInteraction.MOB);
        }
    }

    public int getCreeperState() {
        return entityData.get(DATA_CREEPER_STATE);
    }

    public void setCreeperState(int state) {
        if ((this.commitToExplode) && (state != 1)) {
            return;
        }
        entityData.set(DATA_CREEPER_STATE, (byte) state);
    }

}
