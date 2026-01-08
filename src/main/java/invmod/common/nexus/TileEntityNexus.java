package invmod.common.nexus;

import com.whammich.invasion.registry.ModBlockEntities;
import invmod.Invasion;
import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.ai.AttackerAI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IMenuProviderExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityNexus extends BlockEntity implements Container, INexusAccess, MenuProvider, IMenuProviderExtension {
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int SLOT_COUNT = 2;
    private static final int ACTIVATION_TICKS = 400;
    private static final int GENERATION_TICKS = 3000;
    private static final int COOK_TICKS = 1200;
    private static final int TICK_MILLIS = 50;
    private static final int DEFAULT_POWER_LEVEL = 100;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final List<EntityIMLiving> mobs = new ArrayList<>();
    private final Map<String, Long> boundPlayers = new HashMap<>();
    private final AttackerAI attackerAI = new AttackerAI(this);
    private boolean active;
    private int activationTimer;
    private int currentWave;
    private int nexusLevel = 1;
    private int nexusKills;
    private int spawnRadius = 52;
    private int generation;
    private int powerLevel;
    private int cookTime;
    private int mode;
    private int waveRestTimer;
    private IMWaveSpawner waveSpawner;

    public TileEntityNexus(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEXUS.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            if (state.hasProperty(BlockNexus.ACTIVE) && state.getValue(BlockNexus.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(BlockNexus.ACTIVE, active), 3);
            }
        }
    }

    @Override
    public boolean isActivating() {
        return activationTimer > 0 && activationTimer < ACTIVATION_TICKS;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileEntityNexus nexus) {
        if (level.isClientSide) {
            return;
        }
        nexus.serverTick();
    }

    private void serverTick() {
        mobs.removeIf(mob -> mob == null || !mob.isAlive() || mob.isRemoved());

        if (!active) {
            if (activationTimer > 0) {
                activationTimer = Math.max(0, activationTimer - 1);
            }
            return;
        }

        ensureWaveSpawner();
        attackerAI.update();

        if (waveRestTimer > 0) {
            waveRestTimer--;
            if (waveRestTimer == 0) {
                beginWave(currentWave + 1);
            }
            return;
        }

        try {
            waveSpawner.spawn(TICK_MILLIS);
            if (waveSpawner.isWaveComplete()) {
                waveRestTimer = Math.max(1, waveSpawner.getWaveRestTime() / TICK_MILLIS);
            }
        } catch (WaveSpawnerException e) {
            Invasion.log("Nexus wave failed: " + e.getMessage());
            emergencyStop();
        }
    }

    @Override
    public int getMode() {
        return mode;
    }

    void setMode(int mode) {
        this.mode = mode;
        setActive(mode != 0);
    }

    @Override
    public int getActivationTimer() {
        return activationTimer;
    }

    void setActivationTimer(int activationTimer) {
        this.activationTimer = activationTimer;
    }

    @Override
    public int getSpawnRadius() {
        return spawnRadius;
    }

    public boolean setSpawnRadius(int radius) {
        if (active) {
            return false;
        }
        if (radius <= 0) {
            return false;
        }
        spawnRadius = radius;
        setChanged();
        return true;
    }

    public boolean adjustSpawnRadius(int delta) {
        return setSpawnRadius(spawnRadius + delta);
    }

    @Override
    public int getNexusKills() {
        return nexusKills;
    }

    void setNexusKills(int nexusKills) {
        this.nexusKills = nexusKills;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    void setGeneration(int generation) {
        this.generation = generation;
    }

    @Override
    public int getNexusLevel() {
        return nexusLevel;
    }

    void setNexusLevel(int nexusLevel) {
        this.nexusLevel = nexusLevel;
    }

    public boolean addNexusLevel(int amount) {
        if (amount <= 0) {
            return false;
        }
        nexusLevel += amount;
        setChanged();
        return true;
    }

    public int getNexusPowerLevel() {
        return powerLevel;
    }

    void setNexusPowerLevel(int powerLevel) {
        this.powerLevel = powerLevel;
    }

    public boolean addPowerLevel(int amount) {
        if (amount <= 0) {
            return false;
        }
        powerLevel = Math.max(0, powerLevel + amount);
        setChanged();
        return true;
    }

    @Override
    public int getCurrentWave() {
        return currentWave;
    }

    void setWave(int currentWave) {
        this.currentWave = currentWave;
    }

    public int getCookTime() {
        return cookTime;
    }

    void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public int getActivationProgressScaled(int scale) {
        return activationTimer * scale / ACTIVATION_TICKS;
    }

    public int getGenerationProgressScaled(int scale) {
        return generation * scale / GENERATION_TICKS;
    }

    public int getCookProgressScaled(int scale) {
        return cookTime * scale / COOK_TICKS;
    }

    @Override
    public void attackNexus(int damage) {
        if (!active) {
            return;
        }
        powerLevel = Math.max(0, powerLevel - Math.max(1, damage));
        if (powerLevel == 0) {
            emergencyStop();
        }
        setChanged();
    }

    @Override
    public void registerMobDied() {
        nexusKills++;
        setChanged();
    }

    @Override
    public List<EntityIMLiving> getMobList() {
        return mobs;
    }

    @Override
    public void registerMob(EntityIMLiving mob) {
        if (mob == null || mobs.contains(mob)) {
            return;
        }
        mobs.add(mob);
    }

    @Override
    public Map<String, Long> getBoundPlayers() {
        return boundPlayers;
    }

    @Override
    public AttackerAI getAttackerAI() {
        return attackerAI;
    }

    public void bindPlayer(Player player) {
        if (player != null) {
            boundPlayers.put(player.getGameProfile().getName(), player.level().getGameTime());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("Active", active);
        tag.putInt("ActivationTimer", activationTimer);
        tag.putInt("Mode", mode);
        tag.putInt("CurrentWave", currentWave);
        tag.putInt("NexusLevel", nexusLevel);
        tag.putInt("NexusKills", nexusKills);
        tag.putInt("SpawnRadius", spawnRadius);
        tag.putInt("Generation", generation);
        tag.putInt("PowerLevel", powerLevel);
        tag.putInt("CookTime", cookTime);
        tag.putInt("WaveRestTimer", waveRestTimer);
        ContainerHelper.saveAllItems(tag, items, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        active = tag.getBooleanOr("Active", false);
        activationTimer = tag.getIntOr("ActivationTimer", 0);
        mode = tag.getIntOr("Mode", 0);
        currentWave = tag.getIntOr("CurrentWave", 0);
        nexusLevel = tag.getIntOr("NexusLevel", 1);
        nexusKills = tag.getIntOr("NexusKills", 0);
        spawnRadius = tag.getIntOr("SpawnRadius", spawnRadius);
        generation = tag.getIntOr("Generation", 0);
        powerLevel = tag.getIntOr("PowerLevel", 0);
        cookTime = tag.getIntOr("CookTime", 0);
        waveRestTimer = tag.getIntOr("WaveRestTimer", 0);
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    public void debugStartInvaion(int startWave) {
        startInvasion(startWave);
    }

    public void emergencyStop() {
        active = false;
        waveRestTimer = 0;
        mode = 0;
        activationTimer = 0;
        if (waveSpawner != null) {
            waveSpawner.stop();
        }
        Invasion.setActiveNexus(null);
        setChanged();
    }

    public void debugStatus() {
        Invasion.log("Nexus status: active=" + active + " wave=" + currentWave + " mode=" + mode + " power=" + powerLevel);
    }

    public void createBolt(int x, int y, int z, int time) {
        if (level != null) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x + 0.5D, y + 0.5D, z + 0.5D, 0.0D, 0.2D, 0.0D);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.invasion.nexus");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ContainerNexus(containerId, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
    }

    @Override
    public int getXCoord() {
        return worldPosition.getX();
    }

    @Override
    public int getYCoord() {
        return worldPosition.getY();
    }

    @Override
    public int getZCoord() {
        return worldPosition.getZ();
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT;
    }

    private void ensureWaveSpawner() {
        if (waveSpawner == null) {
            waveSpawner = new IMWaveSpawner(this, spawnRadius);
        } else {
            waveSpawner.setRadius(spawnRadius);
        }
    }

    private void beginWave(int waveNumber) {
        ensureWaveSpawner();
        try {
            waveSpawner.beginNextWave(waveNumber);
            currentWave = waveNumber;
            mode = 1;
            setActive(true);
            setChanged();
        } catch (WaveSpawnerException e) {
            Invasion.log("Failed to start wave " + waveNumber + ": " + e.getMessage());
            emergencyStop();
        }
    }

    private void startInvasion(int startWave) {
        if (level == null || level.isClientSide) {
            return;
        }
        powerLevel = Math.max(powerLevel, DEFAULT_POWER_LEVEL + (nexusLevel - 1) * 20);
        waveRestTimer = 0;
        activationTimer = 0;
        setActive(true);
        Invasion.setActiveNexus(this);
        beginWave(Math.max(1, startWave));
    }
}
