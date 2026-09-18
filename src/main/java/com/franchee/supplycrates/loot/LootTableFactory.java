package com.franchee.supplycrates.loot;

import com.franchee.matesystem.MateSystemPlugin;
import com.franchee.matesystem.items.ItemFactory;
import com.franchee.matesystem.items.TipoHierba;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Arma la tabla de loot de las Supply Crates. Pensada para el arranque
 * del server: nada de netherite, nada de diamante encantado, nada que
 * rompa la progresion temprana.
 *
 * Pesos (mientras mas alto, mas comun):
 *  - Comunes:     peso 30-40 (recursos basicos, comida, herramientas de hierro)
 *  - Poco comunes: peso 10-15 (oro, esmeralda, hierbas, mate vacio)
 *  - Raras:       peso 2-5   (diamante, manzana dorada, la Pava ya craftada)
 */
public class LootTableFactory {

    public static LootTable construir(Plugin plugin) {
        LootTable tabla = new LootTable();
        Logger log = plugin.getLogger();

        agregarComunes(tabla);
        agregarPocoComunes(tabla);
        agregarRaras(tabla);
        agregarDeMateSystemPluginSiEstaInstalado(tabla, log);

        return tabla;
    }

    private static int entre(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    // ---------------- COMUNES ----------------
    private static void agregarComunes(LootTable tabla) {
        tabla.agregar(new LootEntry("pan", 35, () -> new ItemStack(Material.BREAD, entre(3, 6))));
        tabla.agregar(new LootEntry("manzana", 30, () -> new ItemStack(Material.APPLE, entre(2, 4))));
        tabla.agregar(new LootEntry("hierro_en_bruto", 30, () -> new ItemStack(Material.RAW_IRON, entre(2, 4))));
        tabla.agregar(new LootEntry("madera", 25, () -> new ItemStack(Material.OAK_LOG, entre(4, 8))));
        tabla.agregar(new LootEntry("flechas", 25, () -> new ItemStack(Material.ARROW, entre(8, 16))));
        tabla.agregar(new LootEntry("antorchas", 25, () -> new ItemStack(Material.TORCH, entre(6, 12))));
        tabla.agregar(new LootEntry("carbon", 25, () -> new ItemStack(Material.COAL, entre(3, 6))));
        tabla.agregar(new LootEntry("cuerda", 20, () -> new ItemStack(Material.STRING, entre(3, 6))));
    }

    // ---------------- POCO COMUNES ----------------
    private static void agregarPocoComunes(LootTable tabla) {
        tabla.agregar(new LootEntry("lingote_oro", 12, () -> new ItemStack(Material.GOLD_INGOT, entre(1, 3))));
        tabla.agregar(new LootEntry("esmeralda", 10, () -> new ItemStack(Material.EMERALD, entre(1, 2))));
        tabla.agregar(new LootEntry("zanahoria_dorada", 10, () -> new ItemStack(Material.GOLDEN_CARROT, entre(1, 2))));
        tabla.agregar(new LootEntry("herramienta_hierro", 10, LootTableFactory::herramientaDeHierroRandom));
    }

    private static ItemStack herramientaDeHierroRandom() {
        Material[] opciones = {
                Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SWORD, Material.IRON_SHOVEL
        };
        return new ItemStack(opciones[ThreadLocalRandom.current().nextInt(opciones.length)]);
    }

    // ---------------- RARAS ----------------
    private static void agregarRaras(LootTable tabla) {
        tabla.agregar(new LootEntry("diamante", 4, () -> new ItemStack(Material.DIAMOND, entre(1, 2))));
        tabla.agregar(new LootEntry("manzana_dorada", 3, () -> new ItemStack(Material.GOLDEN_APPLE, 1)));
    }

    // ---------------- INTEGRACION CON MATESYSTEMPLUGIN ----------------
    private static void agregarDeMateSystemPluginSiEstaInstalado(LootTable tabla, Logger log) {
        Plugin mateSystemPlugin = Bukkit.getPluginManager().getPlugin("MateSystemPlugin");
        if (!(mateSystemPlugin instanceof MateSystemPlugin) || !mateSystemPlugin.isEnabled()) {
            log.info("MateSystemPlugin no esta instalado - las Supply Crates no van a tener Pava/Mate/Hierbas.");
            return;
        }

        ItemFactory itemFactory = ((MateSystemPlugin) mateSystemPlugin).getItemFactory();
        if (itemFactory == null) {
            log.warning("MateSystemPlugin esta instalado pero su ItemFactory es null - raro, revisar version.");
            return;
        }

        // Poco comunes: una hierba random, o un mate vacio
        tabla.agregar(new LootEntry("hierba_random", 12, () ->
                itemFactory.crearHierba(hierbaRandom())));
        tabla.agregar(new LootEntry("mate_vacio", 8, itemFactory::crearMateVacio));

        // Rara: la Pava ya craftada entera - buen premio gordo, pero no rompe nada
        // (sigue habiendo que conseguir hierro/blaze para las demas si la pierden).
        tabla.agregar(new LootEntry("pava_hierro", 3, itemFactory::crearPava));

        log.info("MateSystemPlugin detectado - Supply Crates van a incluir Pava/Mate/Hierbas.");
    }

    private static TipoHierba hierbaRandom() {
        TipoHierba[] opciones = { TipoHierba.CEDRON, TipoHierba.POLEO, TipoHierba.BOLDO, TipoHierba.AZUCAR };
        return opciones[ThreadLocalRandom.current().nextInt(opciones.length)];
    }
}
