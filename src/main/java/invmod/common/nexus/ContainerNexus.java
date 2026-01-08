package invmod.common.nexus;

import com.whammich.invasion.registry.ModMenus;
import com.whammich.invasion.registry.ModBlocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class ContainerNexus extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final TileEntityNexus nexus;

    public ContainerNexus(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof TileEntityNexus found ? found : null);
    }

    public ContainerNexus(int containerId, Inventory inventory, TileEntityNexus nexus) {
        super(ModMenus.NEXUS.get(), containerId);
        this.nexus = nexus;
        this.access = nexus != null ? ContainerLevelAccess.create(nexus.getLevel(), nexus.getBlockPos()) : ContainerLevelAccess.NULL;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.NEXUS.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
