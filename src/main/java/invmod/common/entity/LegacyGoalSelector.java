package invmod.common.entity;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;

public final class LegacyGoalSelector {
    private final GoalSelector selector;

    public LegacyGoalSelector(GoalSelector selector) {
        this.selector = selector;
    }

    public void addTask(int priority, Goal goal) {
        selector.addGoal(priority, goal);
    }
}
