package com.franchee.supplycrates.util;

import com.franchee.supplycrates.loot.LootTable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CrateManager {

    private final Plugin plugin;
    private final LootTable lootTable;

    private final Map<Location, BukkitTask> cratesActivas = new HashMap<>();
    private BukkitTask tareaParticulas;

    public CrateManager(Plugin plugin, LootTable lootTable) {
        this.plugin = plugin;
        this.lootTable = lootTable;
        iniciarParticulas();
    }

    public int cantidadActivas() {
        return cratesActivas.size();
    }

    /** Intenta spawnear una crate nueva. Devuelve la ubicacion si funciono, o null si no encontro lugar. */
    public Location spawnearUna() {
        String nombreMundo = plugin.getConfig().getString("mundo", "world");
        World mundo = plugin.getServer().getWorld(nombreMundo);
        if (mundo == null) {
            plugin.getLogger().warning("El mundo '" + nombreMundo + "' no existe - no se puede spawnear la crate.");
            return null;
        }

        int centroX = plugin.getConfig().getInt("centroX", 0);
        int centroZ = plugin.getConfig().getInt("centroZ", 0);
        int radio = plugin.getConfig().getInt("radio", 500);

        Location ubicacion = null;
        for (int intento = 0; intento < 20; intento++) {
            int x = centroX + ThreadLocalRandom.current().nextInt(-radio, radio + 1);
            int z = centroZ + ThreadLocalRandom.current().nextInt(-radio, radio + 1);
            int y = mundo.getHighestBlockYAt(x, z);
            Block bloqueSuelo = mundo.getBlockAt(x, y, z);

            if (bloqueSuelo.getType() == Material.WATER || bloqueSuelo.getType() == Material.LAVA) {
                continue; // reintenta con otro punto
            }

            ubicacion = new Location(mundo, x + 0.5, y + 1, z + 0.5);
            break;
        }

        if (ubicacion == null) return null; // mala suerte, probamos la proxima tanda

        colocarCrate(ubicacion);
        return ubicacion;
    }

    private void colocarCrate(Location ubicacion) {
        Block bloque = ubicacion.getBlock();
        bloque.setType(Material.CHEST);

        int cantidadItems = plugin.getConfig().getInt("itemsPorCrate", 4);
        if (bloque.getState() instanceof Chest cofre) {
            for (ItemStack item : lootTable.elegirVarios(cantidadItems)) {
                cofre.getInventory().addItem(item);
            }
        }

        ubicacion.getWorld().playSound(ubicacion, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.6f);
        ubicacion.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, ubicacion, 10);

        int minutosExpiracion = plugin.getConfig().getInt("tiempoExpiracionMinutos", 20);
        BukkitTask tareaExpiracion = new BukkitRunnable() {
            @Override
            public void run() {
                expirarCrate(ubicacion);
            }
        }.runTaskLater(plugin, 20L * 60 * minutosExpiracion);

        cratesActivas.put(ubicacion, tareaExpiracion);

        plugin.getServer().broadcast(Component.text(
                "Una Supply Crate cayó cerca de (" + ubicacion.getBlockX() + ", " + ubicacion.getBlockZ() + ")!",
                NamedTextColor.GOLD));
    }

    private void expirarCrate(Location ubicacion) {
        Block bloque = ubicacion.getBlock();
        if (bloque.getType() == Material.CHEST && bloque.getState() instanceof Chest cofre) {
            for (ItemStack item : cofre.getInventory().getContents()) {
                if (item != null) ubicacion.getWorld().dropItemNaturally(ubicacion, item);
            }
            bloque.setType(Material.AIR);
        }
        cratesActivas.remove(ubicacion);
    }

    /** Llamar cuando un jugador vacia y cierra la crate, para liberar el cupo antes de que expire sola. */
    public void marcarComoRecolectada(Location ubicacion) {
        BukkitTask tarea = cratesActivas.remove(ubicacion);
        if (tarea != null) tarea.cancel();
    }

    public boolean esUbicacionDeCrate(Location ubicacion) {
        return cratesActivas.containsKey(ubicacion);
    }

    private void iniciarParticulas() {
        tareaParticulas = new BukkitRunnable() {
            @Override
            public void run() {
                for (Location ubicacion : cratesActivas.keySet()) {
                    if (ubicacion.getWorld() == null) continue;
                    ubicacion.getWorld().spawnParticle(Particle.END_ROD,
                            ubicacion.clone().add(0, 1, 0), 3, 0.2, 1.5, 0.2, 0.01);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // cada 1s
    }

    public void detener() {
        if (tareaParticulas != null) tareaParticulas.cancel();
        for (BukkitTask tarea : cratesActivas.values()) {
            tarea.cancel();
        }
    }
}
