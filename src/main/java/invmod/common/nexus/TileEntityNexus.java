package invmod.common.nexus;

import com.whammich.invasion.registry.ModBlockEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IMenuProviderExtension;

public class TileEntityNexus extends BlockEntity implements INexusAccess, MenuProvider, IMenuProviderExtension {
    private boolean active;

    public TileEntityNexus(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEXUS.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockNexus.ACTIVE) && state.getValue(BlockNexus.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(BlockNexus.ACTIVE, active), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("Active", active);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        active = tag.getBooleanOr("Active", false);
    }

    public void debugStartInvaion(int startWave) {
    }

    public void emergencyStop() {
    }

    public boolean setSpawnRadius(int radius) {
        return false;
    }

    public void debugStatus() {
    }

    public void createBolt(int x, int y, int z, int time) {
        if (level != null) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x + 0.5D, y + 0.5D, z + 0.5D, 0.0D, 0.2D, 0.0D);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.invasion.nexus");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ContainerNexus(containerId, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
    }

    @Override
    public int getXCoord() {
        return worldPosition.getX();
    }

    @Override
    public int getYCoord() {
        return worldPosition.getY();
    }

    @Override
    public int getZCoord() {
        return worldPosition.getZ();
    }

    @Override
    public net.minecraft.world.level.Level getLevel() {
        return level;
    }
}
