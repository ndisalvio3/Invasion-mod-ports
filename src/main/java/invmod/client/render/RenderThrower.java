package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import invmod.common.entity.EntityIMThrower;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderThrower extends LegacyEntityRenderer<EntityIMThrower> {
    private static final ResourceLocation TEXTURE_T1 = ResourceLocation.parse("invmod:textures/throwerT1.png");
    private static final ResourceLocation TEXTURE_T2 = ResourceLocation.parse("invmod:textures/throwerT2.png");

    public RenderThrower(EntityRendererProvider.Context context) {
        super(context, new ModelThrower(), 0.8F);
    }

    @Override
    protected void scale(LegacyRenderState state, PoseStack poseStack) {
        poseStack.scale(2.4F, 2.8F, 2.4F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMThrower entity) {
        return entity.getTextureId() == 2 ? TEXTURE_T2 : TEXTURE_T1;
    }
}
