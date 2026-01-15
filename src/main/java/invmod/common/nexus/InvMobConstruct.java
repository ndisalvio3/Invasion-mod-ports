package invmod.common.nexus;

public class InvMobConstruct {
    private IMEntityType entityType;
    private int texture;
    private int tier;
    private int flavour;
    private float scaling;

    public InvMobConstruct(IMEntityType entityType, int texture, int tier, int flavour, float scaling) {
        this.entityType = entityType;
        this.texture = texture;
        this.tier = tier;
        this.flavour = flavour;
        this.scaling = scaling;
    }

    public InvMobConstruct(int texture, int tier, int flavour, float scaling) {
        this(null, texture, tier, flavour, scaling);
    }

    public IMEntityType getMobType() {
        return this.entityType;
    }

    public void setMobType(IMEntityType entityType) {
        this.entityType = entityType;
    }

    public int getTexture() {
        return this.texture;
    }

    public int getTier() {
        return this.tier;
    }

    public int getFlavour() {
        return this.flavour;
    }

    public float getScaling() {
        return this.scaling;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public void setFlavour(int flavour) {
        this.flavour = flavour;
    }

    public void setScaling(float scaling) {
        this.scaling = scaling;
    }
}
