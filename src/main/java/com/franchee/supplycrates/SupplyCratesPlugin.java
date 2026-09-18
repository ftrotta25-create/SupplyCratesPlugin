package com.franchee.supplycrates;

import com.franchee.supplycrates.listeners.CrateListener;
import com.franchee.supplycrates.loot.LootTable;
import com.franchee.supplycrates.loot.LootTableFactory;
import com.franchee.supplycrates.util.CrateManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public class SupplyCratesPlugin extends JavaPlugin {

    private CrateManager crateManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        LootTable lootTable = LootTableFactory.construir(this);
        this.crateManager = new CrateManager(this, lootTable);

        getServer().getPluginManager().registerEvents(new CrateListener(crateManager), this);

        iniciarSpawnPeriodico();

        getLogger().info("SupplyCratesPlugin habilitado.");
    }

    @Override
    public void onDisable() {
        if (crateManager != null) crateManager.detener();
        getLogger().info("SupplyCratesPlugin deshabilitado.");
    }

    private void iniciarSpawnPeriodico() {
        long intervaloTicks = 20L * 60 * getConfig().getInt("intervaloMinutos", 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                intentarSpawnearTanda();
            }
        }.runTaskTimer(this, intervaloTicks, intervaloTicks);
    }

    private void intentarSpawnearTanda() {
        int maximo = getConfig().getInt("crateSimultaneasMax", 3);
        if (crateManager.cantidadActivas() >= maximo) {
            return; // ya hay suficientes afuera, esperamos a la proxima tanda
        }
        crateManager.spawnearUna();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("supplycrate")) return false;

        if (args.length < 1) {
            sender.sendMessage("Uso: /supplycrate <spawn|lista>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn" -> {
                Location ubicacion = crateManager.spawnearUna();
                if (ubicacion == null) {
                    sender.sendMessage("No se pudo encontrar un lugar válido para la crate (probá de nuevo).");
                } else {
                    sender.sendMessage("Crate spawneada en " + ubicacion.getBlockX() + ", "
                            + ubicacion.getBlockY() + ", " + ubicacion.getBlockZ());
                }
                return true;
            }
            case "lista" -> {
                sender.sendMessage("Crates activas: " + crateManager.cantidadActivas());
                return true;
            }
            default -> {
                sender.sendMessage("Uso: /supplycrate <spawn|lista>");
                return true;
            }
        }
    }
}
