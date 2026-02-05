package invmod.client.render;

import invmod.common.entity.EntityIMWolf;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RenderIMWolf extends MobRenderer<EntityIMWolf, WolfRenderState, WolfModel> {
    private static final ResourceLocation WOLF_WILD = ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf.png");
    private static final ResourceLocation WOLF_TAME = ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_tame.png");
    private static final ResourceLocation WOLF_TAME_NEXUS = ResourceLocation.parse("invmod:textures/wolf_tame_nexus.png");

    public RenderIMWolf(EntityRendererProvider.Context context) {
        super(context, new WolfModel(context.bakeLayer(ModelLayers.WOLF)), 0.6F);
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    @Override
    public void extractRenderState(EntityIMWolf entity, WolfRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isAngry = entity.getTarget() != null;
        state.isSitting = false;
        state.tailAngle = (float) (Math.PI / 5);
        state.headRollAngle = 0.0F;
        state.shakeAnim = 0.0F;
        state.wetShade = 1.0F;
        state.texture = resolveWolfTexture(entity);
        state.collarColor = null;
        state.bodyArmorItem = ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getTextureLocation(WolfRenderState state) {
        return state.texture;
    }

    private static ResourceLocation resolveWolfTexture(EntityIMWolf entity) {
        return switch (entity.getTextureId()) {
            case EntityIMWolf.TEXTURE_NEXUS -> WOLF_TAME_NEXUS;
            case EntityIMWolf.TEXTURE_WILD -> WOLF_WILD;
            default -> WOLF_TAME;
        };
    }
}
