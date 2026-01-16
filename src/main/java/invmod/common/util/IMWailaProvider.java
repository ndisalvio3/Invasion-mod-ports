package invmod.common.util;

import invmod.common.nexus.BlockNexus;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

@WailaPlugin
public final class IMWailaProvider implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(NexusComponentProvider.INSTANCE, TileEntityNexus.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(NexusComponentProvider.INSTANCE, BlockNexus.class);
    }

    private enum NexusComponentProvider implements IComponentProvider<BlockAccessor>, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        private static final String KEY_ACTIVE = "invasionActive";
        private static final String KEY_WAVE = "invasionWave";
        private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("invasion", "nexus");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            boolean active = data.getBooleanOr(KEY_ACTIVE, false);
            tooltip.add(Component.translatable("waila.invasion.status", active));
            if (active) {
                int wave = data.getIntOr(KEY_WAVE, 0);
                tooltip.add(Component.translatable("waila.invasion.wavenumber", wave));
            }
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            BlockEntity blockEntity = accessor.getBlockEntity();
            if (!(blockEntity instanceof TileEntityNexus nexus)) {
                return;
            }

            data.putBoolean(KEY_ACTIVE, nexus.isActive());
            if (nexus.isActive()) {
                data.putInt(KEY_WAVE, nexus.getCurrentWave());
            }
        }
    }
}
