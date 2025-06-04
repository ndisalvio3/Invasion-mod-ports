package mcp.mobius.waila.api;

import java.util.List;
import net.minecraft.item.ItemStack;

public interface IWailaDataProvider {
    ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config);
    List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config);
    List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config);
    List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config);
    net.minecraft.nbt.NBTTagCompound getNBTData(net.minecraft.entity.player.EntityPlayerMP player, net.minecraft.tileentity.TileEntity te, net.minecraft.nbt.NBTTagCompound tag, net.minecraft.world.World world, int x, int y, int z);
}
