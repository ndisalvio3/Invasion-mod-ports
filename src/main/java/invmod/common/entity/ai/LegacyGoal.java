package invmod.common.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Minimal adapter to keep legacy AI classes compiling while mapping to modern Goal APIs.
 */
public abstract class LegacyGoal extends Goal {
    public boolean shouldExecute() {
        return false;
    }

    public boolean continueExecuting() {
        return shouldExecute();
    }

    public void startExecuting() {
        // no-op
    }

    public void resetTask() {
        // no-op
    }

    public void updateTask() {
        // no-op
    }

    @Override
    public final boolean canUse() {
        return shouldExecute();
    }

    @Override
    public final boolean canContinueToUse() {
        return continueExecuting();
    }

    @Override
    public final void start() {
        startExecuting();
    }

    @Override
    public final void stop() {
        resetTask();
    }

    @Override
    public final void tick() {
        updateTask();
    }

    protected void setMutexBits(int bits) {
        EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);
        if ((bits & 0x1) != 0) {
            flags.add(Goal.Flag.MOVE);
        }
        if ((bits & 0x2) != 0) {
            flags.add(Goal.Flag.LOOK);
        }
        if ((bits & 0x4) != 0) {
            flags.add(Goal.Flag.JUMP);
        }
        if ((bits & 0x8) != 0) {
            flags.add(Goal.Flag.TARGET);
        }
        setFlags(flags);
    }
}
