package invmod.common;

import invmod.common.entity.PathNode;
import invmod.common.entity.PathfinderIM;
import net.minecraft.world.level.BlockGetter;

public abstract interface IPathfindable {
    public abstract float getBlockPathCost(PathNode paramPathNode1, PathNode paramPathNode2, BlockGetter paramIBlockAccess);

    public abstract void getPathOptionsFromNode(BlockGetter paramIBlockAccess, PathNode paramPathNode, PathfinderIM paramPathfinderIM);
}
