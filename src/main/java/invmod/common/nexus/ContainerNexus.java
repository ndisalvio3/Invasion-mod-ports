package invmod.common.nexus;

import com.whammich.invasion.network.NetworkHandler;
import com.whammich.invasion.network.payload.NexusStatusPayload;
import com.whammich.invasion.registry.ModMenus;
import com.whammich.invasion.registry.ModBlocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

public class ContainerNexus extends AbstractContainerMenu {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int SLOT_COUNT = 2;
    private static final int PLAYER_INV_START = SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;
    private static final int DATA_COUNT = 9;

    private static final int DATA_ACTIVATION_TIMER = 0;
    private static final int DATA_MODE = 1;
    private static final int DATA_CURRENT_WAVE = 2;
    private static final int DATA_NEXUS_LEVEL = 3;
    private static final int DATA_NEXUS_KILLS = 4;
    private static final int DATA_SPAWN_RADIUS = 5;
    private static final int DATA_GENERATION = 6;
    private static final int DATA_POWER_LEVEL = 7;
    private static final int DATA_COOK_TIME = 8;
    public static final int BUTTON_START = 0;
    public static final int BUTTON_STOP = 1;
    public static final int BUTTON_RADIUS_UP = 2;
    public static final int BUTTON_RADIUS_DOWN = 3;
    public static final int BUTTON_STATUS = 4;

    private final ContainerLevelAccess access;
    private final TileEntityNexus nexus;
    private final ContainerData data;

    public ContainerNexus(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof TileEntityNexus found ? found : null);
    }

    public ContainerNexus(int containerId, Inventory inventory, TileEntityNexus nexus) {
        super(ModMenus.NEXUS.get(), containerId);
        this.nexus = nexus;
        this.access = nexus != null ? ContainerLevelAccess.create(nexus.getLevel(), nexus.getBlockPos()) : ContainerLevelAccess.NULL;
        Container container = nexus != null ? nexus : new SimpleContainer(SLOT_COUNT);

        addSlot(new Slot(container, INPUT_SLOT, 32, 33));
        addSlot(new SlotOutput(container, OUTPUT_SLOT, 102, 33));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            addSlot(new Slot(inventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }

        this.data = nexus != null ? new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_ACTIVATION_TIMER -> nexus.getActivationTimer();
                    case DATA_MODE -> nexus.getMode();
                    case DATA_CURRENT_WAVE -> nexus.getCurrentWave();
                    case DATA_NEXUS_LEVEL -> nexus.getNexusLevel();
                    case DATA_NEXUS_KILLS -> nexus.getNexusKills();
                    case DATA_SPAWN_RADIUS -> nexus.getSpawnRadius();
                    case DATA_GENERATION -> nexus.getGeneration();
                    case DATA_POWER_LEVEL -> nexus.getNexusPowerLevel();
                    case DATA_COOK_TIME -> nexus.getCookTime();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case DATA_ACTIVATION_TIMER -> nexus.setActivationTimer(value);
                    case DATA_MODE -> nexus.setMode(value);
                    case DATA_CURRENT_WAVE -> nexus.setWave(value);
                    case DATA_NEXUS_LEVEL -> nexus.setNexusLevel(value);
                    case DATA_NEXUS_KILLS -> nexus.setNexusKills(value);
                    case DATA_SPAWN_RADIUS -> nexus.setSpawnRadius(value);
                    case DATA_GENERATION -> nexus.setGeneration(value);
                    case DATA_POWER_LEVEL -> nexus.setNexusPowerLevel(value);
                    case DATA_COOK_TIME -> nexus.setCookTime(value);
                    default -> {
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        } : new SimpleContainerData(DATA_COUNT);

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.NEXUS.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == OUTPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index < SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
            if (index < PLAYER_INV_END) {
                if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    public boolean isActivating() {
        return getActivationTimer() > 0 && getActivationTimer() < 400;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (player.level().isClientSide) {
            return isKnownButton(id);
        }
        if (nexus == null || !isKnownButton(id)) {
            return false;
        }
        switch (id) {
            case BUTTON_START -> {
                if (!nexus.isActive()) {
                    nexus.startInvasionFromUi();
                    NetworkHandler.sendItemInteraction(player, "Nexus activated.", true);
                } else {
                    NetworkHandler.sendItemInteraction(player, "Nexus already active.", true);
                }
            }
            case BUTTON_STOP -> {
                if (nexus.isActive()) {
                    nexus.emergencyStop();
                    NetworkHandler.sendItemInteraction(player, "Nexus stopped.", true);
                } else {
                    NetworkHandler.sendItemInteraction(player, "Nexus already inactive.", true);
                }
            }
            case BUTTON_RADIUS_UP -> {
                if (!nexus.adjustSpawnRadius(2)) {
                    NetworkHandler.sendItemInteraction(player, "Nexus spawn radius locked while active.", true);
                } else {
                    NetworkHandler.sendItemInteraction(player, "Nexus spawn radius: " + nexus.getSpawnRadius(), true);
                }
            }
            case BUTTON_RADIUS_DOWN -> {
                if (!nexus.adjustSpawnRadius(-2)) {
                    NetworkHandler.sendItemInteraction(player, "Nexus spawn radius locked while active.", true);
                } else {
                    NetworkHandler.sendItemInteraction(player, "Nexus spawn radius: " + nexus.getSpawnRadius(), true);
                }
            }
            case BUTTON_STATUS -> NetworkHandler.sendItemInteraction(
                player,
                "Nexus: active=" + nexus.isActive()
                    + " level=" + nexus.getNexusLevel()
                    + " power=" + nexus.getNexusPowerLevel()
                    + " wave=" + nexus.getCurrentWave()
                    + " radius=" + nexus.getSpawnRadius()
                    + " kills=" + nexus.getNexusKills(),
                true
            );
            default -> {
            }
        }
        return true;
    }

    public int getMode() {
        return data.get(DATA_MODE);
    }

    public int getCurrentWave() {
        return data.get(DATA_CURRENT_WAVE);
    }

    public int getNexusLevel() {
        return data.get(DATA_NEXUS_LEVEL);
    }

    public int getNexusKills() {
        return data.get(DATA_NEXUS_KILLS);
    }

    public int getSpawnRadius() {
        return data.get(DATA_SPAWN_RADIUS);
    }

    public int getNexusPowerLevel() {
        return data.get(DATA_POWER_LEVEL);
    }

    public int getActivationProgressScaled(int scale) {
        return getActivationTimer() * scale / 400;
    }

    public int getGenerationProgressScaled(int scale) {
        return getGeneration() * scale / 3000;
    }

    public int getCookProgressScaled(int scale) {
        return getCookTime() * scale / 1200;
    }

    private int getActivationTimer() {
        return data.get(DATA_ACTIVATION_TIMER);
    }

    private int getGeneration() {
        return data.get(DATA_GENERATION);
    }

    private int getCookTime() {
        return data.get(DATA_COOK_TIME);
    }

    public void applySnapshot(NexusStatusPayload payload) {
        data.set(DATA_ACTIVATION_TIMER, payload.activationTimer());
        data.set(DATA_MODE, payload.mode());
        data.set(DATA_CURRENT_WAVE, payload.currentWave());
        data.set(DATA_NEXUS_LEVEL, payload.nexusLevel());
        data.set(DATA_NEXUS_KILLS, payload.nexusKills());
        data.set(DATA_SPAWN_RADIUS, payload.spawnRadius());
        data.set(DATA_GENERATION, payload.generation());
        data.set(DATA_POWER_LEVEL, payload.powerLevel());
        data.set(DATA_COOK_TIME, payload.cookTime());
    }

    private boolean isKnownButton(int id) {
        return id == BUTTON_START
            || id == BUTTON_STOP
            || id == BUTTON_RADIUS_UP
            || id == BUTTON_RADIUS_DOWN
            || id == BUTTON_STATUS;
    }
}
