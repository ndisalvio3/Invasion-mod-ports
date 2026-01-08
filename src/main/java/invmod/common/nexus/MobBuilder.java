package invmod.common.nexus;

import com.whammich.invasion.registry.ModEntities;
import invmod.Invasion;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;

public class MobBuilder {
    public Entity createMobFromConstruct(Object construct, Level level, INexusAccess nexus) {
        if (!(construct instanceof EntityConstruct mobConstruct) || level == null) {
            return null;
        }

        Entity entity = switch (mobConstruct.getMobType()) {
            case ZOMBIE -> ModEntities.IM_ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
            case ZOMBIEPIGMAN, PIG_ZOMBIE -> ModEntities.IM_ZOMBIE_PIGMAN.get().create(level, EntitySpawnReason.EVENT);
            case SPIDER -> ModEntities.IM_SPIDER.get().create(level, EntitySpawnReason.EVENT);
            case SKELETON -> ModEntities.IM_SKELETON.get().create(level, EntitySpawnReason.EVENT);
            case THROWER -> ModEntities.IM_THROWER.get().create(level, EntitySpawnReason.EVENT);
            case IMP -> ModEntities.IM_IMP.get().create(level, EntitySpawnReason.EVENT);
            case BURROWER -> ModEntities.IM_BURROWER.get().create(level, EntitySpawnReason.EVENT);
            case PIG_ENGINEER, CREEPER -> null;
        };

        if (entity == null) {
            Invasion.log("Unsupported mob type: " + mobConstruct.getMobType());
            return null;
        }

        applyConstruct(entity, mobConstruct, nexus);
        return entity;
    }

    private void applyConstruct(Entity entity, EntityConstruct construct, INexusAccess nexus) {
        if (entity instanceof EntityIMZombie zombie) {
            zombie.setTier(construct.getTier());
            zombie.setFlavour(construct.getFlavour());
            zombie.setTextureId(construct.getTexture());
        } else if (entity instanceof EntityIMSpider spider) {
            spider.setTier(construct.getTier());
            spider.setFlavour(construct.getFlavour());
            spider.setTextureId(construct.getTexture());
        } else if (entity instanceof EntityIMSkeleton skeleton) {
            skeleton.setTier(construct.getTier());
            skeleton.setFlavour(construct.getFlavour());
            skeleton.setTextureId(construct.getTexture());
        } else if (entity instanceof EntityIMZombiePigman pigman) {
            pigman.setTier(construct.getTier());
            pigman.setFlavour(construct.getFlavour());
            pigman.setTextureId(construct.getTexture());
        } else if (entity instanceof EntityIMThrower thrower) {
            thrower.setTier(construct.getTier());
            thrower.setFlavour(construct.getFlavour());
            thrower.setTextureId(construct.getTexture());
        } else if (entity instanceof EntityIMImp imp) {
            imp.setTier(construct.getTier());
            imp.setFlavour(construct.getFlavour());
            imp.setTextureId(construct.getTexture());
        } else if (entity instanceof EntityIMBurrower burrower) {
            burrower.setTier(construct.getTier());
            burrower.setFlavour(construct.getFlavour());
            burrower.setTextureId(construct.getTexture());
        }

        if (entity instanceof EntityIMLiving living) {
            living.acquiredByNexus(nexus);
            nexus.registerMob(living);
        }
    }
}
