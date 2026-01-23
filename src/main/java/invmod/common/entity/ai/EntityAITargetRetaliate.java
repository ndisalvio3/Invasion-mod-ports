package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.world.entity.LivingEntity;

public class EntityAITargetRetaliate extends EntityAISimpleTarget {
    public EntityAITargetRetaliate(EntityIMLiving entity, Class<? extends LivingEntity> targetType, float distance) {
        super(entity, targetType, distance);
    }

    @Override
    public boolean shouldExecute() {
        LivingEntity attacker = getEntity().getAITarget();
        if (attacker != null) {
            if ((getEntity().getDistanceToEntity(attacker) <= getAggroRange()) && (getTargetType().isAssignableFrom(attacker.getClass()))) {
                setTarget(attacker);
                return true;
            }
        }
        return false;
    }
}