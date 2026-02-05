package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyEntityRenderer.LegacyRenderState> {
    protected final LegacyModelBase model;

    public LegacyEntityRenderer(EntityRendererProvider.Context context) {
        this(context, null, 0.0F);
    }

    public LegacyEntityRenderer(EntityRendererProvider.Context context, LegacyModelBase model, float shadowRadius) {
        super(context);
        this.model = model;
        this.shadowRadius = shadowRadius;
    }

    @Override
    public LegacyRenderState createRenderState() {
        return new LegacyRenderState();
    }

    @Override
    public void extractRenderState(T entity, LegacyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.bodyYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        state.headYaw = 0.0F;
        state.headPitch = entity.getXRot(partialTick);
        state.limbSwing = 0.0F;
        state.limbSwingAmount = 0.0F;

        if (entity instanceof LivingEntity livingEntity) {
            state.bodyYaw = Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            float head = Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot);
            state.headYaw = Mth.wrapDegrees(head - state.bodyYaw);
            state.headPitch = livingEntity.getXRot(partialTick);
            state.limbSwing = livingEntity.walkAnimation.position(partialTick);
            state.limbSwingAmount = livingEntity.walkAnimation.speed(partialTick);
        }
    }

    @Override
    public void render(LegacyRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (this.model != null && state.entity != null) {
            poseStack.pushPose();
            setupRotations(state, poseStack);
            scale(state, poseStack);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);

            @SuppressWarnings("unchecked")
            T entity = (T) state.entity;
            prepareModel(entity, this.model, state.partialTick, state);
            ResourceLocation texture = getTextureLocation(entity);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(texture));
            this.model.renderLegacy(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                -1,
                entity,
                state.limbSwing,
                state.limbSwingAmount,
                state.ageInTicks,
                state.headYaw,
                state.headPitch
            );
            poseStack.popPose();
        }

        super.render(state, poseStack, bufferSource, packedLight);
    }

    protected void setupRotations(LegacyRenderState state, PoseStack poseStack) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyYaw));
    }

    protected void scale(LegacyRenderState state, PoseStack poseStack) {
    }

    protected void prepareModel(T entity, LegacyModelBase model, float partialTick, LegacyRenderState state) {
    }

    protected ResourceLocation getTextureLocation(T entity) {
        return TextureManager.INTENTIONAL_MISSING_TEXTURE;
    }

    public static class LegacyRenderState extends EntityRenderState {
        public Entity entity;
        public float limbSwing;
        public float limbSwingAmount;
        public float bodyYaw;
        public float headYaw;
        public float headPitch;
    }
}
