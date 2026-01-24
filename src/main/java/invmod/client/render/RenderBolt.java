package invmod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class RenderBolt extends LegacyEntityRenderer<Entity> {
    public RenderBolt(EntityRendererProvider.Context context) {
        super(context);
    }
}
