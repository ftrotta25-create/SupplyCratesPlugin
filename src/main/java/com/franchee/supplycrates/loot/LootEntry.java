package com.franchee.supplycrates.loot;

import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

/**
 * Una entrada de la tabla de loot.
 *
 * peso: mientras mas alto, mas probable (no hace falta que sumen 100,
 * son pesos relativos entre si).
 * generador: se llama recien cuando toca esta entrada, asi cada item
 * generado tiene su propia cantidad random si corresponde.
 */
public record LootEntry(String etiqueta, int peso, Supplier<ItemStack> generador) {
}
