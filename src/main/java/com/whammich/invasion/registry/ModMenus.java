package com.whammich.invasion.registry;

import invmod.common.nexus.ContainerNexus;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenus {
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNexus>> NEXUS = ModRegistries.MENUS.register("nexus", () -> IMenuTypeExtension.create(ContainerNexus::new));

    private ModMenus() {
    }
}
