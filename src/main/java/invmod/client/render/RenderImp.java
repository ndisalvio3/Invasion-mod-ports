package invmod.client.render;

import invmod.common.entity.EntityIMImp;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderImp extends LegacyEntityRenderer<EntityIMImp> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invmod:textures/imp.png");

    public RenderImp(EntityRendererProvider.Context context) {
        super(context, new ModelImp(), 0.4F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMImp entity) {
        return TEXTURE;
    }
}
