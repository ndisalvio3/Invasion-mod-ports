package invmod.common.nexus;

import invmod.common.util.FiniteSelectionPool;
import invmod.common.util.ISelect;
import invmod.common.util.RandomSelectionPool;

import java.util.ArrayList;
import java.util.Random;

public class WaveBuilderSpiders implements IWaveSource {
    private static final float SPIDER_T1_WEIGHT = 1.0F;
    private static final float SPIDER_T2_WEIGHT = 2.0F;

    private final Random rand;
    private final float difficulty;
    private final float tierLevel;
    private final int lengthSeconds;

    public WaveBuilderSpiders() {
        this(0.0F, 0.0F, 0);
    }

    public WaveBuilderSpiders(float difficulty, float tierLevel, int lengthSeconds) {
        this.rand = new Random();
        this.difficulty = difficulty;
        this.tierLevel = tierLevel;
        this.lengthSeconds = lengthSeconds;
    }

    public Wave getWave() {
        if (this.difficulty <= 0.0F || this.lengthSeconds <= 0) {
            return null;
        }
        float basicMobsPerSecond = 0.12F * this.difficulty;
        int numberOfGroups = 7;
        int numberOfBigGroups = 1;
        float proportionInGroups = 0.5F;
        int mobsPerGroup = Math.round(proportionInGroups * basicMobsPerSecond * this.lengthSeconds / (numberOfGroups + numberOfBigGroups * 2));
        int mobsPerBigGroup = mobsPerGroup * 2;
        int remainingMobs = (int) (basicMobsPerSecond * this.lengthSeconds) - mobsPerGroup * numberOfGroups - mobsPerBigGroup * numberOfBigGroups;
        int mobsPerSteady = Math.round(0.7F * remainingMobs / numberOfGroups);
        int extraMobsForFinale = Math.round(0.3F * remainingMobs);
        int extraMobsForCleanup = (int) (basicMobsPerSecond * this.lengthSeconds * 0.2F);
        float timeForGroups = 0.5F;
        int groupTimeInterval = (int) (this.lengthSeconds * 1000 * timeForGroups / (numberOfGroups + numberOfBigGroups * 3));
        int steadyTimeInterval = (int) (this.lengthSeconds * 1000 * (1.0F - timeForGroups) / numberOfGroups);

        int time = 0;
        ArrayList entryList = new ArrayList();
        for (int i = 0; i < numberOfGroups; i++) {
            if (this.rand.nextInt(2) == 0) {
                entryList.add(new WaveEntry(time, time + 3500, mobsPerGroup, 500, generateGroupPool(this.tierLevel), 25, 3));
                entryList.add(new WaveEntry(time += groupTimeInterval, time += steadyTimeInterval, mobsPerSteady, 2000, generateSteadyPool(this.tierLevel), 160, 5));
            } else {
                entryList.add(new WaveEntry(time, time += steadyTimeInterval, mobsPerSteady, 2000, generateSteadyPool(this.tierLevel), 160, 5));
                entryList.add(new WaveEntry(time, time + 5000, mobsPerGroup, 500, generateGroupPool(this.tierLevel), 25, 3));
                time += groupTimeInterval;
            }
        }

        time = (int) (time + groupTimeInterval * 0.75D);
        FiniteSelectionPool finaleGroup = new FiniteSelectionPool();
        generateGroupPool(this.tierLevel + 0.5F, finaleGroup, mobsPerBigGroup);
        WaveEntry finale = new WaveEntry(time, time + 8000, mobsPerBigGroup + mobsPerBigGroup / 7, 500, finaleGroup, 45, 3);
        finale.addAlert("A large number of mobs are slipping through the nexus rift!", 0);

        entryList.add(finale);
        entryList.add(new WaveEntry(time + 5000, (int) (time + groupTimeInterval * 2.25F), extraMobsForFinale / 2, 500, generateSteadyPool(this.tierLevel), 160, 5));
        entryList.add(new WaveEntry(time + 5000, (int) (time + groupTimeInterval * 2.25F), extraMobsForFinale / 2, 500, generateSteadyPool(this.tierLevel), 160, 5));
        entryList.add(new WaveEntry(time + 5000, (int) (time + groupTimeInterval * 2.25F), extraMobsForFinale / 2, 500, generateSteadyPool(this.tierLevel), 160, 5));
        entryList.add(new WaveEntry(time + 15000, (int) (time + 10000 + groupTimeInterval * 2.25F), extraMobsForCleanup, 500, generateSteadyPool(this.tierLevel)));
        time = (int) (time + groupTimeInterval * 2.25D);

        return new Wave(time + 16000, groupTimeInterval * 3, entryList);
    }

    private ISelect<IEntityIMPattern> generateGroupPool(float tierLevel) {
        RandomSelectionPool newPool = new RandomSelectionPool();
        generateGroupPool(tierLevel, newPool, 6.0F);
        return newPool;
    }

    private void generateGroupPool(float tierLevel, FiniteSelectionPool<IEntityIMPattern> startPool, int amount) {
        RandomSelectionPool newPool = new RandomSelectionPool();
        generateGroupPool(tierLevel, newPool, 6.0F);
        startPool.addEntry(newPool, amount);
    }

    private void generateGroupPool(float tierLevel, RandomSelectionPool<IEntityIMPattern> startPool, float weight) {
        float[] weights = buildTierWeights(tierLevel);
        startPool.addEntry(IMWaveBuilder.getPattern("spider_t1_any"), SPIDER_T1_WEIGHT * weights[0] * weight);
        startPool.addEntry(IMWaveBuilder.getPattern("spider_t2_any"), SPIDER_T2_WEIGHT * weights[2] * weight);
    }

    private ISelect<IEntityIMPattern> generateSteadyPool(float tierLevel) {
        float[] weights = buildTierWeights(tierLevel);
        RandomSelectionPool pool = new RandomSelectionPool();
        pool.addEntry(IMWaveBuilder.getPattern("spider_t1_any"), SPIDER_T1_WEIGHT * weights[0]);
        pool.addEntry(IMWaveBuilder.getPattern("spider_t2_any"), SPIDER_T2_WEIGHT * weights[2]);
        return pool;
    }

    private float[] buildTierWeights(float tierLevel) {
        float[] weights = new float[6];
        for (int i = 0; i < 6; i++) {
            if (tierLevel - i * 0.5F > 0.0F) {
                weights[i] = (tierLevel - i <= 1.0F ? tierLevel - i * 0.5F : 1.0F);
            }
        }
        return weights;
    }
}
