package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderIMSkeleton extends LegacyEntityRenderer<Entity> {
    public RenderIMSkeleton(EntityRendererProvider.Context context) {
        super(context);
    }
}
