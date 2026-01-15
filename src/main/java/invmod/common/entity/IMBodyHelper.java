package invmod.common.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

public class IMBodyHelper extends BodyRotationControl {
    public IMBodyHelper(Mob mob) {
        super(mob);
    }

    @Override
    public void clientTick() {
        // Intentionally disabled: IM entities control body rotation elsewhere.
    }
}
