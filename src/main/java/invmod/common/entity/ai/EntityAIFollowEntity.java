package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityAIFollowEntity<T extends LivingEntity> extends EntityAIMoveToEntity<T> {
    private float followDistanceSq;

    public EntityAIFollowEntity(EntityIMLiving entity, float followDistance) {
        this(entity, (Class<? extends T>) LivingEntity.class, followDistance);
    }

    public EntityAIFollowEntity(EntityIMLiving entity, Class<? extends T> target, float followDistance) {
        super(entity, target);
        this.followDistanceSq = (followDistance * followDistance);
    }

    public void startExecuting() {
        getEntity().onFollowingEntity(getTarget());
        super.startExecuting();
    }

    public void resetTask() {
        getEntity().onFollowingEntity(null);
        super.resetTask();
    }

    public void updateTask() {
        super.updateTask();
        Entity entity = getTarget();
        if (getEntity().getDistanceSq(entity.getX(), entity.getBoundingBox().minY, entity.getZ()) < this.followDistanceSq)
            getEntity().getNavigatorNew().haltForTick();
    }
}