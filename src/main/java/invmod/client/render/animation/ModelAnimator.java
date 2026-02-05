package invmod.client.render.animation;

import invmod.client.render.LegacyModelPart;
import invmod.common.util.Triplet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

public class ModelAnimator<T extends Enum<T>> {
    private final List<Triplet<LegacyModelPart, Integer, List<KeyFrame>>> parts;
    private float animationPeriod;

    public ModelAnimator() {
        this(1.0F);
    }

    public ModelAnimator(float animationPeriod) {
        this.animationPeriod = animationPeriod;
        this.parts = new ArrayList<>(1);
    }

    public ModelAnimator(Map<T, LegacyModelPart> modelParts, Animation<T> animation) {
        this.animationPeriod = animation.getAnimationPeriod();
        this.parts = new ArrayList<>(animation.getSkeletonType().getEnumConstants().length);
        for (Map.Entry<T, LegacyModelPart> entry : modelParts.entrySet()) {
            List<KeyFrame> keyFrames = animation.getKeyFramesFor(entry.getKey());
            if (keyFrames != null) {
                this.parts.add(new Triplet<>(entry.getValue(), 0, keyFrames));
            }
        }
    }

    public void addPart(LegacyModelPart part, List<KeyFrame> keyFrames) {
        if (validate(keyFrames)) {
            this.parts.add(new Triplet<>(part, 0, keyFrames));
        }
    }

    public void clearParts() {
        this.parts.clear();
    }

    public void updateAnimation(float newTime) {
        for (Triplet<LegacyModelPart, Integer, List<KeyFrame>> entry : this.parts) {
            int prevIndex = entry.getVal2();
            List<KeyFrame> keyFrames = entry.getVal3();
            KeyFrame prevFrame = keyFrames.get(prevIndex++);
            KeyFrame nextFrame = null;

            if (prevFrame.getTime() <= newTime) {
                for (; prevIndex < keyFrames.size(); prevIndex++) {
                    KeyFrame keyFrame = keyFrames.get(prevIndex);
                    if (newTime < keyFrame.getTime()) {
                        nextFrame = keyFrame;
                        prevIndex--;
                        break;
                    }
                    prevFrame = keyFrame;
                }
                if (prevIndex >= keyFrames.size()) {
                    prevIndex = keyFrames.size() - 1;
                    nextFrame = keyFrames.get(0);
                }
            } else {
                for (prevIndex = 0; prevIndex < keyFrames.size(); prevIndex++) {
                    KeyFrame keyFrame = keyFrames.get(prevIndex);
                    if (newTime < keyFrame.getTime()) {
                        nextFrame = keyFrame;
                        prevIndex--;
                        prevFrame = keyFrames.get(prevIndex);
                        break;
                    }
                }
            }

            entry.setVal2(prevIndex);
            interpolate(prevFrame, nextFrame, newTime, entry.getVal1());
        }
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

    private void interpolate(KeyFrame prevFrame, KeyFrame nextFrame, float time, LegacyModelPart part) {
        if (nextFrame == null || prevFrame.getInterpType() != InterpType.LINEAR) {
            return;
        }

        float dtPrev = time - prevFrame.getTime();
        float dtFrame = nextFrame.getTime() - prevFrame.getTime();
        if (dtFrame < 0.0F) {
            dtFrame += this.animationPeriod;
        }
        float ratio = dtPrev / dtFrame;

        part.rotateAngleX = prevFrame.getRotX() + ratio * (nextFrame.getRotX() - prevFrame.getRotX());
        part.rotateAngleY = prevFrame.getRotY() + ratio * (nextFrame.getRotY() - prevFrame.getRotY());
        part.rotateAngleZ = prevFrame.getRotZ() + ratio * (nextFrame.getRotZ() - prevFrame.getRotZ());

        if (prevFrame.hasPos()) {
            if (nextFrame.hasPos()) {
                part.rotationPointX = prevFrame.getPosX() + ratio * (nextFrame.getPosX() - prevFrame.getPosX());
                part.rotationPointY = prevFrame.getPosY() + ratio * (nextFrame.getPosY() - prevFrame.getPosY());
                part.rotationPointZ = prevFrame.getPosZ() + ratio * (nextFrame.getPosZ() - prevFrame.getPosZ());
            } else {
                part.rotationPointX = prevFrame.getPosX();
                part.rotationPointY = prevFrame.getPosY();
                part.rotationPointZ = prevFrame.getPosZ();
            }
        }
    }

    private boolean validate(List<KeyFrame> keyFrames) {
        if (keyFrames == null || keyFrames.size() < 2) {
            return false;
        }
        if (keyFrames.get(0).getTime() != 0.0F) {
            return false;
        }

        float prevTime = keyFrames.get(0).getTime();
        for (int i = 1; i < keyFrames.size(); i++) {
            float time = keyFrames.get(i).getTime();
            if (time <= prevTime) {
                return false;
            }
            prevTime = time;
        }
        return true;
    }
}
