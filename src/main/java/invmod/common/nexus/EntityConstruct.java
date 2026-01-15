package invmod.common.nexus;

public class EntityConstruct extends InvMobConstruct {
    private int minAngle;
    private int maxAngle;

    public EntityConstruct(IMEntityType mobType, int tier, int texture, int flavour, float scaling, int minAngle, int maxAngle) {
        super(mobType, texture, tier, flavour, scaling);
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
    }

    public int getMinAngle() {
        return this.minAngle;
    }

    public int getMaxAngle() {
        return this.maxAngle;
    }
}
