package com.whammich.invasion.config;

public record InvasionConfigSnapshot(
    boolean enableLogging,
    boolean updateMessagesEnabled,
    boolean destructedBlocksDrop,
    boolean nightMobsBurnInDay,
    int nightMobSightRange,
    int nightMobSenseRange
) {
    public static InvasionConfigSnapshot fromConfig() {
        return new InvasionConfigSnapshot(
            InvasionConfig.COMMON.enableLogging.get(),
            InvasionConfig.COMMON.updateMessagesEnabled.get(),
            InvasionConfig.COMMON.destructedBlocksDrop.get(),
            InvasionConfig.COMMON.nightMobsBurnInDay.get(),
            InvasionConfig.COMMON.nightMobSightRange.get(),
            InvasionConfig.COMMON.nightMobSenseRange.get()
        );
    }
}
