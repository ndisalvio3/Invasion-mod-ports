package invmod.client.render;

import invmod.common.entity.EntityIMSpider;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class RenderSpiderIM extends MobRenderer<EntityIMSpider, RenderSpiderIM.InvasionLivingRenderState, SpiderModel> {
    private static final ResourceLocation T_SPIDER = ResourceLocation.withDefaultNamespace("textures/entity/spider/spider.png");
    private static final ResourceLocation T_JUMPING = ResourceLocation.parse("invmod:textures/spiderT2.png");
    private static final ResourceLocation T_MOTHER = ResourceLocation.parse("invmod:textures/spiderT2b.png");

    public RenderSpiderIM(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.9F);
    }

    @Override
    public InvasionLivingRenderState createRenderState() {
        return new InvasionLivingRenderState();
    }

    @Override
    public void extractRenderState(EntityIMSpider entity, InvasionLivingRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.textureId = entity.getTextureId();
    }

    @Override
    public ResourceLocation getTextureLocation(InvasionLivingRenderState state) {
        return switch (state.textureId) {
            case 1 -> T_JUMPING;
            case 2 -> T_MOTHER;
            default -> T_SPIDER;
        };
    }

    public static final class InvasionLivingRenderState extends LivingEntityRenderState {
        int textureId;
    }
}
