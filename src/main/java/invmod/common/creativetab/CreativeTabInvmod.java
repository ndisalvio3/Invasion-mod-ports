package invmod.common.creativetab;

import com.whammich.invasion.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CreativeTabInvmod {
    public static final String LEGACY_TRANSLATION_KEY = "itemGroup.invasionTab";

    private CreativeTabInvmod() {
    }

    public static Component getDisplayName() {
        return Component.translatable(LEGACY_TRANSLATION_KEY);
    }

    public static ItemStack getIconItem() {
        return ModBlocks.NEXUS_ITEM.get().getDefaultInstance();
    }
}
