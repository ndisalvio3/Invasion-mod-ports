package com.whammich.invasion.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class InvasionConfig {
    public static final ModConfigSpec SPEC;
    public static final Common COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        SPEC = builder.build();
    }

    private InvasionConfig() {
    }

    public static final class Common {
        public final ModConfigSpec.BooleanValue enableLogging;
        public final ModConfigSpec.BooleanValue updateMessagesEnabled;
        public final ModConfigSpec.BooleanValue destructedBlocksDrop;
        public final ModConfigSpec.BooleanValue mobsDropSmallRemnants;
        public final ModConfigSpec.BooleanValue craftItemsEnabled;
        public final ModConfigSpec.BooleanValue debugMode;
        public final ModConfigSpec.IntValue guiIdNexus;
        public final ModConfigSpec.IntValue minContinuousModeDays;
        public final ModConfigSpec.IntValue maxContinuousModeDays;
        public final ModConfigSpec.BooleanValue nightSpawnsEnabled;
        public final ModConfigSpec.IntValue nightMobSpawnChance;
        public final ModConfigSpec.IntValue nightMobMaxGroupSize;
        public final ModConfigSpec.IntValue mobLimitOverride;
        public final ModConfigSpec.BooleanValue nightMobsBurnInDay;
        public final ModConfigSpec.IntValue nightMobSightRange;
        public final ModConfigSpec.IntValue nightMobSenseRange;

        private Common(ModConfigSpec.Builder builder) {
            builder.push("general");
            enableLogging = builder
                .comment("Enables logging additional information to the console.")
                .define("enableLogging", true);
            updateMessagesEnabled = builder
                .comment("Enable update notification messages.")
                .define("updateMessagesEnabled", false);
            destructedBlocksDrop = builder
                .comment("Whether blocks destroyed by invasion events drop items.")
                .define("destructedBlocksDrop", true);
            mobsDropSmallRemnants = builder
                .comment("Whether invasion mobs drop small remnants.")
                .define("mobsDropSmallRemnants", true);
            craftItemsEnabled = builder
                .comment("Whether invasion items can be crafted.")
                .define("craftItemsEnabled", true);
            debugMode = builder
                .comment("Enables additional debug behavior.")
                .define("debugMode", false);
            guiIdNexus = builder
                .comment("Legacy Nexus GUI id (kept for compatibility).")
                .defineInRange("guiIdNexus", 76, 0, 32767);
            minContinuousModeDays = builder
                .comment("Minimum days before a continuous mode invasion starts.")
                .defineInRange("minContinuousModeDays", 2, 1, 365);
            maxContinuousModeDays = builder
                .comment("Maximum days before a continuous mode invasion starts.")
                .defineInRange("maxContinuousModeDays", 3, 1, 365);
            nightSpawnsEnabled = builder
                .comment("Whether night spawns are enabled.")
                .define("nightSpawnsEnabled", false);
            nightMobSpawnChance = builder
                .comment("Relative chance for night mob spawns (higher means more common).")
                .defineInRange("nightMobSpawnChance", 30, 1, 100);
            nightMobMaxGroupSize = builder
                .comment("Maximum number of night mobs that can spawn together.")
                .defineInRange("nightMobMaxGroupSize", 3, 1, 64);
            mobLimitOverride = builder
                .comment("Override for the global mob cap used by night spawns.")
                .defineInRange("mobLimitOverride", 70, 1, 512);
            nightMobsBurnInDay = builder
                .comment("Whether night mobs burn during the day.")
                .define("nightMobsBurnInDay", true);
            nightMobSightRange = builder
                .comment("How far night mobs can see a player.")
                .defineInRange("nightMobSightRange", 20, 4, 128);
            nightMobSenseRange = builder
                .comment("How far night mobs can sense a player through walls.")
                .defineInRange("nightMobSenseRange", 12, 2, 128);
            builder.pop();
        }
    }
}
