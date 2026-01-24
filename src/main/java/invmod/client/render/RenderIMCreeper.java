package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderIMCreeper extends LegacyEntityRenderer<Entity> {
    public RenderIMCreeper(EntityRendererProvider.Context context) {
        super(context);
    }
}
