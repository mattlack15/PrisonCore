package com.soraxus.prisons.util.display.animation.objects;

import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Animation<T> {
    private List<AnimationElement<T>> elements;
    private int index = 0;
    private long lastUpdate = System.currentTimeMillis();
    private long lastDiff = 0;
    @Setter
    private boolean loop = false;

    public Animation(AnimationElement<T>... els) {
        elements = Arrays.asList(els);
    }

    public Animation(long delay, T... els) {
        elements = new ArrayList<>();
        for (T t : els) {
            elements.add(new AnimationElement<>(t, delay));
        }
    }

    public Animation(long delay, T value, DeltaFunction<T> delta, int count) {
        elements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            elements.add(new AnimationElement<>(value, delay));
            value = delta.transform(value);
        }
    }

    public T update() {
        return update(System.currentTimeMillis(), lastDiff);
    }

    private T update(long curr, long cd) {
        long diff = curr - lastUpdate + cd;
        lastUpdate = curr;
        for (; index < elements.size(); index++) {
            AnimationElement<T> el = elements.get(index);
            if (diff < el.getDelay()) {
                lastDiff = diff;
                return el.getValue();
            }
            diff -= el.getDelay();
        }
        if (loop) {
            index = 0;
            return update(curr, diff);
        }
        return null;
    }
}
