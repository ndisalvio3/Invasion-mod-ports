package invmod.common.nexus;

import com.whammich.invasion.network.NetworkHandler;
import com.whammich.invasion.registry.ModBlockEntities;
import com.whammich.invasion.registry.ModItems;
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
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IMenuProviderExtension;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.AABB;

public class TileEntityNexus extends BlockEntity implements Container, INexusAccess, MenuProvider, IMenuProviderExtension {
    private static final long BIND_EXPIRE_TIME_MS = 300000L;
    private static final long DAY_TICKS = 24000L;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int SLOT_COUNT = 2;
    private static final int ACTIVATION_TICKS = 400;
    private static final int GENERATION_TICKS = 3000;
    private static final int COOK_TICKS = 1200;
    private static final int TICK_MILLIS = 50;
    private static final int CONTINUOUS_WAVE_SECONDS = 240;
    private static final int CONTINUOUS_POWER_TICK_MS = 2200;
    private static final int DEFAULT_HP = 100;
    private static final int DEFAULT_POWER_LEVEL = 100;
    private static final String TAG_BOUND_PLAYERS = "BoundPlayers";
    private static final String TAG_WAVE_ELAPSED = "WaveElapsed";
    private static final String TAG_QUEUED_START_WAVE = "QueuedStartWave";
    private static final String TAG_CONTINUOUS_ATTACK = "continuousAttack";
    private static final String TAG_NEXT_ATTACK_TIME = "nextAttackTime";
    private static final String TAG_LAST_WORLD_TIME = "lastWorldTime";
    private static final String TAG_LAST_POWER_LEVEL = "lastPowerLevel";
    private static final String TAG_MOBS_LEFT = "mobsLeftInWave";
    private static final String TAG_MOBS_TO_KILL = "mobsToKillInWave";
    private static final String TAG_LAST_MOBS_LEFT = "lastMobsLeftInWave";
    private static final String TAG_ZAP_TIMER = "zapTimer";
    private static final String TAG_WAVE_DELAY_TIMER = "waveDelayTimer";
    private static final String TAG_WAVE_DELAY = "waveDelay";
    private static final String TAG_POWER_LEVEL_TIMER = "powerLevelTimer";
    private static final String TAG_PENDING_ACTIVATION = "PendingActivationType";
    private static final String LEGACY_TAG_ACTIVATION_TIMER = "activationTimer";
    private static final String LEGACY_TAG_WAVE_ELAPSED = "spawnerElapsed";
    private static final String LEGACY_TAG_BOUND_PLAYERS = "boundPlayers";
    private static final SoundEvent WAVE_START_SOUND = SoundEvents.WITHER_SPAWN;
    private static final SoundEvent WAVE_COMPLETE_SOUND = SoundEvents.PLAYER_LEVELUP;
    private static final int PENDING_NONE = 0;
    private static final int PENDING_UNSTABLE_CATALYST = 1;
    private static final int PENDING_STRONG_CATALYST = 2;
    private static final int PENDING_STABLE_CATALYST = 3;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final List<EntityIMLiving> mobs = new ArrayList<>();
    private final Map<String, Long> boundPlayers = new HashMap<>();
    private final AttackerAI attackerAI = new AttackerAI(this);
    private final IMWaveBuilder waveBuilder = new IMWaveBuilder();
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
    private int queuedStartWave;
    private long waveElapsed;
    private IMWaveSpawner waveSpawner;
    private int maxHp = DEFAULT_HP;
    private int hp = DEFAULT_HP;
    private int lastHp = DEFAULT_HP;
    private int tickCount;
    private int lastPowerLevel;
    private int powerLevelTimer;
    private int mobsLeftInWave;
    private int lastMobsLeftInWave;
    private int mobsToKillInWave;
    private long nextAttackTime;
    private long lastWorldTime;
    private int zapTimer;
    private long waveDelayTimer = -1L;
    private long waveDelay;
    private boolean continuousAttack;
    private int pendingActivationType;

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
        updateStatus();

        if (mode == 0) {
            if (activationTimer > 0) {
                return;
            }
            if (powerLevel > 0) {
                enterContinuousIdle();
            }
            return;
        }

        if (mode == 4) {
            if (activationTimer > 0) {
                return;
            }
            return;
        }

        if (mode == 2 || mode == 3) {
            if (powerLevel <= 0) {
                stopInvasion();
                return;
            }
            ensureWaveSpawner();
            resumeContinuousIfNeeded();
            attackerAI.update();
            if (++tickCount >= 60) {
                tickCount = 0;
                bindPlayersInRange();
            }
            doContinuous(TICK_MILLIS);
            return;
        }

        ensureWaveSpawner();
        resumeWaveIfNeeded();
        attackerAI.update();
        if (++tickCount >= 60) {
            tickCount = 0;
            bindPlayersInRange();
        }

        if (waveSpawner != null && waveSpawner.isActive()) {
            generateFlux(1);
        }

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
                if (waveRestTimer == 0) {
                    announceWaveComplete(currentWave);
                }
                waveRestTimer = Math.max(1, waveSpawner.getWaveRestTime() / TICK_MILLIS);
                waveElapsed = 0L;
            }
            waveElapsed = waveSpawner.getElapsedTime();
        } catch (WaveSpawnerException e) {
            Invasion.log("Nexus wave failed: " + e.getMessage());
            emergencyStop();
        }
    }

    private void updateStatus() {
        ItemStack input = items.get(INPUT_SLOT);
        ItemStack output = items.get(OUTPUT_SLOT);
        boolean changed = false;

        if (isItem(input, ModItems.TRAP_EMPTY.get())) {
            int increment = mode == 0 ? 1 : 9;
            if (cookTime < COOK_TICKS) {
                cookTime = Math.min(COOK_TICKS, cookTime + increment);
                changed = true;
            }
            if (cookTime >= COOK_TICKS && canAcceptOutput(output, ModItems.TRAP_RIFT.get())) {
                if (output.isEmpty()) {
                    items.set(OUTPUT_SLOT, new ItemStack(ModItems.TRAP_RIFT.get()));
                } else {
                    output.grow(1);
                }
                consumeInput();
                cookTime = 0;
                changed = true;
            }
        } else if (isItem(input, ModItems.RIFT_FLUX.get())) {
            if (nexusLevel >= 10 && cookTime < COOK_TICKS) {
                cookTime = Math.min(COOK_TICKS, cookTime + 5);
                changed = true;
            }
            if (cookTime >= COOK_TICKS && output.isEmpty()) {
                items.set(OUTPUT_SLOT, new ItemStack(ModItems.CATALYST_STRONG.get()));
                consumeInput();
                cookTime = 0;
                changed = true;
            }
        } else if (cookTime != 0) {
            cookTime = 0;
            changed = true;
        }

        if (updateActivation(input)) {
            changed = true;
        }

        if (changed) {
            setChanged();
        }
    }

    private boolean updateActivation(ItemStack input) {
        boolean changed = false;

        if (activationTimer > 0) {
            if (pendingActivationType == PENDING_NONE && queuedStartWave == 0) {
                activationTimer = 0;
                return true;
            }
            if (pendingActivationType != PENDING_NONE && !isCatalyst(input, pendingActivationType)) {
                activationTimer = 0;
                queuedStartWave = 0;
                pendingActivationType = PENDING_NONE;
                if (mode == 4) {
                    setMode(0);
                }
                return true;
            }

            activationTimer += 1;
            changed = true;

            if (activationTimer >= ACTIVATION_TICKS) {
                activationTimer = 0;
                if (pendingActivationType == PENDING_STABLE_CATALYST) {
                    if (consumeCatalyst(pendingActivationType)) {
                        enterContinuousIdle();
                    } else {
                        setMode(0);
                    }
                    pendingActivationType = PENDING_NONE;
                    return true;
                }

                if (queuedStartWave > 0) {
                    int startWave = queuedStartWave;
                    queuedStartWave = 0;
                    boolean canStart = pendingActivationType == PENDING_NONE || consumeCatalyst(pendingActivationType);
                    pendingActivationType = PENDING_NONE;
                    if (canStart) {
                        startInvasion(startWave);
                    }
                } else {
                    pendingActivationType = PENDING_NONE;
                }
            }

            return changed;
        }

        if (mode == 4 && !isCatalyst(input, PENDING_STABLE_CATALYST)) {
            setMode(0);
            return true;
        }

        if (mode == 0 || mode == 2 || mode == 4) {
            if ((mode == 0 || mode == 4) && isCatalyst(input, PENDING_STABLE_CATALYST)) {
                activationTimer = 1;
                pendingActivationType = PENDING_STABLE_CATALYST;
                setMode(4);
                return true;
            }
            if (isCatalyst(input, PENDING_STRONG_CATALYST)) {
                activationTimer = 1;
                queuedStartWave = 10;
                pendingActivationType = PENDING_STRONG_CATALYST;
                return true;
            }
            if (isCatalyst(input, PENDING_UNSTABLE_CATALYST)) {
                activationTimer = 1;
                queuedStartWave = 1;
                pendingActivationType = PENDING_UNSTABLE_CATALYST;
                return true;
            }
        }

        if (queuedStartWave > 0) {
            activationTimer = 1;
            return true;
        }

        return changed;
    }

    private boolean isCatalyst(ItemStack stack, int type) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return switch (type) {
            case PENDING_UNSTABLE_CATALYST -> stack.is(ModItems.NEXUS_CATALYST_UNSTABLE.get());
            case PENDING_STRONG_CATALYST -> stack.is(ModItems.CATALYST_STRONG.get());
            case PENDING_STABLE_CATALYST -> stack.is(ModItems.NEXUS_CATALYST_STABLE.get());
            default -> false;
        };
    }

    private boolean consumeCatalyst(int type) {
        if (!isCatalyst(items.get(INPUT_SLOT), type)) {
            return false;
        }
        consumeInput();
        return true;
    }

    private void consumeInput() {
        ItemStack input = items.get(INPUT_SLOT);
        if (input.isEmpty()) {
            return;
        }
        input.shrink(1);
        if (input.isEmpty()) {
            items.set(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    private boolean isItem(ItemStack stack, Item item) {
        return !stack.isEmpty() && stack.is(item);
    }

    private boolean canAcceptOutput(ItemStack output, Item target) {
        if (output.isEmpty()) {
            return true;
        }
        if (!output.is(target)) {
            return false;
        }
        return output.getCount() < output.getMaxStackSize();
    }

    private void generateFlux(int increment) {
        generation += increment;
        if (generation < GENERATION_TICKS) {
            return;
        }

        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, new ItemStack(ModItems.RIFT_FLUX.get()));
            generation -= GENERATION_TICKS;
            setChanged();
        } else if (output.is(ModItems.RIFT_FLUX.get()) && output.getCount() < output.getMaxStackSize()) {
            output.grow(1);
            generation -= GENERATION_TICKS;
            setChanged();
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
        this.nexusLevel = Math.max(1, nexusLevel);
        powerLevel = Math.min(powerLevel, getMaxPowerLevel());
    }

    public boolean addNexusLevel(int amount) {
        if (amount <= 0) {
            return false;
        }
        nexusLevel += amount;
        powerLevel = Math.min(powerLevel, getMaxPowerLevel());
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
        powerLevel = Math.min(getMaxPowerLevel(), Math.max(0, powerLevel + amount));
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
        int appliedDamage = Math.max(1, damage);
        hp = Math.max(0, hp - appliedDamage);
        while (hp + 5 <= lastHp) {
            notifyBoundPlayers("Nexus at " + (lastHp - 5) + " hp", null, 0.0F, 0.0F);
            lastHp -= 5;
        }
        if (hp == 0 && mode == 1) {
            theEnd();
        }
        setChanged();
    }

    @Override
    public void registerMobDied() {
        nexusKills++;
        if (continuousAttack && mobsToKillInWave > 0) {
            mobsLeftInWave = Math.max(0, mobsLeftInWave - 1);
            if (mobsLeftInWave <= 0) {
                if (lastMobsLeftInWave > 0) {
                    notifyBoundPlayers("Nexus stabilized.", null, 0.0F, 0.0F);
                    lastMobsLeftInWave = mobsLeftInWave;
                }
            } else {
                int threshold = (int) (mobsToKillInWave * 0.1F);
                if (threshold > 0 && mobsLeftInWave + threshold <= lastMobsLeftInWave) {
                    int percent = 100 - (int) (100.0F * mobsLeftInWave / mobsToKillInWave);
                    notifyBoundPlayers("Nexus stabilized to " + percent + "%", null, 0.0F, 0.0F);
                    lastMobsLeftInWave = Math.max(0, lastMobsLeftInWave - threshold);
                }
            }
        }
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
    public void askForRespawn(EntityIMLiving entity) {
        if (entity == null) {
            return;
        }
        Invasion.log("Stuck entity asking for respawn: " + entity + "  " + entity.getX() + ", " + entity.getY() + ", " + entity.getZ());
        ensureWaveSpawner();
        waveSpawner.askForRespawn(entity);
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
            boundPlayers.put(player.getGameProfile().getName(), System.currentTimeMillis());
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
        tag.putInt(TAG_QUEUED_START_WAVE, queuedStartWave);
        tag.putLong(TAG_WAVE_ELAPSED, waveElapsed);
        tag.putInt("MaxHp", maxHp);
        tag.putInt("Hp", hp);
        tag.putInt("LastHp", lastHp);
        tag.putInt(TAG_LAST_POWER_LEVEL, lastPowerLevel);
        tag.putInt(TAG_POWER_LEVEL_TIMER, powerLevelTimer);
        tag.putInt(TAG_MOBS_LEFT, mobsLeftInWave);
        tag.putInt(TAG_LAST_MOBS_LEFT, lastMobsLeftInWave);
        tag.putInt(TAG_MOBS_TO_KILL, mobsToKillInWave);
        tag.putLong(TAG_NEXT_ATTACK_TIME, nextAttackTime);
        tag.putLong(TAG_LAST_WORLD_TIME, lastWorldTime);
        tag.putInt(TAG_ZAP_TIMER, zapTimer);
        tag.putLong(TAG_WAVE_DELAY_TIMER, waveDelayTimer);
        tag.putLong(TAG_WAVE_DELAY, waveDelay);
        tag.putBoolean(TAG_CONTINUOUS_ATTACK, continuousAttack);
        tag.putInt(TAG_PENDING_ACTIVATION, pendingActivationType);
        if (!boundPlayers.isEmpty()) {
            CompoundTag playersTag = new CompoundTag();
            for (Map.Entry<String, Long> entry : boundPlayers.entrySet()) {
                playersTag.putLong(entry.getKey(), entry.getValue());
            }
            tag.put(TAG_BOUND_PLAYERS, playersTag);
        }
        ContainerHelper.saveAllItems(tag, items, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        active = tag.getBooleanOr("Active", false);
        activationTimer = readTagInt(tag, "ActivationTimer", LEGACY_TAG_ACTIVATION_TIMER, 0);
        mode = tag.getIntOr("Mode", 0);
        active = mode != 0;
        currentWave = tag.getIntOr("CurrentWave", 0);
        nexusLevel = tag.getIntOr("NexusLevel", 1);
        nexusKills = tag.getIntOr("NexusKills", 0);
        spawnRadius = tag.getIntOr("SpawnRadius", spawnRadius);
        generation = tag.getIntOr("Generation", 0);
        powerLevel = tag.getIntOr("PowerLevel", 0);
        cookTime = tag.getIntOr("CookTime", 0);
        waveRestTimer = tag.getIntOr("WaveRestTimer", 0);
        queuedStartWave = tag.getIntOr(TAG_QUEUED_START_WAVE, 0);
        waveElapsed = readTagLong(tag, TAG_WAVE_ELAPSED, LEGACY_TAG_WAVE_ELAPSED, 0L);
        maxHp = Math.max(DEFAULT_HP, tag.getIntOr("MaxHp", DEFAULT_HP));
        hp = Math.min(maxHp, readTagInt(tag, "Hp", "hp", maxHp));
        lastHp = Math.max(hp, tag.getIntOr("LastHp", hp));
        lastPowerLevel = tag.getIntOr(TAG_LAST_POWER_LEVEL, powerLevel);
        powerLevelTimer = tag.getIntOr(TAG_POWER_LEVEL_TIMER, 0);
        mobsLeftInWave = tag.getIntOr(TAG_MOBS_LEFT, 0);
        lastMobsLeftInWave = tag.getIntOr(TAG_LAST_MOBS_LEFT, mobsLeftInWave);
        mobsToKillInWave = tag.getIntOr(TAG_MOBS_TO_KILL, 0);
        nextAttackTime = tag.getLongOr(TAG_NEXT_ATTACK_TIME, 0L);
        lastWorldTime = tag.getLongOr(TAG_LAST_WORLD_TIME, 0L);
        zapTimer = tag.getIntOr(TAG_ZAP_TIMER, 0);
        waveDelayTimer = tag.getLongOr(TAG_WAVE_DELAY_TIMER, -1L);
        waveDelay = tag.getLongOr(TAG_WAVE_DELAY, 0L);
        continuousAttack = tag.getBooleanOr(TAG_CONTINUOUS_ATTACK, false);
        pendingActivationType = tag.getIntOr(TAG_PENDING_ACTIVATION, PENDING_NONE);
        boundPlayers.clear();
        if (tag.contains(TAG_BOUND_PLAYERS)) {
            CompoundTag playersTag = tag.getCompoundOrEmpty(TAG_BOUND_PLAYERS);
            for (String key : playersTag.keySet()) {
                boundPlayers.put(key, normalizeBindTime(playersTag.getLongOr(key, 0L)));
            }
        } else if (tag.contains(LEGACY_TAG_BOUND_PLAYERS)) {
            long bindTime = System.currentTimeMillis();
            ListTag playersTag = tag.getListOrEmpty(LEGACY_TAG_BOUND_PLAYERS);
            for (int i = 0; i < playersTag.size(); i++) {
                CompoundTag entry = playersTag.getCompoundOrEmpty(i);
                String name = entry.getString("name").orElse("");
                if (!name.isEmpty()) {
                    boundPlayers.put(name, bindTime);
                }
            }
        }
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    public void debugStartInvaion(int startWave) {
        startInvasion(startWave);
    }

    public void startInvasionFromUi() {
        if (active || activationTimer > 0) {
            return;
        }
        activationTimer = 1;
        queuedStartWave = 1;
        pendingActivationType = PENDING_NONE;
        setChanged();
    }

    public void emergencyStop() {
        stopInvasion();
    }

    public void debugStatus() {
        Invasion.log("Nexus status: active=" + active + " wave=" + currentWave + " mode=" + mode + " power=" + powerLevel);
    }

    public void createBolt(int x, int y, int z, int time) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            NetworkHandler.sendParticleSound(
                serverLevel,
                new BlockPos(x, y, z),
                ParticleTypes.ELECTRIC_SPARK,
                6,
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.BLOCKS,
                0.6F,
                1.0F
            );
        } else if (level != null) {
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
    public Level getWorld() {
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

    private void resumeWaveIfNeeded() {
        if (waveSpawner == null || waveSpawner.isActive() || waveRestTimer > 0) {
            return;
        }
        int waveNumber = Math.max(1, currentWave);
        try {
            if (waveElapsed > 0) {
                waveSpawner.resumeFromState(waveNumber, waveElapsed, spawnRadius);
                currentWave = waveNumber;
                mode = 1;
                setActive(true);
                acquireEntities();
                setChanged();
            } else {
                beginWave(waveNumber);
            }
        } catch (WaveSpawnerException e) {
            Invasion.log("Failed to resume wave " + waveNumber + ": " + e.getMessage());
            emergencyStop();
        }
    }

    private void resumeContinuousIfNeeded() {
        if (waveSpawner == null || waveSpawner.isActive() || !continuousAttack || waveElapsed <= 0) {
            return;
        }
        try {
            Wave wave = buildContinuousWave();
            if (mobsToKillInWave <= 0) {
                mobsToKillInWave = (int) (wave.getTotalMobAmount() * 0.8F);
            }
            int spawns = waveSpawner.resumeFromState(wave, waveElapsed, spawnRadius);
            mobsLeftInWave = Math.max(0, mobsToKillInWave - spawns);
            lastMobsLeftInWave = mobsLeftInWave;
            mode = 3;
            setActive(true);
            acquireEntities();
            setChanged();
        } catch (WaveSpawnerException e) {
            Invasion.log("Failed to resume continuous wave: " + e.getMessage());
            stopInvasion();
        }
    }

    private void beginWave(int waveNumber) {
        ensureWaveSpawner();
        try {
            waveSpawner.beginNextWave(waveNumber);
            currentWave = waveNumber;
            mode = 1;
            waveElapsed = 0L;
            setActive(true);
            bindPlayersInRange();
            announceWaveStart(waveNumber);
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
        pendingActivationType = PENDING_NONE;
        powerLevel = Math.max(powerLevel, getMaxPowerLevel());
        hp = maxHp;
        lastHp = maxHp;
        waveRestTimer = 0;
        activationTimer = 0;
        queuedStartWave = 0;
        setActive(true);
        Invasion.setActiveNexus(this);
        beginWave(Math.max(1, startWave));
    }

    private void enterContinuousIdle() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (mode == 2 || mode == 3) {
            return;
        }
        setMode(2);
        hp = maxHp;
        lastHp = maxHp;
        lastPowerLevel = powerLevel;
        lastWorldTime = level.getDayTime();
        nextAttackTime = scheduleNextContinuousAttack(lastWorldTime);
        waveDelayTimer = -1L;
        continuousAttack = false;
        zapTimer = 0;
        setChanged();
    }

    private long scheduleNextContinuousAttack(long currentTime) {
        int minDays = Invasion.getMinContinuousModeDays();
        int maxDays = Invasion.getMaxContinuousModeDays();
        int span = Math.max(0, maxDays - minDays);
        int days = minDays + (span == 0 || level == null ? 0 : level.getRandom().nextInt(span + 1));
        return (currentTime / DAY_TICKS) * DAY_TICKS + 14000L + (long) days * DAY_TICKS;
    }

    private Wave buildContinuousWave() {
        float difficulty = 1.0F + powerLevel / 4500.0F;
        float tierLevel = 1.0F + powerLevel / 4500.0F;
        return waveBuilder.generateWave(difficulty, tierLevel, CONTINUOUS_WAVE_SECONDS);
    }

    private void startContinuousWave(long currentTime) throws WaveSpawnerException {
        Wave wave = buildContinuousWave();
        mobsToKillInWave = (int) (wave.getTotalMobAmount() * 0.8F);
        mobsLeftInWave = mobsToKillInWave;
        lastMobsLeftInWave = mobsToKillInWave;
        waveSpawner.beginNextWave(wave);
        continuousAttack = true;
        mode = 3;
        setActive(true);
        Invasion.setActiveNexus(this);
        nextAttackTime = scheduleNextContinuousAttack(currentTime);
        hp = maxHp;
        lastHp = maxHp;
        zapTimer = 0;
        waveDelayTimer = -1L;
        waveElapsed = 0L;
        notifyBoundPlayers("Nexus destabilizing.", WAVE_START_SOUND, 1.0F, 0.8F);
        setChanged();
    }

    private int getMaxPowerLevel() {
        return DEFAULT_POWER_LEVEL + Math.max(0, nexusLevel - 1) * 20;
    }

    private void announceWaveStart(int waveNumber) {
        notifyBoundPlayers("Wave " + waveNumber + " has begun.", WAVE_START_SOUND, 1.0F, 0.8F);
    }

    private void announceWaveComplete(int waveNumber) {
        notifyBoundPlayers("Wave " + waveNumber + " complete.", WAVE_COMPLETE_SOUND, 0.9F, 1.1F);
    }

    private void notifyBoundPlayers(String message, SoundEvent sound, float volume, float pitch) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (boundPlayers.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        boundPlayers.entrySet().removeIf(entry -> now - entry.getValue() > BIND_EXPIRE_TIME_MS);
        for (String playerName : boundPlayers.keySet()) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayerByName(playerName);
            if (player != null) {
                NetworkHandler.sendWaveStatus(player, message, true);
                if (sound != null) {
                    player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
                }
            }
        }
    }

    private void doContinuous(int elapsedMillis) {
        powerLevelTimer += elapsedMillis;
        if (powerLevelTimer > CONTINUOUS_POWER_TICK_MS) {
            powerLevelTimer -= CONTINUOUS_POWER_TICK_MS;
            generateFlux(5 + (int) (5 * powerLevel / 1550.0F));
            addPowerLevel(1);
        }

        if (!continuousAttack) {
            if (level == null) {
                return;
            }
            long currentTime = level.getDayTime();
            long timeOfDay = lastWorldTime % DAY_TICKS;
            if (timeOfDay < 12000 && currentTime % DAY_TICKS >= 12000 && currentTime + 12000 > nextAttackTime) {
                notifyBoundPlayers("Night is looming around the nexus.", null, 0.0F, 0.0F);
            }
            if (lastWorldTime > currentTime) {
                nextAttackTime = Math.max(0, nextAttackTime - (lastWorldTime - currentTime));
            }
            lastWorldTime = currentTime;
            if (nextAttackTime == 0L) {
                nextAttackTime = scheduleNextContinuousAttack(currentTime);
            }
            if (lastWorldTime >= nextAttackTime) {
                try {
                    startContinuousWave(currentTime);
                } catch (WaveSpawnerException e) {
                    Invasion.log("Nexus continuous wave failed: " + e.getMessage());
                    stopInvasion();
                }
            }
            return;
        }

        if (hp <= 0) {
            continuousAttack = false;
            continuousNexusHurt();
            return;
        }

        if (waveSpawner.isWaveComplete()) {
            if (waveDelayTimer == -1L) {
                waveDelayTimer = 0L;
                waveDelay = waveSpawner.getWaveRestTime();
            } else {
                waveDelayTimer += elapsedMillis;
                if (waveDelayTimer > waveDelay && zapTimer < -200) {
                    endContinuousWave();
                }
            }

            zapTimer -= 1;
            if (mobsLeftInWave <= 0 && zapTimer <= 0) {
                if (zapEnemy()) {
                    zapTimer = 23;
                }
            }
        } else {
            try {
                waveSpawner.spawn(elapsedMillis);
                waveElapsed = waveSpawner.getElapsedTime();
            } catch (WaveSpawnerException e) {
                Invasion.log("Nexus continuous wave failed: " + e.getMessage());
                stopInvasion();
            }
        }
    }

    private int readTagInt(CompoundTag tag, String primary, String fallback, int defaultValue) {
        if (tag.contains(primary)) {
            return tag.getIntOr(primary, defaultValue);
        }
        if (tag.contains(fallback)) {
            return tag.getShort(fallback).orElse((short) defaultValue);
        }
        return defaultValue;
    }

    private long readTagLong(CompoundTag tag, String primary, String fallback, long defaultValue) {
        if (tag.contains(primary)) {
            return tag.getLongOr(primary, defaultValue);
        }
        if (tag.contains(fallback)) {
            return tag.getLong(fallback).orElse(defaultValue);
        }
        return defaultValue;
    }

    private void bindPlayersInRange() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB bounds = getBindingBox();
        long now = System.currentTimeMillis();
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, bounds)) {
            String name = player.getGameProfile().getName();
            Long lastBound = boundPlayers.get(name);
            if (lastBound == null || now - lastBound > BIND_EXPIRE_TIME_MS) {
                notifyBoundPlayers(formatBoundMessage(name), null, 0.0F, 0.0F);
            }
            boundPlayers.put(name, now);
        }
        boundPlayers.entrySet().removeIf(entry -> now - entry.getValue() > BIND_EXPIRE_TIME_MS);
        setChanged();
    }

    private AABB getBindingBox() {
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        int xzRange = spawnRadius + 10;
        int yRange = spawnRadius + 40;
        int minY = level != null ? level.getMinY() : y - yRange;
        int maxY = level != null ? level.getMaxY() : y + yRange;
        return new AABB(
            x - xzRange,
            Math.max(minY, y - yRange),
            z - xzRange,
            x + xzRange + 1,
            Math.min(maxY, y + yRange + 1),
            z + xzRange + 1
        );
    }

    private String formatBoundMessage(String name) {
        String suffix = name.toLowerCase().endsWith("s") ? "'" : "'s";
        return name + suffix + " life is now bound to the nexus";
    }

    private void theEnd() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        notifyBoundPlayers("The nexus is destroyed!", SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.0F);
        emergencyStop();
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : boundPlayers.entrySet()) {
            if (now - entry.getValue() <= BIND_EXPIRE_TIME_MS) {
                ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayerByName(entry.getKey());
                if (player != null) {
                    player.hurt(player.damageSources().magic(), 500.0F);
                }
            }
        }
        boundPlayers.clear();
        killAllMobs();
        setChanged();
    }

    private void continuousNexusHurt() {
        notifyBoundPlayers("The nexus is severely damaged!", SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 1.0F);
        killAllMobs();
        if (waveSpawner != null) {
            waveSpawner.stop();
        }
        powerLevel = Math.max(0, Math.round(lastPowerLevel * 0.7F));
        lastPowerLevel = powerLevel;
        waveElapsed = 0L;
        if (powerLevel <= 0) {
            stopInvasion();
        }
        setChanged();
    }

    private void endContinuousWave() {
        waveDelayTimer = -1L;
        continuousAttack = false;
        waveSpawner.stop();
        hp = maxHp;
        lastHp = maxHp;
        lastPowerLevel = powerLevel;
        waveElapsed = 0L;
        mode = 2;
        setActive(true);
        Invasion.setActiveNexus(null);
        setChanged();
    }

    private boolean zapEnemy() {
        if (level == null) {
            return false;
        }
        AABB bounds = getBindingBox();
        List<EntityIMLiving> candidates = level.getEntitiesOfClass(EntityIMLiving.class, bounds);
        if (candidates.isEmpty()) {
            return false;
        }
        EntityIMLiving target = null;
        double farthest = -1.0D;
        for (EntityIMLiving mob : candidates) {
            double distance = mob.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D);
            if (distance > farthest) {
                farthest = distance;
                target = mob;
            }
        }
        if (target == null) {
            return false;
        }
        target.hurt(target.damageSources().magic(), 500.0F);
        createBolt((int) target.getX(), (int) target.getY(), (int) target.getZ(), 15);
        return true;
    }

    private void killAllMobs() {
        if (level == null) {
            return;
        }
        AABB bounds = getBindingBox();
        for (EntityIMLiving mob : level.getEntitiesOfClass(EntityIMLiving.class, bounds)) {
            mob.hurt(mob.damageSources().magic(), 500.0F);
        }
        for (invmod.common.entity.EntityIMWolf wolf : level.getEntitiesOfClass(invmod.common.entity.EntityIMWolf.class, bounds)) {
            wolf.hurt(wolf.damageSources().magic(), 500.0F);
        }
        mobs.clear();
    }

    private int acquireEntities() {
        if (level == null) {
            return 0;
        }
        AABB bounds = getBindingBox().inflate(10.0D, 128.0D, 10.0D);
        List<EntityIMLiving> entities = level.getEntitiesOfClass(EntityIMLiving.class, bounds);
        for (EntityIMLiving entity : entities) {
            entity.acquiredByNexus(this);
        }
        Invasion.log("Acquired " + entities.size() + " entities after state restore");
        return entities.size();
    }

    private long normalizeBindTime(long storedTime) {
        if (storedTime < 1_000_000_000_000L) {
            return System.currentTimeMillis();
        }
        return storedTime;
    }

    private void stopInvasion() {
        if (mode == 3) {
            mode = 2;
            setActive(true);
            if (level != null) {
                nextAttackTime = scheduleNextContinuousAttack(level.getDayTime());
            }
        } else {
            mode = 0;
            setActive(false);
        }
        waveRestTimer = 0;
        activationTimer = 0;
        queuedStartWave = 0;
        pendingActivationType = PENDING_NONE;
        waveElapsed = 0L;
        waveDelayTimer = -1L;
        continuousAttack = false;
        mobsLeftInWave = 0;
        lastMobsLeftInWave = 0;
        mobsToKillInWave = 0;
        zapTimer = 0;
        if (waveSpawner != null) {
            waveSpawner.stop();
        }
        Invasion.setActiveNexus(null);
        setChanged();
    }
}
