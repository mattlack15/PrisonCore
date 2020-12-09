package com.soraxus.prisons.config.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class Base {
    private String name;
    private String[] description;
}
