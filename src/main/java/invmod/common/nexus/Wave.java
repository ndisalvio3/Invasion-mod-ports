package invmod.common.nexus;

import java.util.List;

public class Wave {
    private List<WaveEntry> entries;
    private int elapsed;
    private int waveTotalTime;
    private int waveBreakTime;

    public Wave(int waveTotalTime, int waveBreakTime, List<WaveEntry> entries) {
        this.entries = entries;
        this.waveTotalTime = waveTotalTime;
        this.waveBreakTime = waveBreakTime;
        this.elapsed = 0;
    }

    public void addEntry(WaveEntry entry) {
        this.entries.add(entry);
    }

    public int doNextSpawns(int elapsedMillis, ISpawnerAccess spawner) {
        int numberOfSpawns = 0;
        int previousElapsed = this.elapsed;
        this.elapsed += elapsedMillis;
        for (WaveEntry entry : this.entries) {
            int entryBegin = entry.getTimeBegin();
            int entryEnd = entry.getTimeEnd();
            if (this.elapsed <= entryBegin || previousElapsed >= entryEnd) {
                continue;
            }
            int effectiveStart = Math.max(previousElapsed, entryBegin);
            int effectiveEnd = Math.min(this.elapsed, entryEnd);
            int effectiveElapsed = effectiveEnd - effectiveStart;
            if (effectiveElapsed > 0) {
                numberOfSpawns += entry.doNextSpawns(effectiveElapsed, spawner);
            }
        }
        return numberOfSpawns;
    }

    public int getTimeInWave() {
        return this.elapsed;
    }

    public int getWaveTotalTime() {
        return this.waveTotalTime;
    }

    public int getWaveBreakTime() {
        return this.waveBreakTime;
    }

    public boolean isComplete() {
        return this.elapsed > this.waveTotalTime;
    }

    public void resetWave() {
        this.elapsed = 0;
        for (WaveEntry entry : this.entries) {
            entry.resetToBeginning();
        }
    }

    public void setWaveToTime(int millis) {
        this.elapsed = millis;
        for (WaveEntry entry : this.entries) {
            int entryElapsed = millis - entry.getTimeBegin();
            if (entryElapsed < 0) {
                entryElapsed = 0;
            }
            int entryDuration = entry.getTimeEnd() - entry.getTimeBegin();
            if (entryDuration >= 0 && entryElapsed > entryDuration) {
                entryElapsed = entryDuration;
            }
            entry.setToTime(entryElapsed);
        }
    }

    public int getTotalMobAmount() {
        int total = 0;
        for (WaveEntry entry : this.entries) {
            total += entry.getAmount();
        }
        return total;
    }
}
