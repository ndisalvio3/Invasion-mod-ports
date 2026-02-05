package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

public class LegacyModelBase extends EntityModel<EntityRenderState> {
    public int textureWidth = 64;
    public int textureHeight = 32;
    public boolean isRiding;
    public boolean isChild;
    public float onGround;

    public LegacyModelBase() {
        super(new ModelPart(List.of(), Map.of()));
    }

    @Override
    public void setupAnim(EntityRenderState state) {
    }

    public void renderLegacy(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        renderLegacy(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor, null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    public void renderLegacy(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        int packedLight,
        int packedOverlay,
        int packedColor,
        Entity entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        LegacyRenderContext.with(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor, () -> {
            this.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 0.0625F);
        });
    }

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    }

    public void setRotationAngles(
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        float scale,
        Entity entity
    ) {
    }
}
