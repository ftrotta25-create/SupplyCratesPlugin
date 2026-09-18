package com.franchee.supplycrates.listeners;

import com.franchee.supplycrates.util.CrateManager;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class CrateListener implements Listener {

    private final CrateManager crateManager;

    public CrateListener(CrateManager crateManager) {
        this.crateManager = crateManager;
    }

    @EventHandler
    public void onCerrarInventario(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof Chest cofre)) return;

        Location ubicacion = cofre.getLocation();
        if (!crateManager.esUbicacionDeCrate(ubicacion)) return;

        boolean estaVacio = true;
        for (ItemStack item : e.getInventory().getContents()) {
            if (item != null) {
                estaVacio = false;
                break;
            }
        }

        if (estaVacio) {
            crateManager.marcarComoRecolectada(ubicacion);
            ubicacion.getBlock().setType(org.bukkit.Material.AIR);
        }
    }
}
