package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderIMLiving extends LegacyEntityRenderer<Entity> {
    public RenderIMLiving(EntityRendererProvider.Context context) {
        super(context);
    }
}
