package invmod.common.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class IMNearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
    private final Class<T> targetClass;
    private final double range;
    private final boolean requiresLineOfSight;
    private final TargetingConditions targetConditions;
    private T target;

    public IMNearestAttackableTargetGoal(Mob mob, Class<T> targetClass, double range, boolean requiresLineOfSight) {
        super(mob, false, false);
        this.targetClass = targetClass;
        this.range = range;
        this.requiresLineOfSight = requiresLineOfSight;
        TargetingConditions conditions = TargetingConditions.forCombat().range(range);
        if (!requiresLineOfSight) {
            conditions = conditions.ignoreLineOfSight();
        }
        this.targetConditions = conditions;
    }

    @Override
    public boolean canUse() {
        AABB searchArea = mob.getBoundingBox().inflate(range, range / 2.0D, range);
        List<T> candidates = mob.level().getEntitiesOfClass(targetClass, searchArea);
        target = mob.level().getNearestEntity(candidates, targetConditions, mob, mob.getX(), mob.getEyeY(), mob.getZ());
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (mob.distanceToSqr(target) > range * range) {
            return false;
        }
        if (requiresLineOfSight && !mob.getSensing().hasLineOfSight(target)) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        mob.setTarget(target);
        super.start();
    }
}
