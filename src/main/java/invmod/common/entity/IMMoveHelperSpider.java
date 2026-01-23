package invmod.common.entity;

import net.minecraft.util.Mth;

public class IMMoveHelperSpider extends IMMoveHelper {
    public IMMoveHelperSpider(EntityIMLiving par1EntityLiving) {
        super(par1EntityLiving);
    }

    protected int getClimbFace(double x, double y, double z) {
        int mobX = Mth.floor(x - this.a.getBbWidth() / 2.0F);
        int mobY = Mth.floor(y);
        int mobZ = Mth.floor(z - this.a.getBbWidth() / 2.0F);

        int index = 0;
        Path path = this.a.getNavigatorNew().getPath();
        if ((path != null) && (!path.isFinished())) {
            PathNode currentPoint = path.getPathPointFromIndex(path.getCurrentPathIndex());
            int pathLength = path.getCurrentPathLength();
            for (int i = path.getCurrentPathIndex(); i < pathLength; i++) {
                PathNode point = path.getPathPointFromIndex(i);
                if (point.xCoord > currentPoint.xCoord) {
                    break;
                }
                if (point.xCoord < currentPoint.xCoord) {
                    index = 2;
                    break;
                }
                if (point.zCoord > currentPoint.zCoord) {
                    index = 4;
                    break;
                }
                if (point.zCoord < currentPoint.zCoord) {
                    index = 6;
                    break;
                }
            }

        }

        for (int count = 0; count < 8; count++) {
            //set bool to true, donno why
            net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(mobX + invmod.common.util.CoordsInt.offsetAdj2X[index], mobY, mobZ + invmod.common.util.CoordsInt.offsetAdj2Z[index]);
            if (this.a.level().getBlockState(pos).isCollisionShapeFullBlock(this.a.level(), pos)) {
                return index / 2;
            }
            index++;
            if (index > 7) {
                index = 0;
            }
        }
        return -1;
    }
}
