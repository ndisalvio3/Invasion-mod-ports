package com.whammich.invasion.registry;

import com.whammich.invasion.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModSounds {
    public static final DeferredHolder<SoundEvent, SoundEvent> BIG_ZOMBIE = register("bigzombie1");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIREBALL = register("fireball1");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCRAPE_1 = register("scrape1");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCRAPE_2 = register("scrape2");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCRAPE_3 = register("scrape3");

    public static void init() {
    }

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return ModRegistries.SOUND_EVENTS.register(name,
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Reference.MODID, name)));
    }
}
