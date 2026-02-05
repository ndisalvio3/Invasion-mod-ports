package invmod.client.render;

import invmod.common.entity.EntityIMSkeleton;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderIMSkeleton extends LegacyEntityRenderer<EntityIMSkeleton> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public RenderIMSkeleton(EntityRendererProvider.Context context) {
        super(context, new ModelIMSkeleton(), 0.5F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMSkeleton entity) {
        return TEXTURE;
    }
}
