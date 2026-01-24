package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderEgg extends LegacyEntityRenderer<Entity> {
    public RenderEgg(EntityRendererProvider.Context context) {
        super(context);
    }
}
