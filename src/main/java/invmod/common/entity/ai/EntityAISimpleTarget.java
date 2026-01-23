package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import invmod.common.util.ComparatorEntityDistanceFrom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntityAISimpleTarget extends LegacyGoal {
    private final EntityIMLiving theEntity;
    private LivingEntity targetEntity;
    private Class<? extends LivingEntity> targetClass;
    private int outOfLosTimer;
    private float distance;
    private boolean needsLos;

    public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends LivingEntity> targetType, float distance) {
        this(entity, targetType, distance, true);
    }

    public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends LivingEntity> targetType, float distance, boolean needsLoS) {
        this.theEntity = entity;
        this.targetClass = targetType;
        this.outOfLosTimer = 0;
        this.distance = distance;
        this.needsLos = needsLoS;
        setMutexBits(1);
    }

    public EntityIMLiving getEntity() {
        return this.theEntity;
    }

    public boolean shouldExecute() {
        if (this.targetClass == Player.class) {
            Player player = this.theEntity.worldObj.getClosestVulnerablePlayerToEntity(this.theEntity, this.distance);
            if (isValidTarget(player)) {
                this.targetEntity = player;
                return true;
            }
        }

        List list = this.theEntity.worldObj.getEntitiesWithinAABB(this.targetClass, this.theEntity.getBoundingBox().inflate(this.distance, this.distance / 2.0F, this.distance));
        Comparator comp = new ComparatorEntityDistanceFrom(this.theEntity.getX(), this.theEntity.getY(), this.theEntity.getZ());
        Collections.sort(list, comp);

        boolean foundEntity = false;
        while (list.size() > 0) {
            LivingEntity entity = (LivingEntity) list.remove(list.size() - 1);
            if (isValidTarget(entity)) {
                this.targetEntity = entity;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean continueExecuting() {
        LivingEntity entityliving = this.theEntity.getAttackTarget();
        if (entityliving == null) {
            return false;
        }
        if (!entityliving.isAlive()) {
            return false;
        }
        if (this.theEntity.getDistanceSqToEntity(entityliving) > this.distance * this.distance) {
            return false;
        }
        if (this.needsLos) {
            if (!this.theEntity.getEntitySenses().hasLineOfSight(entityliving)) {
                if (++this.outOfLosTimer > 60) {
                    return false;
                }
            } else {
                this.outOfLosTimer = 0;
            }
        }

        return true;
    }

    @Override
    public void startExecuting() {
        this.theEntity.setAttackTarget(this.targetEntity);
        this.outOfLosTimer = 0;
    }

    @Override
    public void resetTask() {
        this.theEntity.setAttackTarget(null);
    }

    public Class<? extends LivingEntity> getTargetType() {
        return this.targetClass;
    }

    public float getAggroRange() {
        return this.distance;
    }

    protected void setTarget(LivingEntity entity) {
        this.targetEntity = entity;
    }

    protected boolean isValidTarget(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (entity == this.theEntity) {
            return false;
        }
        if (!entity.isAlive()) {
            return false;
        }

        //players in creative mode won't be targeted 
        if (this.targetClass == Player.class && entity instanceof Player player) {
            if (player.getAbilities().invulnerable) {
                return false;
            }
        }

        if ((this.needsLos) && (!this.theEntity.getEntitySenses().hasLineOfSight(entity))) {
            return false;
        }
        return true;
    }
}
