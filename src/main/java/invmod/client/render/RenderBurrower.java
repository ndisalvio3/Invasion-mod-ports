package invmod.client.render;

import invmod.common.entity.EntityIMBurrower;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;

public class RenderBurrower extends LegacyEntityRenderer<EntityIMBurrower> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invmod:textures/burrower.png");

    public RenderBurrower(EntityRendererProvider.Context context) {
        super(context, new ModelBurrower2(16), 0.5F);
    }

    @Override
    protected void scale(LegacyRenderState state, PoseStack poseStack) {
        poseStack.scale(2.2F, 2.2F, 2.2F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMBurrower entity) {
        return TEXTURE;
    }
}
