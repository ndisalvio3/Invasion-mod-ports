package invmod.client.render;

import invmod.client.render.animation.Animation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class AnimationRegistry {
    private static final AnimationRegistry INSTANCE = new AnimationRegistry();
    private static final Map<String, Animation<?>> ANIMATIONS = new HashMap<>();

    private AnimationRegistry() {
    }

    public static AnimationRegistry instance() {
        return INSTANCE;
    }

    public static <T extends Enum<T>> void register(String name, Animation<T> animation) {
        if (name == null || animation == null) {
            return;
        }
        ANIMATIONS.put(name, animation);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> Animation<T> get(String name) {
        return (Animation<T>) ANIMATIONS.get(name);
    }

    public <T extends Enum<T>> Animation<T> getAnimation(String name) {
        return get(name);
    }

    public static boolean contains(String name) {
        return ANIMATIONS.containsKey(name);
    }

    public static Set<String> keys() {
        return Collections.unmodifiableSet(ANIMATIONS.keySet());
    }

    public static void clear() {
        ANIMATIONS.clear();
    }
}
