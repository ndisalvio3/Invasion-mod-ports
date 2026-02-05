package invmod.client.render;

import invmod.common.entity.EntityIMEgg;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderEgg extends LegacyEntityRenderer<EntityIMEgg> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invmod:textures/spideregg.png");

    public RenderEgg(EntityRendererProvider.Context context) {
        super(context, new ModelEgg(), 0.2F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMEgg entity) {
        return TEXTURE;
    }
}
