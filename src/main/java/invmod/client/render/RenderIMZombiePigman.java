package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import invmod.common.entity.EntityIMZombiePigman;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderIMZombiePigman extends LegacyEntityRenderer<EntityIMZombiePigman> {
    private static final ResourceLocation T_T1 = ResourceLocation.parse("invmod:textures/pigzombie64x32.png");
    private static final ResourceLocation T_T3 = ResourceLocation.parse("invmod:textures/zombiePigmanT3.png");

    public RenderIMZombiePigman(EntityRendererProvider.Context context) {
        super(context, new ModelBigBiped(), 0.5F);
    }

    @Override
    protected void scale(LegacyRenderState state, PoseStack poseStack) {
        if (state.entity instanceof EntityIMZombiePigman pigman) {
            float s = pigman.scaleAmount();
            poseStack.scale(s, (2.0F + s) / 3.0F, s);
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMZombiePigman entity) {
        return entity.getTextureId() == 2 ? T_T3 : T_T1;
    }
}
