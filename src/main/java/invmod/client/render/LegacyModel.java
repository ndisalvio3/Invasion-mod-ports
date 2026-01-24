package invmod.client.render;

import java.util.List;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

// Minimal placeholder for legacy models until they are fully ported.
public class LegacyModel<T extends EntityRenderState> extends EntityModel<T> {
    public LegacyModel() {
        super(new ModelPart(List.of(), Map.of()));
    }
}
