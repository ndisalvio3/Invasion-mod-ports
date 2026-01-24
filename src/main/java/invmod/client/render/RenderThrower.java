package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderThrower extends LegacyEntityRenderer<Entity> {
    public RenderThrower(EntityRendererProvider.Context context) {
        super(context);
    }
}
