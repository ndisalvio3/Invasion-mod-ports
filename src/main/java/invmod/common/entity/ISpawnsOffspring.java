package invmod.common.entity;

import net.minecraft.world.entity.Entity;

public abstract interface ISpawnsOffspring {
    public abstract Entity[] getOffspring(Entity paramEntity);
}
