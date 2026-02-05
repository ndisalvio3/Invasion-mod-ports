package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

final class LegacyRenderContext {
    private static final ThreadLocal<LegacyRenderContext> CURRENT = new ThreadLocal<>();

    final PoseStack poseStack;
    final VertexConsumer vertexConsumer;
    final Object entity;
    final float limbSwing;
    final float limbSwingAmount;
    final float ageInTicks;
    final float netHeadYaw;
    final float headPitch;
    final int packedLight;
    final int packedOverlay;
    final int packedColor;

    private LegacyRenderContext(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        Object entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        int packedLight,
        int packedOverlay,
        int packedColor
    ) {
        this.poseStack = poseStack;
        this.vertexConsumer = vertexConsumer;
        this.entity = entity;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.packedLight = packedLight;
        this.packedOverlay = packedOverlay;
        this.packedColor = packedColor;
    }

    static void with(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor, Runnable action) {
        with(poseStack, vertexConsumer, null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, packedLight, packedOverlay, packedColor, action);
    }

    static void with(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        Object entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        int packedLight,
        int packedOverlay,
        int packedColor,
        Runnable action
    ) {
        LegacyRenderContext previous = CURRENT.get();
        CURRENT.set(
            new LegacyRenderContext(
                poseStack,
                vertexConsumer,
                entity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch,
                packedLight,
                packedOverlay,
                packedColor
            )
        );
        try {
            action.run();
        } finally {
            CURRENT.set(previous);
        }
    }

    static LegacyRenderContext current() {
        return CURRENT.get();
    }
}
