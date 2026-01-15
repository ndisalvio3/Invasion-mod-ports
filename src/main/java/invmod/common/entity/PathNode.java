package invmod.common.entity;

import invmod.common.util.IPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class PathNode
        implements IPosition {
    public final int xCoord;
    public final int yCoord;
    public final int zCoord;
    public final PathAction action;
    private final long posHash;
    public boolean isFirst;
    int index;
    float totalPathDistance;
    float distanceToNext;
    float distanceToTarget;
    private PathNode previous;

    public PathNode(int i, int j, int k) {
        this(i, j, k, PathAction.NONE);
    }

    public PathNode(int i, int j, int k, PathAction pathAction) {
        this.index = -1;
        this.isFirst = false;
        this.xCoord = i;
        this.yCoord = j;
        this.zCoord = k;
        this.action = pathAction;
        this.posHash = makeHash(i, j, k);
    }

    public static long makeHash(int x, int y, int z) {
        return BlockPos.asLong(x, y, z);
    }

    public float distanceTo(PathNode pathpoint) {
        float f = pathpoint.xCoord - this.xCoord;
        float f1 = pathpoint.yCoord - this.yCoord;
        float f2 = pathpoint.zCoord - this.zCoord;
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public float distanceTo(float x, float y, float z) {
        float f = x - this.xCoord;
        float f1 = y - this.yCoord;
        float f2 = z - this.zCoord;
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public boolean equals(Object obj) {
        if ((obj instanceof PathNode)) {
            PathNode node = (PathNode) obj;
            return (this.xCoord == node.xCoord) && (this.yCoord == node.yCoord) && (this.zCoord == node.zCoord) && (node.action == this.action);
        }

        return false;
    }

    public boolean equals(int x, int y, int z) {
        return (this.xCoord == x) && (this.yCoord == y) && (this.zCoord == z);
    }

    public int hashCode() {
        int result = Long.hashCode(this.posHash);
        result = 31 * result + this.action.ordinal();
        return result;
    }

    public boolean isAssigned() {
        return this.index >= 0;
    }

    public int getXCoord() {
        return this.xCoord;
    }

    public int getYCoord() {
        return this.yCoord;
    }

    public int getZCoord() {
        return this.zCoord;
    }

    public String toString() {
        return this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ", " + this.action;
    }

    public PathNode getPrevious() {
        return previous;
    }

    public void setPrevious(PathNode previous) {
        this.previous = previous;
    }
}
