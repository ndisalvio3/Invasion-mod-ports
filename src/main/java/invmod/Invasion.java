package invmod;

import com.mojang.logging.LogUtils;
import com.whammich.invasion.config.InvasionConfigSnapshot;
import com.whammich.invasion.network.NetworkHandler;
import invmod.common.nexus.TileEntityNexus;
import invmod.common.nexus.MobBuilder;
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
import java.util.List;
import java.util.Map;

public final class Invasion {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS = new String[] {"IMZombie", "IMZombie", "IMSpider"};

    public static final Map<String, Integer> mobHealthInvasion = new HashMap<>();
    public static final Map<String, Integer> mobHealthNightspawn = new HashMap<>();

    public static final Block blockNexus = Blocks.AIR;
    public static final Item itemSmallRemnants = Items.AIR;
    public static final Item itemEngyHammer = Items.AIR;
    public static final Item itemIMTrap = Items.AIR;
    public static final Item itemProbe = Items.AIR;
    public static final Item itemDebugWand = Items.AIR;
    public static final Item itemStrangeBone = Items.AIR;

    public static String latestVersionNumber = "null";
    public static String recentNews = "null";

    private static final MobBuilder MOB_BUILDER = new MobBuilder();
    private static TileEntityNexus focusNexus;
    private static TileEntityNexus activeNexus;
    private static volatile InvasionConfigSnapshot serverConfigSnapshot;

    private Invasion() {
    }

    public static void log(String message) {
        LOGGER.info(message);
    }

    public static boolean isDebug() {
        return getConfigSnapshot().enableLogging();
    }

    public static MobBuilder getMobBuilder() {
        return MOB_BUILDER;
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

    public static int getMobHealth(Entity mob) {
        return 20;
    }

    public static Entity[] getNightMobSpawns1(Level level) {
        return new Entity[0];
    }

    public static ItemStack getRenderHammerItem() {
        return new ItemStack(Items.AIR);
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

    public static int getGuiIdNexus() {
        return 0;
    }

    private static InvasionConfigSnapshot getConfigSnapshot() {
        InvasionConfigSnapshot snapshot = serverConfigSnapshot;
        if (snapshot != null) {
            return snapshot;
        }
        return InvasionConfigSnapshot.fromConfig();
    }
}
