package com.whammich.invasion.items;

import com.whammich.invasion.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class ModToolMaterials {
    public static final TagKey<Item> INFUSED_TOOL_MATERIALS = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "infused_tool_materials")
    );
    public static final ToolMaterial INFUSED = new ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL,
        40,
        6.0F,
        2.0F,
        14,
        INFUSED_TOOL_MATERIALS
    );

    private ModToolMaterials() {
    }
}
