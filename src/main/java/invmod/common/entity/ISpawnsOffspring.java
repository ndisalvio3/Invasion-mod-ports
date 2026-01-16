package invmod.common.entity;

import invmod.common.nexus.InvMobConstruct;
import net.minecraft.world.entity.Entity;

public abstract interface ISpawnsOffspring {
    public abstract InvMobConstruct[] getOffspring(Entity paramEntity);
}
