package invmod.common;

import net.minecraft.world.level.BlockGetter;

public interface IBlockAccessExtended extends BlockGetter {
    int getLayeredData(int paramInt1, int paramInt2, int paramInt3);

    void setData(int paramInt1, int paramInt2, int paramInt3, Integer paramInteger);
}
