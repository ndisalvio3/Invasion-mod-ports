package invmod.common.entity;

import invmod.Invasion;
import invmod.common.entity.ai.*;
import invmod.common.nexus.INexusAccess;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityIMGiantBird extends EntityIMBird {
    private static final float PICKUP_OFFSET_X = 0.0F;
    private static final float PICKUP_OFFSET_Y = 0.2F;
    private static final float PICKUP_OFFSET_Z = -0.92F;
    private static final float MODEL_ROTATION_OFFSET_Y = 1.9F;
    private static final byte TRIGGER_SQUAWK = 10;
    private static final byte TRIGGER_SCREECH = 10;
    private static final byte TRIGGER_DEATHSOUND = 10;
    private int tier;

    public EntityIMGiantBird(EntityType<? extends EntityIMGiantBird> type, Level level) {
        this(type, level, null);
    }

    public EntityIMGiantBird(EntityType<? extends EntityIMGiantBird> type, Level level, INexusAccess nexus) {
        super(type, level);
        if (nexus != null) {
            acquiredByNexus(nexus);
        }
        setName("Bird");
        setGender(2);

        this.tier = 1;
        setSize(1.9F, 2.8F);
        setGravity(0.03F);
        setThrust(0.028F);
        setMaxPoweredFlightSpeed(0.9F);
        setLiftFactor(0.35F);
        setThrustComponentRatioMin(0.0F);
        setThrustComponentRatioMax(0.5F);
        setMaxTurnForce(getLegacyGravity() * 8.0F);
        setMaxHealthAndHealth(Invasion.getMobHealth(this));
        setBaseMoveSpeedStat(0.4F);
        setAI();
        setDebugMode(1);
    }

    @Override
    public void tick() {
        super.tick();
        if ((getDebugMode() == 1) && (!level().isClientSide)) {
            setRenderLabel(getAIGoal() + "\n" + getNavString());
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public void updateRiderPosition() {
        Entity passenger = getFirstPassenger();
        if (passenger != null) {
            double x = 0.0D;
            double y = getMountedYOffset() - 1.899999976158142D;
            double z = -0.9200000166893005D;

            double dAngle = this.getXRot() / 180.0F * 3.141592653589793D;
            double sinF = Math.sin(dAngle);
            double cosF = Math.cos(dAngle);
            double tmp = z * cosF - y * sinF;
            y = y * cosF + z * sinF;
            z = tmp;

            dAngle = this.getYRot() / 180.0F * 3.141592653589793D;
            sinF = Math.sin(dAngle);
            cosF = Math.cos(dAngle);
            tmp = x * cosF - z * sinF;
            z = z * cosF + x * sinF;
            x = tmp;

            y += 1.899999976158142D;
            passenger.setPos(this.getX() + x, this.getY() + y, this.getZ() + z);
            passenger.setYRot(getCarriedEntityYawOffset() + this.getYRot());
        }
    }

    public boolean shouldRiderSit() {
        return false;
    }

    public double getMountedYOffset() {
        return -0.2000000029802322D;
    }

    @Override
    public void doScreech() {
        if (!level().isClientSide) {
            this.worldObj.playSoundAtEntity(this, "invmod:v_screech" + (random.nextInt(2) + 1), 6.0F, 1.0F + (this.random.nextFloat() * 0.2F - 0.1F));
            level().broadcastEntityEvent(this, TRIGGER_SCREECH);
        } else {
            setBeakState(35);
        }
    }

    @Override
    public void doMeleeSound() {
        doSquawk();
    }

    protected void doHurtSound() {
        doSquawk();
    }

    protected void doDeathSound() {
        if (!level().isClientSide) {
            this.worldObj.playSoundAtEntity(this, "invmod:v_death1", 1.9F, 1.0F + (this.random.nextFloat() * 0.2F - 0.1F));
            level().broadcastEntityEvent(this, TRIGGER_DEATHSOUND);
        } else {
            setBeakState(25);
        }
    }

    protected void onDebugChange() {
        if (getDebugMode() == 1) {
            setCustomNameVisible(true);
        } else {
            setCustomNameVisible(false);
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        super.handleEntityEvent(b);
        if (b == TRIGGER_SQUAWK) {
            doSquawk();
        } else if (b == TRIGGER_SCREECH) {
            doScreech();
        } else if (b == TRIGGER_DEATHSOUND) {
            doDeathSound();
        }
    }

    private void doSquawk() {
        if (!level().isClientSide) {
            this.worldObj.playSoundAtEntity(this, "invmod:v_squawk" + (random.nextInt(3) + 1), 1.9F, 1.0F + (this.random.nextFloat() * 0.2F - 0.1F));
            level().broadcastEntityEvent(this, TRIGGER_SQUAWK);
        } else {
            setBeakState(10);
        }
    }

    public String getNavString() {
        return getNavigatorNew().getStatus();
    }

    private void setAI() {
        this.goalSelector.addGoal(0, new EntityAISwoop(this));
        this.goalSelector.addGoal(4, new EntityAIFlyingStrike(this));
        this.goalSelector.addGoal(4, new EntityAIPickUpEntity(this, 0.0F, 0.2F, 0.0F, 1.5F, 1.5F, 20, 45.0F, 45.0F));
        this.goalSelector.addGoal(4, new EntityAIBirdFight(this, Zombie.class, 25, 0.4F));
        this.goalSelector.addGoal(4, new EntityAIWatchTarget(this));

    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public String toString() {
        return "IMVulture-T" + this.getTier();
    }
}
