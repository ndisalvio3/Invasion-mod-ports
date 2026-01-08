package invmod.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class EntityIMMob extends EntityIMLiving {
    protected EntityIMMob(EntityType<? extends EntityIMLiving> type, Level level) {
        super(type, level);
    }
}
