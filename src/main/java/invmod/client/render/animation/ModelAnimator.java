package invmod.client.render.animation;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;

public final class ModelAnimator {
    private ModelAnimator() {
    }

    public static KeyFrame interpolate(List<KeyFrame> keyFrames, float time) {
        if (keyFrames == null || keyFrames.isEmpty()) {
            return null;
        }

        KeyFrame prev = keyFrames.get(0);
        if (time <= prev.getTime()) {
            return prev;
        }

        KeyFrame next = prev;
        for (int i = 1; i < keyFrames.size(); i++) {
            next = keyFrames.get(i);
            if (time <= next.getTime()) {
                break;
            }
            prev = next;
        }

        if (prev == next || next.getTime() == prev.getTime()) {
            return prev;
        }

        float span = next.getTime() - prev.getTime();
        float t = (time - prev.getTime()) / span;

        float rotX = lerp(prev.getRotX(), next.getRotX(), t);
        float rotY = lerp(prev.getRotY(), next.getRotY(), t);
        float rotZ = lerp(prev.getRotZ(), next.getRotZ(), t);

        if (prev.hasPos() || next.hasPos()) {
            float posX = lerp(prev.getPosX(), next.getPosX(), t);
            float posY = lerp(prev.getPosY(), next.getPosY(), t);
            float posZ = lerp(prev.getPosZ(), next.getPosZ(), t);
            return new KeyFrame(time, rotX, rotY, rotZ, posX, posY, posZ, InterpType.LINEAR);
        }

        return new KeyFrame(time, rotX, rotY, rotZ, InterpType.LINEAR);
    }

    public static void apply(ModelPart part, List<KeyFrame> keyFrames, float time) {
        KeyFrame frame = interpolate(keyFrames, time);
        if (frame == null || part == null) {
            return;
        }
        apply(part, frame);
    }

    public static void apply(ModelPart part, KeyFrame frame) {
        if (part == null || frame == null) {
            return;
        }
        part.xRot = frame.getRotX();
        part.yRot = frame.getRotY();
        part.zRot = frame.getRotZ();
        if (frame.hasPos()) {
            part.x = frame.getPosX();
            part.y = frame.getPosY();
            part.z = frame.getPosZ();
        }
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}
