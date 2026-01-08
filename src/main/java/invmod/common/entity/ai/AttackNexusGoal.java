package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMZombie;
import invmod.common.nexus.BlockNexus;
import invmod.common.nexus.INexusAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;

public class AttackNexusGoal extends Goal {
    private final EntityIMLiving attacker;
    private final int damage;
    private final double reach;
    private int attackCooldown;
    private BlockPos targetPos;
    private INexusAccess targetNexus;

    public AttackNexusGoal(EntityIMLiving attacker, int damage, double reach) {
        this.attacker = attacker;
        this.damage = damage;
        this.reach = reach;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return locateNexusTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null && attacker.distanceToSqr(targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D) <= reach * reach;
    }

    @Override
    public void start() {
        attackCooldown = 10;
    }

    @Override
    public void tick() {
        if (!locateNexusTarget()) {
            return;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
            return;
        }
        doAttack();
        attackCooldown = 20;
    }

    private boolean locateNexusTarget() {
        if (attacker.getNexus() != null) {
            targetNexus = attacker.getNexus();
            targetPos = new BlockPos(targetNexus.getXCoord(), targetNexus.getYCoord(), targetNexus.getZCoord());
            return isInRange(targetPos);
        }

        Level level = attacker.level();
        BlockPos origin = attacker.blockPosition();
        int radius = (int) Math.ceil(reach);
        BlockPos min = origin.offset(-radius, -1, -radius);
        BlockPos max = origin.offset(radius, 1, radius);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(pos).getBlock() instanceof BlockNexus) {
                targetPos = pos.immutable();
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof INexusAccess nexus) {
                    targetNexus = nexus;
                } else {
                    targetNexus = null;
                }
                return true;
            }
        }
        targetPos = null;
        targetNexus = null;
        return false;
    }

    private boolean isInRange(BlockPos pos) {
        return attacker.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= reach * reach;
    }

    private void doAttack() {
        if (targetNexus != null) {
            targetNexus.attackNexus(damage);
        }
        if (attacker instanceof EntityIMZombie zombie) {
            zombie.updateAnimation(true);
        }
    }
}
