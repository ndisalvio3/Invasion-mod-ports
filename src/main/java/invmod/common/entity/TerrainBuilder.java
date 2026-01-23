package invmod.common.entity;

import invmod.common.INotifyTask;
import invmod.common.util.IPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class TerrainBuilder
        implements ITerrainBuild {
    private static final float LADDER_COST = 25.0F;
    private static final float PLANKS_COST = 45.0F;
    private static final float COBBLE_COST = 65.0F;
    private EntityIMLiving theEntity;
    private ITerrainModify modifier;
    private float buildRate;

    public TerrainBuilder(EntityIMLiving entity, ITerrainModify modifier, float buildRate) {
        this.theEntity = entity;
        this.modifier = modifier;
        this.buildRate = buildRate;
    }

    public float getBuildRate() {
        return this.buildRate;
    }

    public void setBuildRate(float buildRate) {
        this.buildRate = buildRate;
    }

    public boolean askBuildScaffoldLayer(IPosition pos, INotifyTask asker) {
        if (this.modifier.isReadyForTask(asker)) {
            Level level = this.theEntity.level();
            Scaffold scaffold = this.theEntity.getNexus().getAttackerAI().getScaffoldAt(pos);
            if (scaffold != null) {
                int height = pos.getYCoord() - scaffold.getYCoord();
                int xOffset = invmod.common.util.CoordsInt.offsetAdjX[scaffold.getOrientation()];
                int zOffset = invmod.common.util.CoordsInt.offsetAdjZ[scaffold.getOrientation()];
                BlockPos basePos = new BlockPos(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset);
                BlockState blockState = level.getBlockState(basePos);
                Block block = blockState.getBlock();
                List modList = new ArrayList();

                if (height == 1) {
                    if (!blockState.isCollisionShapeFullBlock(level, basePos)) {
                        modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset, Blocks.OAK_PLANKS, (int) (45.0F / this.buildRate)));
                    }
                    BlockPos ladderBase = new BlockPos(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord());
                    blockState = level.getBlockState(ladderBase);
                    if (blockState.isAir()) {
                        modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Blocks.LADDER, (int) (25.0F / this.buildRate)));
                    }
                }
                BlockPos anchorPos = new BlockPos(pos.getXCoord() + xOffset, pos.getYCoord(), pos.getZCoord() + zOffset);
                blockState = level.getBlockState(anchorPos);
                block = blockState.getBlock();
                if (!blockState.isCollisionShapeFullBlock(level, anchorPos)) {
                    modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord(), pos.getZCoord() + zOffset, Blocks.OAK_PLANKS, (int) (45.0F / this.buildRate)));
                }
                BlockPos ladderPos = new BlockPos(pos.getXCoord(), pos.getYCoord(), pos.getZCoord());
                blockState = level.getBlockState(ladderPos);
                block = blockState.getBlock();
                if (block != Blocks.LADDER) {
                    modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), Blocks.LADDER, (int) (25.0F / this.buildRate)));
                }

                if (scaffold.isLayerPlatform(height)) {
                    for (int i = 0; i < 8; i++) {
                        if ((invmod.common.util.CoordsInt.offsetRing1X[i] != xOffset) || (invmod.common.util.CoordsInt.offsetRing1Z[i] != zOffset)) {
                            BlockPos ringPos = new BlockPos(pos.getXCoord() + invmod.common.util.CoordsInt.offsetRing1X[i], pos.getYCoord(), pos.getZCoord() + invmod.common.util.CoordsInt.offsetRing1Z[i]);
                            blockState = level.getBlockState(ringPos);
                            if (!blockState.isCollisionShapeFullBlock(level, ringPos))
                                modList.add(new ModifyBlockEntry(pos.getXCoord() + invmod.common.util.CoordsInt.offsetRing1X[i], pos.getYCoord(), pos.getZCoord() + invmod.common.util.CoordsInt.offsetRing1Z[i], Blocks.OAK_PLANKS, (int) (45.0F / this.buildRate)));
                        }
                    }
                }
                if (modList.size() > 0)
                    return this.modifier.requestTask((ModifyBlockEntry[]) modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
            }
        }
        return false;
    }

    public boolean askBuildLadderTower(IPosition pos, int orientation, int layersToBuild, INotifyTask asker) {
        if (this.modifier.isReadyForTask(asker)) {
            Level level = this.theEntity.level();
            int xOffset = orientation == 1 ? -1 : orientation == 0 ? 1 : 0;
            int zOffset = orientation == 3 ? -1 : orientation == 2 ? 1 : 0;
            List modList = new ArrayList();

            BlockPos basePos = new BlockPos(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset);
            BlockState blockState = level.getBlockState(basePos);
            if (!blockState.isCollisionShapeFullBlock(level, basePos)) {
                modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset, Blocks.OAK_PLANKS, (int) (45.0F / this.buildRate)));
            }
            BlockPos ladderBase = new BlockPos(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord());
            blockState = level.getBlockState(ladderBase);
            if (blockState.isAir()) {
                modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Blocks.LADDER, (int) (25.0F / this.buildRate)));
            }
            for (int i = 0; i < layersToBuild; i++) {
                BlockPos anchorPos = new BlockPos(pos.getXCoord() + xOffset, pos.getYCoord() + i, pos.getZCoord() + zOffset);
                blockState = level.getBlockState(anchorPos);
                if (!blockState.isCollisionShapeFullBlock(level, anchorPos)) {
                    modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord() + i, pos.getZCoord() + zOffset, Blocks.OAK_PLANKS, (int) (45.0F / this.buildRate)));
                }
                BlockPos ladderPos = new BlockPos(pos.getXCoord(), pos.getYCoord() + i, pos.getZCoord());
                blockState = level.getBlockState(ladderPos);
                if (blockState.getBlock() != Blocks.LADDER) {
                    modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() + i, pos.getZCoord(), Blocks.LADDER, (int) (25.0F / this.buildRate)));
                }
            }
            if (modList.size() > 0)
                return this.modifier.requestTask((ModifyBlockEntry[]) modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
        }
        return false;
    }

    public boolean askBuildLadder(IPosition pos, INotifyTask asker) {
        if (this.modifier.isReadyForTask(asker)) {
            Level level = this.theEntity.level();
            List modList = new ArrayList();
            BlockPos ladderPos = new BlockPos(pos.getXCoord(), pos.getYCoord(), pos.getZCoord());
            BlockState blockState = level.getBlockState(ladderPos);
            if (blockState.getBlock() != Blocks.LADDER) {
                if (EntityIMPigEngy.canPlaceLadderAt(level, pos.getXCoord(), pos.getYCoord(), pos.getZCoord()))
                    modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), Blocks.LADDER, (int) (25.0F / this.buildRate)));
                else {
                    return false;
                }
            }

            BlockPos belowTwo = new BlockPos(pos.getXCoord(), pos.getYCoord() - 2, pos.getZCoord());
            blockState = level.getBlockState(belowTwo);
            if (!blockState.isAir() && blockState.isSolidRender()) {
                if (EntityIMPigEngy.canPlaceLadderAt(level, pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord())) {
                    modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Blocks.LADDER, (int) (25.0F / this.buildRate)));
                }
            }
            if (modList.size() > 0)
                return this.modifier.requestTask((ModifyBlockEntry[]) modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
        }
        return false;
    }

    public boolean askBuildBridge(IPosition pos, INotifyTask asker) {
        if (this.modifier.isReadyForTask(asker)) {
            Level level = this.theEntity.level();
            List modList = new ArrayList();
            BlockPos basePos = new BlockPos(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord());
            if (level.getBlockState(basePos).isAir()) {
                BlockPos belowTwo = new BlockPos(pos.getXCoord(), pos.getYCoord() - 2, pos.getZCoord());
                BlockPos belowThree = new BlockPos(pos.getXCoord(), pos.getYCoord() - 3, pos.getZCoord());
                if ((this.theEntity.avoidsBlock(level.getBlockState(belowTwo))) || (this.theEntity.avoidsBlock(level.getBlockState(belowThree)))) {
                    modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Blocks.COBBLESTONE, (int) (65.0F / this.buildRate)));
                } else {
                    modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Blocks.OAK_PLANKS, (int) (45.0F / this.buildRate)));
                }

                if (modList.size() > 0)
                    return this.modifier.requestTask((ModifyBlockEntry[]) modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
            }
        }
        return false;
    }
}
