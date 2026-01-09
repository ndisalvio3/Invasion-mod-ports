package com.whammich.invasion.items;

import com.whammich.invasion.network.NetworkHandler;
import com.whammich.invasion.network.payload.CustomEffectPayload;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemMaterials extends Item {
    public enum Type {
        CATALYST_MIXTURE_UNSTABLE,
        CATALYST_MIXTURE_STABLE,
        NEXUS_CATALYST_UNSTABLE,
        NEXUS_CATALYST_STABLE,
        CATALYST_STRONG,
        DAMPING_AGENT_WEAK,
        DAMPING_AGENT_STRONG,
        SMALL_REMNANTS,
        RIFT_FLUX,
        PHASE_CRYSTAL
    }

    private final Type type;

    public ItemMaterials(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof TileEntityNexus nexus)) {
            return InteractionResult.PASS;
        }

        boolean applied = switch (type) {
            case CATALYST_MIXTURE_UNSTABLE -> nexus.addPowerLevel(10);
            case CATALYST_MIXTURE_STABLE -> nexus.addPowerLevel(20);
            case CATALYST_STRONG -> nexus.addPowerLevel(40);
            case NEXUS_CATALYST_UNSTABLE -> nexus.addNexusLevel(1);
            case NEXUS_CATALYST_STABLE -> nexus.addNexusLevel(1);
            case DAMPING_AGENT_WEAK -> nexus.adjustSpawnRadius(-2);
            case DAMPING_AGENT_STRONG -> nexus.adjustSpawnRadius(-4);
            default -> false;
        };

        if (!applied) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player != null && !player.isCreative()) {
            context.getItemInHand().shrink(1);
        }
        if (player != null) {
            NetworkHandler.sendItemInteraction(player, "Nexus material applied.", true);
            NetworkHandler.sendCustomEffect(player, nexus.getBlockPos(), CustomEffectPayload.EffectType.NEXUS_MATERIAL_APPLIED);
        }
        return InteractionResult.SUCCESS;
    }
}
