package invmod.common.entity;

import invmod.common.util.IPosition;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

public abstract interface ICanDig {
    public abstract IPosition[] getBlockRemovalOrder(int paramInt1, int paramInt2, int paramInt3);

    public abstract float getBlockRemovalCost(int paramInt1, int paramInt2, int paramInt3);

    public abstract boolean canClearBlock(int paramInt1, int paramInt2, int paramInt3);

    public abstract void onBlockRemoved(int paramInt1, int paramInt2, int paramInt3, Block block);

    public abstract BlockGetter getTerrain();
}
