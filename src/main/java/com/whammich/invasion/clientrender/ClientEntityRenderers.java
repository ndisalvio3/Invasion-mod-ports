package com.whammich.invasion.clientrender;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.whammich.invasion.Reference;
import com.whammich.invasion.registry.ModEntities;
import com.whammich.invasion.registry.ModItems;
import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMTrap;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.entity.EntityIMSkeleton;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = Reference.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEntityRenderers {
    private static final ResourceLocation ZOMBIE_OLD = texture("zombie_old.png");
    private static final ResourceLocation ZOMBIE_T1A = texture("zombieT1a.png");
    private static final ResourceLocation ZOMBIE_T2 = texture("zombieT2.png");
    private static final ResourceLocation ZOMBIE_T2A = texture("zombieT2a.png");
    private static final ResourceLocation ZOMBIE_T3 = texture("zombieT3.png");
    private static final ResourceLocation ZOMBIE_PIG = texture("pigzombie64x32.png");
    private static final ResourceLocation ZOMBIE_TAR = texture("zombietar.png");
    private static final ResourceLocation ZOMBIE_PIGMAN_T3 = texture("zombiePigmanT3.png");
    private static final ResourceLocation IMP = texture("imp.png");
    private static final ResourceLocation THROWER_T1 = texture("throwerT1.png");
    private static final ResourceLocation THROWER_T2 = texture("throwerT2.png");
    private static final ResourceLocation SPIDER_T2 = texture("spiderT2.png");
    private static final ResourceLocation SPIDER_T2B = texture("spiderT2b.png");
    private static final ResourceLocation SPIDER_VANILLA = ResourceLocation.withDefaultNamespace("textures/entity/spider/spider.png");
    private static final ResourceLocation WOLF_TAME = texture("wolf_tame_nexus.png");
    private static final ResourceLocation VULTURE = texture("vulture.png");
    private static final ResourceLocation BURROWER = texture("burrower.png");
    private static final ResourceLocation BOULDER = texture("boulder.png");
    private static final ResourceLocation EGG = texture("spideregg.png");
    private static final ResourceLocation BOLT = ResourceLocation.withDefaultNamespace("textures/entity/lightning_bolt.png");
    private static final ResourceLocation SKELETON = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.IM_ZOMBIE.get(), ImZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_SPIDER.get(), ImSpiderRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_WOLF.get(), ImWolfRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_ZOMBIE_PIGMAN.get(), ImZombiePigmanRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_SKELETON.get(), ImSkeletonRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_IMP.get(), ImImpRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_THROWER.get(), ImThrowerRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_BIRD.get(), ImBirdRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_BURROWER.get(), ImBurrowerRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_TRAP.get(), ImTrapRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_BOULDER.get(), context -> new SimpleTextureRenderer<>(context, BOULDER, 1.0F));
        event.registerEntityRenderer(ModEntities.IM_EGG.get(), context -> new SimpleTextureRenderer<>(context, EGG, 0.9F));
        event.registerEntityRenderer(ModEntities.IM_BOLT.get(), context -> new SimpleTextureRenderer<>(context, BOLT, 0.7F));
        event.registerEntityRenderer(ModEntities.IM_PRIMED_TNT.get(), context -> new ItemStackRenderer<>(context, new ItemStack(Items.TNT), 0.9F));
        event.registerEntityRenderer(ModEntities.IM_SPAWN_PROXY.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.IM_SFX.get(), NoopRenderer::new);
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/" + path);
    }

    private static final class InvasionHumanoidRenderState extends HumanoidRenderState {
        private int textureId;
    }

    private static final class InvasionLivingRenderState extends LivingEntityRenderState {
        private int textureId;
    }

    private static final class InvasionChickenRenderState extends ChickenRenderState {
        private ResourceLocation texture;
    }

    private static final class TrapRenderState extends EntityRenderState {
        private int trapType;
        private boolean empty;
    }

    private static final class ImZombieRenderer extends HumanoidMobRenderer<EntityIMZombie, InvasionHumanoidRenderState, HumanoidModel<InvasionHumanoidRenderState>> {
        private static final ResourceLocation[] TEXTURES = new ResourceLocation[] {
            ZOMBIE_OLD,
            ZOMBIE_T1A,
            ZOMBIE_T2,
            ZOMBIE_PIG,
            ZOMBIE_T2A,
            ZOMBIE_TAR,
            ZOMBIE_T3
        };

        private ImZombieRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public InvasionHumanoidRenderState createRenderState() {
            return new InvasionHumanoidRenderState();
        }

        @Override
        public void extractRenderState(EntityIMZombie entity, InvasionHumanoidRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.textureId = entity.getTextureId();
        }

        @Override
        public ResourceLocation getTextureLocation(InvasionHumanoidRenderState state) {
            return pickTexture(TEXTURES, state.textureId, ZOMBIE_OLD);
        }
    }

    private static final class ImZombiePigmanRenderer extends HumanoidMobRenderer<EntityIMZombiePigman, InvasionHumanoidRenderState, HumanoidModel<InvasionHumanoidRenderState>> {
        private ImZombiePigmanRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIFIED_PIGLIN)), 0.5F);
        }

        @Override
        public InvasionHumanoidRenderState createRenderState() {
            return new InvasionHumanoidRenderState();
        }

        @Override
        public void extractRenderState(EntityIMZombiePigman entity, InvasionHumanoidRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.textureId = entity.getTextureId();
        }

        @Override
        public ResourceLocation getTextureLocation(InvasionHumanoidRenderState state) {
            return state.textureId == 2 ? ZOMBIE_PIGMAN_T3 : ZOMBIE_PIG;
        }
    }

    private static final class ImImpRenderer extends HumanoidMobRenderer<EntityIMImp, InvasionHumanoidRenderState, HumanoidModel<InvasionHumanoidRenderState>> {
        private ImImpRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.4F);
        }

        @Override
        public InvasionHumanoidRenderState createRenderState() {
            return new InvasionHumanoidRenderState();
        }

        @Override
        public ResourceLocation getTextureLocation(InvasionHumanoidRenderState state) {
            return IMP;
        }
    }

    private static final class ImThrowerRenderer extends HumanoidMobRenderer<EntityIMThrower, InvasionHumanoidRenderState, HumanoidModel<InvasionHumanoidRenderState>> {
        private ImThrowerRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.8F);
        }

        @Override
        public InvasionHumanoidRenderState createRenderState() {
            return new InvasionHumanoidRenderState();
        }

        @Override
        public void extractRenderState(EntityIMThrower entity, InvasionHumanoidRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.textureId = entity.getTextureId();
        }

        @Override
        public ResourceLocation getTextureLocation(InvasionHumanoidRenderState state) {
            return state.textureId == 2 ? THROWER_T2 : THROWER_T1;
        }
    }

    private static final class ImSkeletonRenderer extends HumanoidMobRenderer<EntityIMSkeleton, SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {
        private ImSkeletonRenderer(EntityRendererProvider.Context context) {
            super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
        }

        @Override
        public SkeletonRenderState createRenderState() {
            return new SkeletonRenderState();
        }

        @Override
        public void extractRenderState(EntityIMSkeleton entity, SkeletonRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.isAggressive = false;
            state.isShaking = false;
            state.isHoldingBow = false;
        }

        @Override
        public ResourceLocation getTextureLocation(SkeletonRenderState state) {
            return SKELETON;
        }
    }

    private static final class ImSpiderRenderer extends MobRenderer<EntityIMSpider, InvasionLivingRenderState, SpiderModel> {
        private ImSpiderRenderer(EntityRendererProvider.Context context) {
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
                case 1 -> SPIDER_T2;
                case 2 -> SPIDER_T2B;
                default -> SPIDER_VANILLA;
            };
        }
    }

    private static final class ImWolfRenderer extends MobRenderer<EntityIMWolf, WolfRenderState, WolfModel> {
        private ImWolfRenderer(EntityRendererProvider.Context context) {
            super(context, new WolfModel(context.bakeLayer(ModelLayers.WOLF)), 0.6F);
        }

        @Override
        public WolfRenderState createRenderState() {
            return new WolfRenderState();
        }

        @Override
        public void extractRenderState(EntityIMWolf entity, WolfRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.isAngry = false;
            state.isSitting = false;
            state.tailAngle = (float) (Math.PI / 5);
            state.headRollAngle = 0.0F;
            state.shakeAnim = 0.0F;
            state.wetShade = 1.0F;
            state.texture = WOLF_TAME;
            state.collarColor = null;
            state.bodyArmorItem = ItemStack.EMPTY;
        }

        @Override
        public ResourceLocation getTextureLocation(WolfRenderState state) {
            return state.texture;
        }
    }

    private static final class ImBirdRenderer extends MobRenderer<EntityIMBird, InvasionChickenRenderState, ChickenModel> {
        private ImBirdRenderer(EntityRendererProvider.Context context) {
            super(context, new ChickenModel(context.bakeLayer(ModelLayers.CHICKEN)), 0.5F);
        }

        @Override
        public InvasionChickenRenderState createRenderState() {
            return new InvasionChickenRenderState();
        }

        @Override
        public void extractRenderState(EntityIMBird entity, InvasionChickenRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.flap = 0.0F;
            state.flapSpeed = 0.0F;
            state.variant = null;
            state.texture = VULTURE;
        }

        @Override
        public ResourceLocation getTextureLocation(InvasionChickenRenderState state) {
            return state.texture;
        }
    }

    private static final class ImBurrowerRenderer extends MobRenderer<EntityIMBurrower, LivingEntityRenderState, SilverfishModel> {
        private ImBurrowerRenderer(EntityRendererProvider.Context context) {
            super(context, new SilverfishModel(context.bakeLayer(ModelLayers.SILVERFISH)), 0.5F);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
            return BURROWER;
        }
    }

    private static final class ImTrapRenderer extends EntityRenderer<EntityIMTrap, TrapRenderState> {
        private final ItemRenderer itemRenderer;

        private ImTrapRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        }

        @Override
        public TrapRenderState createRenderState() {
            return new TrapRenderState();
        }

        @Override
        public void extractRenderState(EntityIMTrap entity, TrapRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.trapType = entity.getTrapType();
            state.empty = entity.isEmpty();
        }

        @Override
        public void render(TrapRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            ItemStack stack = getTrapStack(state);
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.1F, 0.0F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(0.7F, 0.7F, 0.7F);
            this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
            poseStack.popPose();
            super.render(state, poseStack, buffer, packedLight);
        }

        private ItemStack getTrapStack(TrapRenderState state) {
            if (state.empty) {
                return new ItemStack(ModItems.TRAP_EMPTY.get());
            }
            return switch (state.trapType) {
                case EntityIMTrap.TRAP_RIFT -> new ItemStack(ModItems.TRAP_RIFT.get());
                case EntityIMTrap.TRAP_FIRE -> new ItemStack(ModItems.TRAP_FLAME.get());
                default -> new ItemStack(ModItems.TRAP_EMPTY.get());
            };
        }
    }

    private static final class SimpleTextureRenderer<T extends net.minecraft.world.entity.Entity> extends EntityRenderer<T, EntityRenderState> {
        private final ResourceLocation texture;
        private final float scale;

        private SimpleTextureRenderer(EntityRendererProvider.Context context, ResourceLocation texture, float scale) {
            super(context);
            this.texture = texture;
            this.scale = scale;
        }

        @Override
        public EntityRenderState createRenderState() {
            return new EntityRenderState();
        }

        @Override
        public void render(EntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            poseStack.pushPose();
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(this.scale, this.scale, this.scale);
            Matrix4f pose = poseStack.last().pose();
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(texture));
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

    private static final class ItemStackRenderer<T extends net.minecraft.world.entity.Entity> extends EntityRenderer<T, EntityRenderState> {
        private final ItemRenderer itemRenderer;
        private final ItemStack itemStack;
        private final float scale;

        private ItemStackRenderer(EntityRendererProvider.Context context, ItemStack itemStack, float scale) {
            super(context);
            this.itemRenderer = Minecraft.getInstance().getItemRenderer();
            this.itemStack = itemStack;
            this.scale = scale;
        }

        @Override
        public EntityRenderState createRenderState() {
            return new EntityRenderState();
        }

        @Override
        public void render(EntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            poseStack.pushPose();
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.scale(scale, scale, scale);
            this.itemRenderer.renderStatic(this.itemStack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
            poseStack.popPose();
            super.render(state, poseStack, buffer, packedLight);
        }
    }

    private static final class NoopRenderer<T extends net.minecraft.world.entity.Entity> extends EntityRenderer<T, EntityRenderState> {
        private NoopRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public EntityRenderState createRenderState() {
            return new EntityRenderState();
        }

    }

    private static ResourceLocation pickTexture(ResourceLocation[] textures, int index, ResourceLocation fallback) {
        if (index >= 0 && index < textures.length) {
            return textures[index];
        }
        return fallback;
    }
}
