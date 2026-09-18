package com.franchee.supplycrates.loot;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LootTable {

    private final List<LootEntry> entradas = new ArrayList<>();
    private int pesoTotal = 0;

    public void agregar(LootEntry entrada) {
        entradas.add(entrada);
        pesoTotal += entrada.peso();
    }

    public boolean estaVacia() {
        return entradas.isEmpty();
    }

    /** Elige una entrada al azar respetando los pesos, y genera su item. */
    public ItemStack elegirUno() {
        if (entradas.isEmpty()) return null;

        int tiro = ThreadLocalRandom.current().nextInt(pesoTotal);
        int acumulado = 0;
        for (LootEntry entrada : entradas) {
            acumulado += entrada.peso();
            if (tiro < acumulado) {
                return entrada.generador().get();
            }
        }
        // no deberia pasar nunca, pero por las dudas devolvemos la ultima
        return entradas.get(entradas.size() - 1).generador().get();
    }

    public List<ItemStack> elegirVarios(int cantidad) {
        List<ItemStack> resultado = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            ItemStack item = elegirUno();
            if (item != null) resultado.add(item);
        }
        return resultado;
    }
}
