package invmod.common.entity;

import invmod.common.IPathfindable;
import invmod.common.util.CoordsInt;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

public class PathCreator implements IPathSource {
    private int searchDepth;
    private int quickFailDepth;
    private int[] nanosUsed;
    private int index;

    public PathCreator() {
        this(200, 50);
    }

    public PathCreator(int searchDepth, int quickFailDepth) {
        this.searchDepth = searchDepth;
        this.quickFailDepth = quickFailDepth;
        this.nanosUsed = new int[6];
        this.index = 0;
    }

    public int getSearchDepth() {
        return this.searchDepth;
    }

    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }

    public int getQuickFailDepth() {
        return this.quickFailDepth;
    }

    public void setQuickFailDepth(int depth) {
        this.quickFailDepth = depth;
    }

    public Path createPath(IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float targetRadius, float maxSearchRange, BlockGetter terrainMap) {
        long time = System.nanoTime();
        Path path = PathfinderIM.createPath(entity, x, y, z, x2, y2, z2, targetRadius, maxSearchRange, terrainMap, this.searchDepth, this.quickFailDepth);
        int elapsed = (int) (System.nanoTime() - time);
        this.nanosUsed[this.index] = elapsed;
        if (++this.index >= this.nanosUsed.length) {
            this.index = 0;
        }
        return path;
    }

    public Path createPath(EntityIMLiving entity, Entity target, float targetRadius, float maxSearchRange, BlockGetter terrainMap) {
        return createPath(entity, Mth.floor(target.getX() + 0.5D - entity.getBbWidth() / 2.0F), Mth.floor(target.getY()), Mth.floor(target.getZ() + 0.5D - entity.getBbWidth() / 2.0F), targetRadius, maxSearchRange, terrainMap);
    }

    public Path createPath(EntityIMLiving entity, int x, int y, int z, float targetRadius, float maxSearchRange, BlockGetter terrainMap) {
        CoordsInt size = entity.getCollideSize();
        int startZ;
        int startX;
        int startY;
        if ((size.getXCoord() <= 1) && (size.getZCoord() <= 1)) {
            startX = entity.getXCoord();
            startY = Mth.floor(entity.getBoundingBox().minY);
            startZ = entity.getZCoord();
        } else {
            startX = Mth.floor(entity.getBoundingBox().minX);
            startY = Mth.floor(entity.getBoundingBox().minY);
            startZ = Mth.floor(entity.getBoundingBox().minZ);
        }
        return createPath(entity, startX, startY, startZ, Mth.floor(x + 0.5F - entity.getBbWidth() / 2.0F), y, Mth.floor(z + 0.5F - entity.getBbWidth() / 2.0F), targetRadius, maxSearchRange, terrainMap);
    }

    public void createPath(IPathResult observer, IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float maxSearchRange, BlockGetter terrainMap) {
    }

    public void createPath(IPathResult observer, EntityIMLiving entity, Entity target, float maxSearchRange, BlockGetter terrainMap) {
    }

    public void createPath(IPathResult observer, EntityIMLiving entity, int x, int y, int z, float maxSearchRange, BlockGetter terrainMap) {
    }

    public boolean canPathfindNice(IPathSource.PathPriority priority, float maxSearchRange, int searchDepth, int quickFailDepth) {
        return true;
    }
}
