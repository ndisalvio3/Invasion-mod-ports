package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderImp extends LegacyEntityRenderer<Entity> {
    public RenderImp(EntityRendererProvider.Context context) {
        super(context);
    }
}
