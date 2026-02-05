package invmod.client.render;

import invmod.common.entity.EntityIMPigEngy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderPigEngy extends LegacyEntityRenderer<EntityIMPigEngy> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invmod:textures/pigengT1.png");

    public RenderPigEngy(EntityRendererProvider.Context context) {
        super(context, new ModelBigBiped(), 0.5F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMPigEngy entity) {
        return TEXTURE;
    }
}
