package invmod.common.entity.ai;

//NOOB HAUS: Done

import invmod.common.entity.EntityIMFlying;
import invmod.common.entity.Goal;
import invmod.common.entity.INavigationFlying;
import invmod.common.entity.MoveState;
import net.minecraft.world.entity.LivingEntity;

public class EntityAIFlyingTackle extends LegacyGoal {
    private EntityIMFlying theEntity;
    private int time;

    public EntityAIFlyingTackle(EntityIMFlying entity) {
        this.theEntity = entity;
        this.time = 0;
    }

    public boolean shouldExecute() {
        return this.theEntity.getAIGoal() == Goal.TACKLE_TARGET;
    }

    public boolean continueExecuting() {
        LivingEntity target = this.theEntity.getAttackTarget();
        if ((target == null) || (!target.isAlive())) {
            this.theEntity.transitionAIGoal(Goal.NONE);
            return false;
        }

        if (this.theEntity.getAIGoal() != Goal.TACKLE_TARGET) {
            return false;
        }
        return true;
    }

    public void startExecuting() {
        this.time = 0;
        LivingEntity target = this.theEntity.getAttackTarget();
        if (target != null) {
            this.theEntity.getNavigatorNew().setMovementType(INavigationFlying.MoveType.PREFER_WALKING);
        }
    }

    public void updateTask() {
        if (this.theEntity.getMoveState() != MoveState.FLYING) {
            this.theEntity.transitionAIGoal(Goal.MELEE_TARGET);
        }
    }
}
