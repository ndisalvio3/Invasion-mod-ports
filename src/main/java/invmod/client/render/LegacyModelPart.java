package invmod.client.render;

import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

public class LegacyModelPart {
    private static final float LEGACY_UNIT_SCALE = 0.0625F;
    private static int childCounter = 0;

    private final List<ModelPart.Cube> cubes;
    private final Map<String, ModelPart> children;
    private final ModelPart part;
    private int textureOffsetX;
    private int textureOffsetY;
    private int textureWidth = 64;
    private int textureHeight = 32;

    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public boolean mirror;

    public LegacyModelPart(LegacyModelBase model, int textureOffsetX, int textureOffsetY) {
        this.cubes = new ArrayList<>();
        this.children = new LinkedHashMap<>();
        this.part = new ModelPart(this.cubes, this.children);
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        if (model != null) {
            this.textureWidth = model.textureWidth;
            this.textureHeight = model.textureHeight;
        }
    }

    public LegacyModelPart(LegacyModelBase model, String unusedName) {
        this(model, 0, 0);
    }

    public void addBox(float x, float y, float z, int width, int height, int depth) {
        this.addBox(x, y, z, width, height, depth, 0.0F);
    }

    public void addBox(float x, float y, float z, int width, int height, int depth, float inflation) {
        ModelPart.Cube cube = new ModelPart.Cube(
            this.textureOffsetX,
            this.textureOffsetY,
            x,
            y,
            z,
            width,
            height,
            depth,
            inflation,
            inflation,
            inflation,
            this.mirror,
            this.textureWidth,
            this.textureHeight,
            EnumSet.allOf(Direction.class)
        );
        this.cubes.add(cube);
    }

    public void setRotationPoint(float x, float y, float z) {
        this.rotationPointX = x;
        this.rotationPointY = y;
        this.rotationPointZ = z;
    }

    public void setTextureSize(int textureWidth, int textureHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public LegacyModelPart setTextureOffset(int textureOffsetX, int textureOffsetY) {
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        return this;
    }

    public void addChild(LegacyModelPart child) {
        if (child == null) {
            return;
        }
        this.children.put("child" + childCounter++, child.part);
    }

    public void render(float scale) {
        LegacyRenderContext context = LegacyRenderContext.current();
        if (context == null) {
            return;
        }
        syncPartTransform();

        context.poseStack.pushPose();
        float renderScale = scale / LEGACY_UNIT_SCALE;
        if (renderScale != 1.0F) {
            context.poseStack.scale(renderScale, renderScale, renderScale);
        }
        this.part.render(context.poseStack, context.vertexConsumer, context.packedLight, context.packedOverlay, context.packedColor);
        context.poseStack.popPose();
    }

    public void postRender(float scale) {
        LegacyRenderContext context = LegacyRenderContext.current();
        if (context == null) {
            return;
        }
        context.poseStack.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
        if (this.rotateAngleZ != 0.0F) {
            context.poseStack.mulPose(Axis.ZP.rotation(this.rotateAngleZ));
        }
        if (this.rotateAngleY != 0.0F) {
            context.poseStack.mulPose(Axis.YP.rotation(this.rotateAngleY));
        }
        if (this.rotateAngleX != 0.0F) {
            context.poseStack.mulPose(Axis.XP.rotation(this.rotateAngleX));
        }
    }

    private void syncPartTransform() {
        this.part.x = this.rotationPointX;
        this.part.y = this.rotationPointY;
        this.part.z = this.rotationPointZ;
        this.part.xRot = this.rotateAngleX;
        this.part.yRot = this.rotateAngleY;
        this.part.zRot = this.rotateAngleZ;
    }
}
