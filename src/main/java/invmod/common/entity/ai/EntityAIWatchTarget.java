package invmod.common.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;

public class EntityAIWatchTarget extends LegacyGoal {
    private Mob theEntity;

    public EntityAIWatchTarget(Mob entity) {
        this.theEntity = entity;
    }

    public boolean shouldExecute() {
        return this.theEntity.getTarget() != null;
    }

    public void updateTask() {
        LivingEntity target = this.theEntity.getTarget();
        if (target != null) {
            this.theEntity.getLookControl().setLookAt(target, 2.0F, 2.0F);
        }
    }
}
