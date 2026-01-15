package invmod.common.entity.ai;

import com.whammich.invasion.registry.ModEntities;
import invmod.common.entity.EntityIMEgg;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.ISpawnsOffspring;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class EntityAILayEgg extends Goal {
    private static final int EGG_LAY_TIME = 45;
    private static final int INITIAL_EGG_DELAY = 25;
    private static final int NEXT_EGG_DELAY = 230;
    private static final int EGG_HATCH_TIME = 125;

    private final EntityIMSpider spider;
    private int time;
    private boolean isLaying;
    private int eggCount;

    public EntityAILayEgg(EntityIMSpider entity, int eggs) {
        this.spider = entity;
        this.eggCount = eggs;
        this.isLaying = false;
        this.time = 0;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    public void addEggs(int eggs) {
        this.eggCount += eggs;
    }

    @Override
    public boolean canUse() {
        LivingEntity target = spider.getTarget();
        if (eggCount > 0 && target != null && spider.hasLineOfSight(target)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return eggCount > 0 && spider.getTarget() != null;
    }

    @Override
    public void start() {
        this.time = INITIAL_EGG_DELAY;
        this.isLaying = false;
    }

    @Override
    public void tick() {
        time--;
        if (time <= 0) {
            if (!isLaying) {
                isLaying = true;
                time = EGG_LAY_TIME;
            } else {
                isLaying = false;
                eggCount--;
                time = NEXT_EGG_DELAY;
                layEgg();
            }
        }
    }

    private void layEgg() {
        if (!(spider.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity[] contents = null;
        if (spider instanceof ISpawnsOffspring spawner) {
            contents = spawner.getOffspring(null);
        }

        EntityIMEgg egg = ModEntities.IM_EGG.get().create(serverLevel, EntitySpawnReason.EVENT);
        if (egg != null) {
            egg.setPos(spider.getX(), spider.getY(), spider.getZ());
            egg.setupEgg(contents, EGG_HATCH_TIME);
            serverLevel.addFreshEntity(egg);
        }
    }
}
