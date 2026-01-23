package invmod.common.entity;

import invmod.Invasion;
import invmod.common.INotifyTask;
import invmod.common.util.Distance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class TerrainModifier
        implements ITerrainModify {
    private static final float DEFAULT_REACH = 2.0F;
    private EntityIMLiving theEntity;
    private INotifyTask taskSetter;
    private INotifyTask blockNotify;
    private List<ModifyBlockEntry> modList;
    private ModifyBlockEntry nextEntry;
    private ModifyBlockEntry lastEntry;
    private int entryIndex;
    private int timer;
    private float reach;
    private boolean outOfRangeFlag;
    private boolean terrainFailFlag;

    public TerrainModifier(EntityIMLiving entity, float defaultReach) {
        this.theEntity = entity;
        this.modList = new ArrayList();
        this.entryIndex = 0;
        this.timer = 0;
        this.reach = defaultReach;
    }

    public void onUpdate() {
        taskUpdate();
    }

    public boolean isReadyForTask(INotifyTask asker) {
        return (this.modList.size() == 0) || (this.taskSetter == asker);
    }

    public void cancelTask() {
        endTask();
    }

    public boolean isBusy() {
        return this.timer > 0;
    }

    public boolean requestTask(ModifyBlockEntry[] entries, INotifyTask onFinished, INotifyTask onBlockChange) {
        if (isReadyForTask(onFinished)) {
            for (ModifyBlockEntry entry : entries) {
                this.modList.add(entry);
            }
            this.taskSetter = onFinished;
            this.blockNotify = onBlockChange;
            return true;
        }
        return false;
    }

    public ModifyBlockEntry getLastBlockModified() {
        return this.lastEntry;
    }

    private void taskUpdate() {
        if (this.timer > 1) {
            this.timer -= 1;
            return;
        }
        if (this.timer == 1) {
            this.entryIndex += 1;
            this.timer = 0;
            int result = changeBlock(this.nextEntry) ? 0 : 1;
            this.lastEntry = this.nextEntry;
            if (this.blockNotify != null) {
                this.blockNotify.notifyTask(result);
            }
        }

        if (this.entryIndex < this.modList.size()) {
            this.nextEntry = ((ModifyBlockEntry) this.modList.get(this.entryIndex));
            while (isTerrainIdentical(this.nextEntry)) {
                this.entryIndex += 1;
                if (this.entryIndex < this.modList.size()) {
                    this.nextEntry = ((ModifyBlockEntry) this.modList.get(this.entryIndex));
                } else {
                    endTask();
                    return;
                }
            }

            this.timer = this.nextEntry.getCost();
            if (this.timer == 0)
                this.timer = 1;
        } else if (this.modList.size() > 0) {
            endTask();
        }
    }

    private void endTask() {
        this.entryIndex = 0;
        this.timer = 0;
        this.modList.clear();
        if (this.taskSetter != null)
            this.taskSetter.notifyTask(this.outOfRangeFlag ? 1 : this.terrainFailFlag ? 2 : 0);
    }

    private boolean changeBlock(ModifyBlockEntry entry) {
        if (Distance.distanceBetween(this.theEntity.getX(), this.theEntity.getY() + this.theEntity.getBbHeight() / 2.0F, this.theEntity.getZ(), entry.getXCoord() + 0.5D, entry.getYCoord() + 0.5D, entry.getZCoord() + 0.5D) > this.reach) {
            this.outOfRangeFlag = true;
            return false;
        }

        Level level = this.theEntity.level();
        BlockPos pos = new BlockPos(entry.getXCoord(), entry.getYCoord(), entry.getZCoord());
        Block newBlock = entry.getNewBlock();
        BlockState oldState = level.getBlockState(pos);
        Block oldBlock = oldState.getBlock();
        entry.setOldBlock(oldBlock);
        if (oldBlock == Invasion.blockNexus) {
            this.terrainFailFlag = true;
            return false;
        }

        boolean succeeded = level.setBlock(pos, newBlock.defaultBlockState(), 3);
        if (succeeded) {
            if (newBlock == Blocks.AIR && Invasion.getDestructedBlocksDrop()) {
                level.destroyBlock(pos, true, this.theEntity);
            }
        } else {
            this.terrainFailFlag = true;
        }
        return succeeded;
    }

    private boolean isTerrainIdentical(ModifyBlockEntry entry) {
        BlockPos pos = new BlockPos(entry.getXCoord(), entry.getYCoord(), entry.getZCoord());
        if (this.theEntity.level().getBlockState(pos).getBlock() == entry.getNewBlock()) {
            return true;
        }
        return false;
    }
}
