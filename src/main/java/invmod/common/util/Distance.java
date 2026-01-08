package invmod.common.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Distance {
    public static double distanceBetween(IPosition pos1, IPosition pos2) {
        double dX = pos2.getXCoord() - pos1.getXCoord();
        double dY = pos2.getYCoord() - pos1.getYCoord();
        double dZ = pos2.getZCoord() - pos1.getZCoord();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distanceBetween(IPosition pos1, Vec3 pos2) {
        double dX = pos2.x - pos1.getXCoord();
        double dY = pos2.y - pos1.getYCoord();
        double dZ = pos2.z - pos1.getZCoord();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distanceBetween(IPosition pos1, double x2, double y2, double z2) {
        double dX = x2 - pos1.getXCoord();
        double dY = y2 - pos1.getYCoord();
        double dZ = z2 - pos1.getZCoord();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distanceBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distanceBetween(Entity entity, Vec3 pos2) {
        double dX = pos2.x - entity.getX();
        double dY = pos2.y - entity.getY();
        double dZ = pos2.z - entity.getZ();
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }
}
