package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import invmod.common.entity.EntityIMBolt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class RenderBolt extends EntityRenderer<EntityIMBolt, EntityRenderState> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/lightning_bolt.png");

    public RenderBolt(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.2F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(EntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.7F, 0.7F, 0.7F);

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        consumer.addVertex(pose, -0.5F, -0.25F, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(0.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, 0.5F, -0.25F, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(1.0F, 1.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, 0.5F, 0.75F, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(1.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, -0.5F, 0.75F, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv(0.0F, 0.0F)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(0.0F, 1.0F, 0.0F);

        poseStack.popPose();
        super.render(state, poseStack, buffer, packedLight);
    }
}
