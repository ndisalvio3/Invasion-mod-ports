package com.whammich.invasion.registry;

import com.whammich.invasion.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reference.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INVASION_TAB = TABS.register("invasion", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.invasion"))
        .icon(() -> ModItems.INFUSED_SWORD.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(ModBlocks.NEXUS_ITEM.get());
            output.accept(ModItems.CATALYST_MIXTURE_UNSTABLE.get());
            output.accept(ModItems.CATALYST_MIXTURE_STABLE.get());
            output.accept(ModItems.NEXUS_CATALYST_UNSTABLE.get());
            output.accept(ModItems.NEXUS_CATALYST_STABLE.get());
            output.accept(ModItems.CATALYST_STRONG.get());
            output.accept(ModItems.DAMPING_AGENT_WEAK.get());
            output.accept(ModItems.DAMPING_AGENT_STRONG.get());
            output.accept(ModItems.SMALL_REMNANTS.get());
            output.accept(ModItems.RIFT_FLUX.get());
            output.accept(ModItems.PHASE_CRYSTAL.get());
            output.accept(ModItems.NEXUS_ADJUSTER.get());
            output.accept(ModItems.MATERIAL_PROBE.get());
            output.accept(ModItems.STRANGE_BONE.get());
            output.accept(ModItems.DEBUG_WAND.get());
            output.accept(ModItems.ENGINEER_HAMMER.get());
            output.accept(ModItems.INFUSED_SWORD.get());
            output.accept(ModItems.SEARING_BOW.get());
            output.accept(ModItems.TRAP_EMPTY.get());
            output.accept(ModItems.TRAP_RIFT.get());
            output.accept(ModItems.TRAP_FLAME.get());
            output.accept(ModItems.IM_ZOMBIE_EGG.get());
            output.accept(ModItems.IM_SPIDER_EGG.get());
            output.accept(ModItems.IM_WOLF_EGG.get());
            output.accept(ModItems.IM_ZOMBIE_PIGMAN_EGG.get());
            output.accept(ModItems.IM_SKELETON_EGG.get());
            output.accept(ModItems.IM_IMP_EGG.get());
            output.accept(ModItems.IM_THROWER_EGG.get());
            output.accept(ModItems.IM_BIRD_EGG.get());
            output.accept(ModItems.IM_BURROWER_EGG.get());
        })
        .build());

    private ModCreativeTabs() {
    }
}
