package com.whammich.invasion.config;

public record InvasionConfigSnapshot(
    boolean enableLogging,
    boolean updateMessagesEnabled,
    boolean destructedBlocksDrop,
    boolean mobsDropSmallRemnants,
    boolean craftItemsEnabled,
    boolean debugMode,
    int guiIdNexus,
    int minContinuousModeDays,
    int maxContinuousModeDays,
    boolean nightSpawnsEnabled,
    int nightMobSpawnChance,
    int nightMobMaxGroupSize,
    int mobLimitOverride,
    boolean nightMobsBurnInDay,
    int nightMobSightRange,
    int nightMobSenseRange
) {
    public static InvasionConfigSnapshot fromConfig() {
        return new InvasionConfigSnapshot(
            InvasionConfig.COMMON.enableLogging.get(),
            InvasionConfig.COMMON.updateMessagesEnabled.get(),
            InvasionConfig.COMMON.destructedBlocksDrop.get(),
            InvasionConfig.COMMON.mobsDropSmallRemnants.get(),
            InvasionConfig.COMMON.craftItemsEnabled.get(),
            InvasionConfig.COMMON.debugMode.get(),
            InvasionConfig.COMMON.guiIdNexus.get(),
            InvasionConfig.COMMON.minContinuousModeDays.get(),
            InvasionConfig.COMMON.maxContinuousModeDays.get(),
            InvasionConfig.COMMON.nightSpawnsEnabled.get(),
            InvasionConfig.COMMON.nightMobSpawnChance.get(),
            InvasionConfig.COMMON.nightMobMaxGroupSize.get(),
            InvasionConfig.COMMON.mobLimitOverride.get(),
            InvasionConfig.COMMON.nightMobsBurnInDay.get(),
            InvasionConfig.COMMON.nightMobSightRange.get(),
            InvasionConfig.COMMON.nightMobSenseRange.get()
        );
    }
}
