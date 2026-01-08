package invmod.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityIMBolt extends Entity {
    private static final EntityDataAccessor<Integer> DATA_TICKS_TO_RENDER = SynchedEntityData.defineId(EntityIMBolt.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_VEC_X = SynchedEntityData.defineId(EntityIMBolt.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_VEC_Y = SynchedEntityData.defineId(EntityIMBolt.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_VEC_Z = SynchedEntityData.defineId(EntityIMBolt.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_SOUND_MADE = SynchedEntityData.defineId(EntityIMBolt.class, EntityDataSerializers.INT);

    private int age;
    private long timeCreated;
    private double[][] vertices;
    private long lastVertexUpdate;
    private float yaw;
    private float pitch;
    private double distance;
    private float widthVariance;

    public EntityIMBolt(EntityType<? extends EntityIMBolt> type, Level level) {
        super(type, level);
        this.age = 0;
        this.timeCreated = (this.lastVertexUpdate = System.currentTimeMillis());
        this.vertices = new double[3][0];
        this.widthVariance = 6.0F;
    }

    public void setupBolt(double x, double y, double z, double x2, double y2, double z2, int ticksToRender, int soundMade) {
        setPos(x, y, z);
        setVec((float) (x2 - x), (float) (y2 - y), (float) (z2 - z));
        setTicksToRender(ticksToRender);
        setSoundMade(soundMade);
        doVertexUpdate();
    }

    @Override
    public void tick() {
        super.tick();
        this.age += 1;
        if ((this.age == 1) && (getSoundMade() == 1)) {
            if (!level().isClientSide) {
                level().playSound(
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.HOSTILE,
                    0.8F,
                    1.0F + (random.nextFloat() - 0.5F) * 0.2F
                );
            }
        }
        if (this.age > getTicksToRender()) {
            discard();
        }
    }

    public double[][] getVertices() {
        long time = System.currentTimeMillis();
        if (time - this.timeCreated > getTicksToRender() * 50L) {
            return null;
        }
        if (time - this.lastVertexUpdate >= 75L) {
            doVertexUpdate();
            while (this.lastVertexUpdate + 50L <= time) {
                this.lastVertexUpdate += 50L;
            }
        }
        return this.vertices;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TICKS_TO_RENDER, 0);
        builder.define(DATA_VEC_X, 0.0F);
        builder.define(DATA_VEC_Y, 0.0F);
        builder.define(DATA_VEC_Z, 0.0F);
        builder.define(DATA_SOUND_MADE, 0);
    }

    private void setHeading(float x, float y, float z) {
        float xzSq = x * x + z * z;
        this.yaw = ((float) (Math.atan2(x, z) * 180.0D / Math.PI) + 90.0F);
        this.pitch = ((float) (Math.atan2(Mth.sqrt(xzSq), y) * 180.0D / Math.PI));
        this.distance = Math.sqrt(xzSq + y * y);
    }

    private void doVertexUpdate() {
        this.widthVariance = (10.0F / (float) Math.log10(this.distance + 1.0D));
        int numberOfVertexes = 60;
        if (numberOfVertexes != this.vertices[0].length) {
            this.vertices[0] = new double[numberOfVertexes];
            this.vertices[1] = new double[numberOfVertexes];
            this.vertices[2] = new double[numberOfVertexes];
        }

        for (int vertex = 0; vertex < numberOfVertexes; vertex++) {
            this.vertices[1][vertex] = (vertex * this.distance / (numberOfVertexes - 1));
        }

        createSegment(0, numberOfVertexes - 1);
    }

    private void createSegment(int begin, int end) {
        int points = end + 1 - begin;
        if (points <= 4) {
            if (points == 3) {
                createVertex(begin, begin + 1, end);
            } else {
                createVertex(begin, begin + 1, end);
                createVertex(begin, begin + 2, end);
            }
            return;
        }
        int midPoint = begin + points / 2;
        createVertex(begin, midPoint, end);
        createSegment(begin, midPoint);
        createSegment(midPoint, end);
    }

    private void createVertex(int begin, int mid, int end) {
        double difference = this.vertices[0][end] - this.vertices[0][begin];
        double yDiffToMid = this.vertices[1][mid] - this.vertices[1][begin];
        double yRatio = yDiffToMid / (this.vertices[1][end] - this.vertices[1][begin]);
        this.vertices[0][mid] = (this.vertices[0][begin] + difference * yRatio + (random.nextFloat() - 0.5D) * yDiffToMid * this.widthVariance);
        difference = this.vertices[2][end] - this.vertices[2][begin];
        this.vertices[2][mid] = (this.vertices[2][begin] + difference * yRatio + (random.nextFloat() - 0.5D) * yDiffToMid * this.widthVariance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setTicksToRender(tag.getIntOr("TicksToRender", 0));
        setVec(
            tag.getFloatOr("VecX", 0.0F),
            tag.getFloatOr("VecY", 0.0F),
            tag.getFloatOr("VecZ", 0.0F)
        );
        setSoundMade(tag.getIntOr("SoundMade", 0));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksToRender", getTicksToRender());
        tag.putFloat("VecX", getVecX());
        tag.putFloat("VecY", getVecY());
        tag.putFloat("VecZ", getVecZ());
        tag.putInt("SoundMade", getSoundMade());
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_VEC_X.equals(accessor) || DATA_VEC_Y.equals(accessor) || DATA_VEC_Z.equals(accessor)) {
            refreshHeading();
            doVertexUpdate();
        }
    }

    public int getTicksToRender() {
        return entityData.get(DATA_TICKS_TO_RENDER);
    }

    public void setTicksToRender(int ticksToRender) {
        entityData.set(DATA_TICKS_TO_RENDER, ticksToRender);
    }

    public int getSoundMade() {
        return entityData.get(DATA_SOUND_MADE);
    }

    public void setSoundMade(int soundMade) {
        entityData.set(DATA_SOUND_MADE, soundMade);
    }

    public float getVecX() {
        return entityData.get(DATA_VEC_X);
    }

    public float getVecY() {
        return entityData.get(DATA_VEC_Y);
    }

    public float getVecZ() {
        return entityData.get(DATA_VEC_Z);
    }

    public void setVec(float x, float y, float z) {
        entityData.set(DATA_VEC_X, x);
        entityData.set(DATA_VEC_Y, y);
        entityData.set(DATA_VEC_Z, z);
        refreshHeading();
    }

    private void refreshHeading() {
        setHeading(getVecX(), getVecY(), getVecZ());
    }
}
