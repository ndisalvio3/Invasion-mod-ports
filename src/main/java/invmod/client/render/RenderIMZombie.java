package invmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import invmod.common.entity.EntityIMZombie;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderIMZombie extends LegacyEntityRenderer<EntityIMZombie> {
    private static final ResourceLocation T_OLD = ResourceLocation.parse("invmod:textures/zombie_old.png");
    private static final ResourceLocation T_T1A = ResourceLocation.parse("invmod:textures/zombieT1a.png");
    private static final ResourceLocation T_PIG = ResourceLocation.parse("invmod:textures/pigzombie64x32.png");
    private static final ResourceLocation T_T2 = ResourceLocation.parse("invmod:textures/zombieT2.png");
    private static final ResourceLocation T_T2A = ResourceLocation.parse("invmod:textures/zombieT2a.png");
    private static final ResourceLocation T_T3 = ResourceLocation.parse("invmod:textures/zombieT3.png");
    private static final ResourceLocation T_TAR = ResourceLocation.parse("invmod:textures/zombietar.png");

    public RenderIMZombie(EntityRendererProvider.Context context) {
        super(context, new ModelBigBiped(), 0.5F);
    }

    @Override
    protected void scale(LegacyRenderState state, PoseStack poseStack) {
        if (state.entity instanceof EntityIMZombie zombie) {
            float s = zombie.scaleAmount();
            poseStack.scale(s, (2.0F + s) / 3.0F, s);
        }
    }

    @Override
    protected ResourceLocation getTextureLocation(EntityIMZombie entity) {
        return switch (entity.getTextureId()) {
            case 1 -> T_T1A;
            case 2 -> T_T2;
            case 3 -> T_PIG;
            case 4 -> T_T2A;
            case 5 -> T_TAR;
            case 6 -> T_T3;
            default -> T_OLD;
        };
    }
}
