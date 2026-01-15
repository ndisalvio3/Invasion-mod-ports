package invmod.common.nexus;

public class WaveContainer {
    private final java.util.List<Wave> waves = new java.util.ArrayList<>();
    private int nextWaveIndex = 0;

    public void addWave(Wave wave) {
        if (wave != null) {
            this.waves.add(wave);
        }
    }

    public Wave getNextWave() {
        if (this.waves.isEmpty()) {
            return null;
        }
        if (this.nextWaveIndex >= this.waves.size()) {
            this.nextWaveIndex = this.waves.size() - 1;
        }
        return this.waves.get(this.nextWaveIndex++);
    }

    public void reset() {
        this.nextWaveIndex = 0;
    }
}
