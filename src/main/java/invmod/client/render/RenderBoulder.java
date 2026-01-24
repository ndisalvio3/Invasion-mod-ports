package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderBoulder extends LegacyEntityRenderer<Entity> {
    public RenderBoulder(EntityRendererProvider.Context context) {
        super(context);
    }
}
