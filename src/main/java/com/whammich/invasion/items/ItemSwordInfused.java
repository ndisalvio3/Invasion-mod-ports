package com.whammich.invasion.items;

import net.minecraft.world.item.Item;

public class ItemSwordInfused extends Item {
    public ItemSwordInfused(Item.Properties properties) {
        super(properties.sword(ModToolMaterials.INFUSED, 3.0F, -2.4F));
    }
}
