package invmod.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;
import java.util.Random;

public final class LegacyWorld {
    private final Level level;
    public final Random rand;
    public final boolean isRemote;
    public final Object theProfiler = null;

    public LegacyWorld(Level level) {
        this.level = level;
        this.isRemote = level.isClientSide;
        this.rand = new Random(level.getGameTime());
    }

    public Block getBlock(int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public int getBlockMetadata(int x, int y, int z) {
        return 0;
    }

    public boolean setBlock(int x, int y, int z, Block block, int meta, int flags) {
        BlockState state = block.defaultBlockState();
        return level.setBlock(new BlockPos(x, y, z), state, flags);
    }

    public void setBlockMetadataWithNotify(int x, int y, int z, int meta, int flags) {
        // metadata unsupported; no-op
    }

    public BlockEntity getTileEntity(int x, int y, int z) {
        return level.getBlockEntity(new BlockPos(x, y, z));
    }

    public void playSoundAtEntity(Entity entity, String soundId, float volume, float pitch) {
        // legacy string-based sounds are not mapped; no-op for now
    }

    public void spawnEntityInWorld(Entity entity) {
        level.addFreshEntity(entity);
    }

    public void setEntityState(Entity entity, byte state) {
        level.broadcastEntityEvent(entity, state);
    }

    public Player getClosestVulnerablePlayerToEntity(Entity entity, double distance) {
        return level.getNearestPlayer(entity, distance);
    }

    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<T> klass, AABB bounds) {
        return level.getEntitiesOfClass(klass, bounds);
    }

    public HitResult rayTraceBlocks(Vec3 start, Vec3 end) {
        return level.clip(new net.minecraft.world.level.ClipContext(start, end, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.ANY, CollisionContext.empty()));
    }
}
