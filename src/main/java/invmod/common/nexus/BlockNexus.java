package invmod.common.nexus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockNexus extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public BlockNexus(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityNexus(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof TileEntityNexus nexus) {
            player.openMenu(nexus);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }

        for (int i = 0; i < 6; i++) {
            double y1 = pos.getY() + random.nextFloat();
            double y2 = (random.nextFloat() - 0.5D) * 0.5D;
            int direction = random.nextInt(2) * 2 - 1;
            double x1;
            double x2;
            double z1;
            double z2;

            if (random.nextInt(2) == 0) {
                z1 = pos.getZ() + 0.5D + 0.25D * direction;
                z2 = random.nextFloat() * 2.0F * direction;
                x1 = pos.getX() + random.nextFloat();
                x2 = (random.nextFloat() - 0.5D) * 0.5D;
            } else {
                x1 = pos.getX() + 0.5D + 0.25D * direction;
                x2 = random.nextFloat() * 2.0F * direction;
                z1 = pos.getZ() + random.nextFloat();
                z2 = (random.nextFloat() - 0.5D) * 0.5D;
            }

            level.addParticle(ParticleTypes.PORTAL, x1, y1, z1, x2, y2, z2);
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof TileEntityNexus nexus && nexus.isActive()) {
            return -1.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }
}
