package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.world.entity.LivingEntity;

public class EntityAILeaderTarget extends EntityAISimpleTarget {
    private final EntityIMLiving theEntity;

    public EntityAILeaderTarget(EntityIMLiving entity, Class<? extends LivingEntity> targetType, float distance) {
        this(entity, targetType, distance, true);
    }

    public EntityAILeaderTarget(EntityIMLiving entity, Class<? extends LivingEntity> targetType, float distance, boolean needsLos) {
        super(entity, targetType, distance, needsLos);
        this.theEntity = entity;
    }

    public boolean shouldExecute() {
        if (!this.theEntity.readyToRally()) {
            return false;
        }
        return super.shouldExecute();
    }
}