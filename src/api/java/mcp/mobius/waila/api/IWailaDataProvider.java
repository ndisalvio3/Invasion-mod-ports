package mcp.mobius.waila.api;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IWailaDataProvider {
    ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config);
    List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config);
    List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config);
    List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config);
    CompoundTag getNBTData(ServerPlayer player, BlockEntity te, CompoundTag tag, Level world, int x, int y, int z);
}
