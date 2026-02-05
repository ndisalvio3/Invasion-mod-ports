package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import invmod.common.entity.EntityIMTrap;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderTrap extends LegacyEntityRenderer<EntityIMTrap> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invmod:textures/trap.png");

    public RenderTrap(EntityRendererProvider.Context context) {
        super(context, new ModelTrap(), 0.2F);
    }

    @Override
    protected void setupRotations(LegacyRenderState state, PoseStack poseStack) {
        super.setupRotations(state, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
    }

    @Override
    protected void scale(LegacyRenderState state, PoseStack poseStack) {
        poseStack.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMTrap entity) {
        return TEXTURE;
    }
}
