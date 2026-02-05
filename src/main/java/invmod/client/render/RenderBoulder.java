package invmod.client.render;

import invmod.common.entity.EntityIMBoulder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;

public class RenderBoulder extends LegacyEntityRenderer<EntityIMBoulder> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invmod:textures/boulder.png");

    public RenderBoulder(EntityRendererProvider.Context context) {
        super(context, new ModelBoulder(), 0.3F);
    }

    @Override
    protected void scale(LegacyRenderState state, PoseStack poseStack) {
        poseStack.scale(2.2F, 2.2F, 2.2F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMBoulder entity) {
        return TEXTURE;
    }
}
