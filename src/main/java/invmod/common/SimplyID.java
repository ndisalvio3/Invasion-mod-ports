package invmod.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class SimplyID {
    private static int nextSimplyID;
    private static Set<String> loadedIDs = new HashSet();
    private static String loadedWorld = null;
    private static File file = null;
    private static PrintWriter writer = null;

    public static String getNextSimplyID(Entity par1Entity) {
        loadSession(par1Entity.level());

        nextSimplyID = 0;

        int i = nextSimplyID;
        while (true) {
            ResourceLocation key = EntityType.getKey(par1Entity.getType());
            String id = (key == null ? "unknown" : key.toString()) + nextSimplyID++;
            if (loadedIDs.add(id)) {
                writeIDToFile(id);
                return id;
            }
        }
    }

    public static void loadSession(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        String worldDir = serverLevel.getServer().getWorldPath(LevelResource.ROOT).toString();
        if ((loadedWorld == null) || (!worldDir.equals(loadedWorld))) {
            resetSimplyIDTo(serverLevel);
        }
    }

    public static void resetSimplyIDTo(ServerLevel level) {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
        loadedIDs.clear();

        loadedWorld = level.getServer().getWorldPath(LevelResource.ROOT).toString();
        file = level.getServer().getWorldPath(LevelResource.ROOT).resolve("savedIDs.txt").toFile();
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        populateSet();
    }

    public static void writeIDToFile(String id) {
        writer.println(id);
        writer.flush();
    }

    public static void populateSet() {
        FileReader pre = null;
        BufferedReader reader = null;
        try {
            pre = new FileReader(file);
            reader = new BufferedReader(pre);

            String line = null;
            try {
                while ((line = reader.readLine()) != null)
                    if (line.startsWith("delete ")) {
                        deleteID(line, Boolean.valueOf(false));
                    } else
                        addID(line);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        refreshLoadedIDFile();
    }

    private static void refreshLoadedIDFile() {
        try {
            PrintWriter writer = new PrintWriter(file);

            for (String id : loadedIDs) {
                writer.println(id);
            }

            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> getLoadedIDs() {
        return loadedIDs;
    }

    public static void setLoadedIDs(Set<String> _loadedIDs) {
        loadedIDs = _loadedIDs;
    }

    public static void addID(String newID) {
        loadedIDs.add(newID);
    }

    public static void deleteID(String deletedID, Boolean flag) {
        if ((!flag.booleanValue()) && (deletedID.startsWith("delete "))) {
            deletedID = deletedID.split(" ")[1];
        }

        if (flag.booleanValue()) {
            writeIDToFile("delete " + deletedID);
        }

        loadedIDs.remove(deletedID);
    }

    public static void deleteID(Level level, String string) {
        loadSession(level);

        deleteID(string, Boolean.valueOf(true));
    }
}
