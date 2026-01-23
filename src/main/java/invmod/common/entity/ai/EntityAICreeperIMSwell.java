package invmod.common.entity.ai;

//NOOB HAUS: Done 

import invmod.common.entity.EntityIMCreeper;
import invmod.common.entity.EntityIMWolf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EntityAICreeperIMSwell extends LegacyGoal {
    EntityIMCreeper theEntity;
    LivingEntity targetEntity;

    public EntityAICreeperIMSwell(EntityIMCreeper par1EntityCreeper) {
        this.theEntity = par1EntityCreeper;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        LivingEntity entityliving = this.theEntity.getAttackTarget();

        return (this.theEntity.getCreeperState() > 0) || ((entityliving != null) && (this.theEntity.getDistanceSqToEntity(entityliving) < 9.0D) && ((entityliving.getClass() == Player.class) || (entityliving.getClass() == EntityIMWolf.class) || (entityliving.getClass() == ServerPlayer.class)));
    }

    @Override
    public void startExecuting() {
        this.theEntity.getNavigatorNew().clearPath();
        this.targetEntity = this.theEntity.getAttackTarget();
    }

    @Override
    public void resetTask() {
        this.targetEntity = null;
    }

    @Override
    public void updateTask() {
        if (this.targetEntity == null) {
            this.theEntity.setCreeperState(-1);
            return;
        }

        if (this.theEntity.getDistanceSqToEntity(this.targetEntity) > 49.0D) {
            this.theEntity.setCreeperState(-1);
            return;
        }

        if (!this.theEntity.getEntitySenses().hasLineOfSight(this.targetEntity)) {
            this.theEntity.setCreeperState(-1);
            return;
        }
        this.theEntity.setCreeperState(1);
    }
}
