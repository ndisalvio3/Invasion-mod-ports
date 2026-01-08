package invmod.common;

import invmod.common.entity.PathAction;
import invmod.common.entity.PathNode;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.Holder;

public class TerrainDataLayer implements IBlockAccessExtended, BlockGetter {
    public static final int EXT_DATA_SCAFFOLD_METAPOSITION = 16384;
    private final BlockGetter world;
    private Int2IntOpenHashMap dataLayer;

    public TerrainDataLayer(BlockGetter world) {
        this.world = world;
        this.dataLayer = new Int2IntOpenHashMap();
    }

    public void setData(int x, int y, int z, Integer data) {
        this.dataLayer.put(PathNode.makeHash(x, y, z, PathAction.NONE), data.intValue());
    }

    public int getLayeredData(int x, int y, int z) {
        int key = PathNode.makeHash(x, y, z, PathAction.NONE);
        return this.dataLayer.getOrDefault(key, 0);
    }

    public void setAllData(Int2IntOpenHashMap data) {
        this.dataLayer = data;
    }

    public Block getBlock(int x, int y, int z) {
        return this.world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public BlockEntity getBlockTileEntity(int x, int y, int z) {
        return this.world.getBlockEntity(new BlockPos(x, y, z));
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.world.getBlockEntity(pos);
    }

    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int meta) {
        BlockPos pos = new BlockPos(x, y, z);
        return this.world.getLightEmission(pos);
    }

    //useless?
//  public float getBrightness(int x, int y, int z, int meta)
//  {
//    return this.world.getBrightness(x, y, z, meta);
//  }
//
//  public float getLightBrightness(int x, int y, int z)
//  {
//    return this.world.getLightBrightness(x, y, z);
//  }

    public int getBlockMetadata(int x, int y, int z) {
        return 0;
    }

    public boolean isBlockOpaqueCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = this.world.getBlockState(pos);
        return state.isSolidRender();
    }

    public boolean isBlockNormalCube(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = this.world.getBlockState(pos);
        return state.isCollisionShapeFullBlock(this.world, pos);
    }

    public boolean isAirBlock(int x, int y, int z) {
        return this.world.getBlockState(new BlockPos(x, y, z)).isAir();
    }

    public Holder<Biome> getBiomeGenForCoords(int i, int j) {
        if (this.world instanceof net.minecraft.world.level.Level level) {
            return level.getBiome(new BlockPos(i, 0, j));
        }
        return null;
    }

    public int getHeight() {
        if (this.world instanceof net.minecraft.world.level.LevelHeightAccessor accessor) {
            return accessor.getHeight();
        }
        return 0;
    }

    @Override
    public int getMinY() {
        if (this.world instanceof net.minecraft.world.level.LevelHeightAccessor accessor) {
            return accessor.getMinY();
        }
        return 0;
    }

    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    public boolean doesBlockHaveSolidTopSurface(int x, int y, int z) {
        return this.world.getBlockState(new BlockPos(x, y, z)).isSolid();
    }


    public int isBlockProvidingPowerTo(int var1, int var2, int var3, int var4) {
        return 0;
    }

    public boolean isBlockSolidOnSide(int x, int y, int z, net.minecraft.core.Direction side, boolean _default) {
        return this.world.getBlockState(new BlockPos(x, y, z)).isSolid();
    }

    public BlockEntity getTileEntity(int x, int y, int z) {
        return this.world.getBlockEntity(new BlockPos(x, y, z));
    }

    public boolean isSideSolid(int x, int y, int z, net.minecraft.core.Direction side, boolean _default) {
        return this.world.getBlockState(new BlockPos(x, y, z)).isSolid();
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return this.world.getBlockState(pos);
    }

    @Override
    public net.minecraft.world.level.material.FluidState getFluidState(BlockPos pos) {
        return this.world.getFluidState(pos);
    }
}
