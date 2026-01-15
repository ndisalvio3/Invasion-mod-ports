package invmod.common.entity.ai;

import invmod.common.entity.EntityIMSpider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class EntityAIPounce extends Goal {
    private final EntityIMSpider spider;
    private boolean isPouncing;
    private int pounceTimer;
    private final int cooldown;
    private final float minPower;
    private final float maxPower;
    private static final double GRAVITY = 0.08D; // Standard entity gravity

    public EntityAIPounce(EntityIMSpider entity, float minPower, float maxPower, int cooldown) {
        this.spider = entity;
        this.isPouncing = false;
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.cooldown = cooldown;
        this.pounceTimer = 0;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = spider.getTarget();
        if (--pounceTimer <= 0 && target != null && spider.hasLineOfSight(target) && spider.onGround()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return isPouncing;
    }

    @Override
    public void start() {
        LivingEntity target = spider.getTarget();
        if (target != null && pounce(target.getX(), target.getY(), target.getZ())) {
            spider.setAirborneTime(0);
            isPouncing = true;
            spider.getNavigation().stop();
        } else {
            isPouncing = false;
        }
    }

    @Override
    public void tick() {
        spider.getNavigation().stop();
        int airborneTime = spider.getAirborneTime();
        if (airborneTime > 20 && spider.onGround()) {
            isPouncing = false;
            pounceTimer = cooldown;
            spider.setAirborneTime(0);
            spider.getNavigation().stop();
        } else {
            spider.setAirborneTime(airborneTime + 1);
        }
    }

    @Override
    public void stop() {
        isPouncing = false;
        spider.setAirborneTime(0);
        spider.getNavigation().stop();
    }

    protected boolean pounce(double x, double y, double z) {
        double dX = x - spider.getX();
        double dY = y - spider.getY();
        double dZ = z - spider.getZ();
        double dXZ = Math.sqrt(dX * dX + dZ * dZ);
        double a = Math.atan(dY / dXZ);
        if (a > -0.7853981633974483D && a < 0.7853981633974483D) {
            double rratio = (1.0D - Math.tan(a)) * (1.0D / Math.cos(a));
            double r = dXZ / rratio;
            double v = 1.0D / Math.sqrt(1.0F / GRAVITY / r);
            if (v > minPower && v < maxPower) {
                double distance = Math.sqrt(2.0D * (dXZ * dXZ));
                Vec3 motion = new Vec3(v * dX / distance, v * dXZ / distance, v * dZ / distance);
                spider.setDeltaMovement(motion);
                return true;
            }
        }
        return false;
    }
}
