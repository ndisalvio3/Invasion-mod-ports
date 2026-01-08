package com.whammich.invasion.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class ItemSwordInfused extends Item {
    public ItemSwordInfused(Item.Properties properties) {
        super(properties.sword(ToolMaterial.IRON, 3.0F, -2.4F));
    }
}
