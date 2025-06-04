package com.whammich.invasion.register;

import com.whammich.invasion.items.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class ItemRegistry {

    public static final Item materials = new ItemMaterials();
    public static final Item debugWand = new ItemWandDebug();
    public static final Item hammerEngineer = new ItemHammerEngineer();
    public static final Item swordInfused = new ItemSwordInfused();
    public static final Item probe = new ItemProbe();
    public static final Item bowSearing = new ItemBowSearing();
    public static final Item strangeBone = new ItemStrangeBone();
    public static final Item trap = new ItemTrap();

    public static void registerItems() {

        GameRegistry.registerItem(materials, "ItemMaterials");
        GameRegistry.registerItem(debugWand, "ItemWandDebug");
        GameRegistry.registerItem(hammerEngineer, "ItemHammerEngineer");
        GameRegistry.registerItem(swordInfused, "ItemSwordInfused");
        GameRegistry.registerItem(probe, "ItemProbe");
        GameRegistry.registerItem(bowSearing, "ItemBowSearing");
        GameRegistry.registerItem(strangeBone, "ItemStrangeBone");
        GameRegistry.registerItem(trap, "ItemTrap");
    }
}
