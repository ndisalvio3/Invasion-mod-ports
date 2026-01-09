package com.whammich.invasion.network;

import net.minecraft.nbt.CompoundTag;

public interface AdvancedSpawnData {
    void writeSpawnData(CompoundTag tag);

    void readSpawnData(CompoundTag tag);
}
