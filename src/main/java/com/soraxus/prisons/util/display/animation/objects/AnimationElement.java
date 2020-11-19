package com.soraxus.prisons.util.display.animation.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnimationElement<T> {
    private T value;
    private long delay;
}
