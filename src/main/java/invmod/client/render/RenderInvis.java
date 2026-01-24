package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderInvis extends LegacyEntityRenderer<Entity> {
    public RenderInvis(EntityRendererProvider.Context context) {
        super(context);
    }
}
