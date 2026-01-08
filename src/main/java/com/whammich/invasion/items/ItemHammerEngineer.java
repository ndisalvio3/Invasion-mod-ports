package com.whammich.invasion.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class ItemHammerEngineer extends Item {
    public ItemHammerEngineer(Item.Properties properties) {
        super(properties.pickaxe(ToolMaterial.IRON, 2.0F, -2.8F));
    }
}
