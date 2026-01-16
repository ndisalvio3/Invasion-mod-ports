package invmod.common.util;

import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;

public class ExplosionUtil {

    public static void doExplosionB(Level level, Explosion explosion, boolean spawnParticles) {
        if (level == null || explosion == null) {
            return;
        }

        Vec3 center = explosion.center();
        ExplosionInteraction interaction = toInteraction(explosion.getBlockInteraction());
        level.explode(explosion.getDirectSourceEntity(), center.x, center.y, center.z, explosion.radius(), spawnParticles, interaction);
    }

    private static ExplosionInteraction toInteraction(Explosion.BlockInteraction interaction) {
        return switch (interaction) {
            case KEEP -> ExplosionInteraction.NONE;
            case TRIGGER_BLOCK -> ExplosionInteraction.TRIGGER;
            case DESTROY_WITH_DECAY -> ExplosionInteraction.TNT;
            case DESTROY -> ExplosionInteraction.BLOCK;
        };
    }
}
