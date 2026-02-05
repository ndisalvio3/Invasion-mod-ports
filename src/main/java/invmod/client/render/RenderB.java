package invmod.client.render;

import invmod.common.entity.EntityIMBird;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderB extends LegacyEntityRenderer<EntityIMBird> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invmod:textures/bird_tx1.png");

    public RenderB(EntityRendererProvider.Context context) {
        super(context, new ModelBird(), 0.4F);
    }

    @Override
    protected void prepareModel(EntityIMBird entity, LegacyModelBase model, float partialTick, LegacyRenderState state) {
        if (model instanceof ModelBird birdModel) {
            float flap = Mth.lerp(partialTick, entity.getOldFlap(), entity.getFlap());
            float legSweep = Mth.clamp(state.limbSwingAmount, 0.0F, 1.0F);
            birdModel.setFlyingAnimations(flap, legSweep, 0.0F);
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMBird entity) {
        return TEXTURE;
    }
}
