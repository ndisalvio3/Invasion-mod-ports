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
