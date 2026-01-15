package invmod.common.nexus;

public class WaveBuilderNormal implements IWaveSource {
    private final IMWaveBuilder waveBuilder;
    private final float difficulty;
    private final float tierLevel;
    private final int lengthSeconds;

    public WaveBuilderNormal() {
        this(0.0F, 0.0F, 0);
    }

    public WaveBuilderNormal(float difficulty, float tierLevel, int lengthSeconds) {
        this.waveBuilder = new IMWaveBuilder();
        this.difficulty = difficulty;
        this.tierLevel = tierLevel;
        this.lengthSeconds = lengthSeconds;
    }

    public Wave getWave() {
        if (this.difficulty <= 0.0F || this.lengthSeconds <= 0) {
            return null;
        }
        return this.waveBuilder.generateWave(this.difficulty, this.tierLevel, this.lengthSeconds);
    }
}
