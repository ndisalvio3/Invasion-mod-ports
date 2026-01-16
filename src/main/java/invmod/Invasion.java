package invmod;

import com.mojang.logging.LogUtils;
import com.whammich.invasion.config.InvasionConfigSnapshot;
import com.whammich.invasion.network.NetworkHandler;
import com.whammich.invasion.registry.ModBlocks;
import com.whammich.invasion.registry.ModItems;
import invmod.common.nexus.TileEntityNexus;
import invmod.common.nexus.IEntityIMPattern;
import invmod.common.nexus.IMWaveBuilder;
import invmod.common.nexus.MobBuilder;
import invmod.common.util.ISelect;
import invmod.common.util.RandomSelectionPool;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Invasion {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS = new String[] {
        "zombie_t1_any", "zombie_t2_any_basic", "zombie_t2_plain", "zombie_t2_tar",
        "zombie_t3_any", "zombiePigman_t1_any", "zombiePigman_t2_any", "zombiePigman_t3_any", "spider_t1_any",
        "spider_t2_any", "pigengy_t1_any", "skeleton_t1_any", "thrower_t1", "thrower_t2", "creeper_t1_basic",
        "imp_t1"
    };
    private static final float[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS = new float[] {
        1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5F,
        0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F
    };

    public static final Map<String, Integer> mobHealthInvasion = new HashMap<>();
    public static final Map<String, Integer> mobHealthNightspawn = new HashMap<>();

    public static Block blockNexus = Blocks.AIR;
    public static Item itemSmallRemnants = Items.AIR;
    public static Item itemEngyHammer = Items.AIR;
    public static Item itemIMTrap = Items.AIR;
    public static Item itemProbe = Items.AIR;
    public static Item itemDebugWand = Items.AIR;
    public static Item itemStrangeBone = Items.AIR;

    public static String latestVersionNumber = "null";
    public static String recentNews = "null";

    private static final MobBuilder MOB_BUILDER = new MobBuilder();
    private static volatile ISelect<IEntityIMPattern> nightSpawnPool1;
    private static TileEntityNexus focusNexus;
    private static TileEntityNexus activeNexus;
    private static volatile InvasionConfigSnapshot serverConfigSnapshot;

    private Invasion() {
    }

    public static void log(String message) {
        if (message == null || !getConfigSnapshot().enableLogging()) {
            return;
        }
        LOGGER.info(message);
    }

    public static boolean isDebug() {
        return getConfigSnapshot().debugMode();
    }

    public static MobBuilder getMobBuilder() {
        return MOB_BUILDER;
    }

    public static void bindRegistries() {
        blockNexus = ModBlocks.NEXUS.get();
        itemSmallRemnants = ModItems.SMALL_REMNANTS.get();
        itemEngyHammer = ModItems.ENGINEER_HAMMER.get();
        itemIMTrap = ModItems.TRAP_EMPTY.get();
        itemProbe = ModItems.MATERIAL_PROBE.get();
        itemDebugWand = ModItems.DEBUG_WAND.get();
        itemStrangeBone = ModItems.STRANGE_BONE.get();
    }

    public static int getNightMobSightRange() {
        return getConfigSnapshot().nightMobSightRange();
    }

    public static int getNightMobSenseRange() {
        return getConfigSnapshot().nightMobSenseRange();
    }

    public static boolean getNightMobsBurnInDay() {
        return getConfigSnapshot().nightMobsBurnInDay();
    }

    public static boolean getDestructedBlocksDrop() {
        return getConfigSnapshot().destructedBlocksDrop();
    }

    public static boolean getMobsDropSmallRemnants() {
        return getConfigSnapshot().mobsDropSmallRemnants();
    }

    public static boolean getCraftItemsEnabled() {
        return getConfigSnapshot().craftItemsEnabled();
    }

    public static int getGuiIdNexus() {
        return getConfigSnapshot().guiIdNexus();
    }

    public static int getMinContinuousModeDays() {
        return getConfigSnapshot().minContinuousModeDays();
    }

    public static int getMaxContinuousModeDays() {
        return getConfigSnapshot().maxContinuousModeDays();
    }

    public static boolean getNightSpawnsEnabled() {
        return getConfigSnapshot().nightSpawnsEnabled();
    }

    public static int getNightMobSpawnChance() {
        return getConfigSnapshot().nightMobSpawnChance();
    }

    public static int getNightMobMaxGroupSize() {
        return getConfigSnapshot().nightMobMaxGroupSize();
    }

    public static int getMobLimitOverride() {
        return getConfigSnapshot().mobLimitOverride();
    }

    public static int getMobHealth(Entity mob) {
        return 20;
    }

    public static Entity[] getNightMobSpawns1(Level level) {
        if (!getNightSpawnsEnabled()) {
            return new Entity[0];
        }
        if (level == null) {
            return new Entity[0];
        }
        ISelect<IEntityIMPattern> mobPool = getNightSpawnPool();
        if (mobPool == null) {
            return new Entity[0];
        }
        int numberOfMobs = level.getRandom().nextInt(getNightMobMaxGroupSize()) + 1;
        ArrayList<Entity> entities = new ArrayList<>(numberOfMobs);
        for (int i = 0; i < numberOfMobs; i++) {
            IEntityIMPattern pattern = mobPool.selectNext();
            if (pattern == null) {
                continue;
            }
            Entity entity = getMobBuilder().createMobFromConstruct(pattern.generateEntityConstruct(), level, null);
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities.toArray(new Entity[0]);
    }

    private static ISelect<IEntityIMPattern> getNightSpawnPool() {
        ISelect<IEntityIMPattern> currentPool = nightSpawnPool1;
        if (currentPool != null) {
            return currentPool;
        }
        if (DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length != DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS.length) {
            log("Mob pattern table element mismatch. Ensure each slot has a probability weight");
            return null;
        }
        RandomSelectionPool<IEntityIMPattern> mobPool = new RandomSelectionPool<>();
        for (int i = 0; i < DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length; i++) {
            String patternName = DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS[i];
            float weight = DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS[i];
            if (weight <= 0.0F) {
                continue;
            }
            if (IMWaveBuilder.isPatternNameValid(patternName)) {
                mobPool.addEntry(IMWaveBuilder.getPattern(patternName), weight);
            } else {
                log("Night spawn pattern slot " + (i + 1) + " not recognized: " + patternName);
            }
        }
        nightSpawnPool1 = mobPool;
        return mobPool;
    }

    public static ItemStack getRenderHammerItem() {
        return new ItemStack(itemEngyHammer);
    }

    public static void sendMessageToPlayer(ServerPlayer player, String message) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    public static void sendMessageToPlayers(Collection<ServerPlayer> players, String message) {
        if (players == null) {
            return;
        }
        for (ServerPlayer player : players) {
            sendMessageToPlayer(player, message);
        }
    }

    public static void broadcastToAll(String message) {
        LOGGER.info(message);
        NetworkHandler.broadcastMessage(message);
    }

    public static boolean getUpdateNotifications() {
        return getConfigSnapshot().updateMessagesEnabled();
    }

    public static String getLatestVersionNumber() {
        return latestVersionNumber;
    }

    public static String getRecentNews() {
        return recentNews;
    }

    public static String getVersionNumber() {
        return "1.1.2";
    }

    public static TileEntityNexus getFocusNexus() {
        return focusNexus;
    }

    public static TileEntityNexus getActiveNexus() {
        return activeNexus;
    }

    public static void setNexusClicked(TileEntityNexus nexus) {
        focusNexus = nexus;
    }

    public static void setActiveNexus(TileEntityNexus nexus) {
        activeNexus = nexus;
    }

    public static void applyServerConfig(InvasionConfigSnapshot snapshot) {
        serverConfigSnapshot = snapshot;
    }

    private static InvasionConfigSnapshot getConfigSnapshot() {
        InvasionConfigSnapshot snapshot = serverConfigSnapshot;
        if (snapshot != null) {
            return snapshot;
        }
        return InvasionConfigSnapshot.fromConfig();
    }
}
