package com.soraxus.prisons.enchants.api.enchant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class EnchantInfo {
    private Map<AbstractCE, Integer> enchants;
}
