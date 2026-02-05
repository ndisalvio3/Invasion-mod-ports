package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import invmod.common.entity.EntityIMCreeper;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderIMCreeper extends MobRenderer<EntityIMCreeper, CreeperRenderState, CreeperModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png");

    public RenderIMCreeper(EntityRendererProvider.Context context) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
    }

    @Override
    protected void scale(CreeperRenderState state, PoseStack poseStack) {
        float swelling = state.swelling;
        float pulse = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Mth.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float xzScale = (1.0F + swelling * 0.4F) * pulse;
        float yScale = (1.0F + swelling * 0.1F) / pulse;
        poseStack.scale(xzScale, yScale, xzScale);
    }

    @Override
    protected float getWhiteOverlayProgress(CreeperRenderState state) {
        float swelling = state.swelling;
        return (int) (swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public void extractRenderState(EntityIMCreeper entity, CreeperRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.swelling = entity.setCreeperFlashTime(partialTick);
        state.isPowered = false;
    }
}
