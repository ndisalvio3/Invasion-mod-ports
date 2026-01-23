package invmod.common.entity.ai;

import invmod.common.entity.EntityIMLiving;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityAIKillEntity<T extends LivingEntity> extends EntityAIMoveToEntity<T> {
    private static final float ATTACK_RANGE = 1.0F;
    private int attackDelay;
    private int nextAttack;

    public EntityAIKillEntity(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay) {
        super(entity, targetClass);
        this.attackDelay = attackDelay;
        this.nextAttack = 0;
    }

    @Override
    public void updateTask() {
        super.updateTask();
        setAttackTime(getAttackTime() - 1);
        Entity target = getTarget();
        if (canAttackEntity(target)) {
            attackEntity(target);
        }
    }

    protected void attackEntity(Entity target) {
        getEntity().attackEntityAsMob(getTarget());
        setAttackTime(getAttackDelay());
    }

    protected boolean canAttackEntity(Entity target) {
        if (getAttackTime() <= 0) {
            Entity entity = getEntity();
            double d = (entity.getBbWidth() + 1.0F) * (entity.getBbWidth() + 1.0F);

            return entity.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) < d;
        }
        return false;
    }

    protected int getAttackTime() {
        return this.nextAttack;
    }

    protected void setAttackTime(int time) {
        this.nextAttack = time;
    }

    protected int getAttackDelay() {
        return this.attackDelay;
    }

    protected void setAttackDelay(int time) {
        this.attackDelay = time;
    }
}
