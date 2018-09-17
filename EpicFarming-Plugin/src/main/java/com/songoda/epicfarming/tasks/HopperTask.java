package com.songoda.epicfarming.tasks;

import com.songoda.epicfarming.EpicFarmingPlugin;
import com.songoda.epicfarming.api.farming.Farm;
import com.songoda.epicfarming.api.farming.FarmManager;
import com.songoda.epicfarming.farming.Crop;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HopperTask extends BukkitRunnable {

    private static HopperTask instance;
    private final FarmManager manager;
    
    private HopperTask(EpicFarmingPlugin plugin) {
        this.manager = plugin.getFarmManager();
    }


    public static HopperTask startTask(EpicFarmingPlugin plugin) {
        if (instance == null) {
            instance = new HopperTask(plugin);
            instance.runTaskTimer(plugin, 0, 8);
        }

        return instance;
    }

    @Override
    public void run() {
        for (Farm farm : manager.getFarms().values()) {
            if (farm.getLocation() == null || farm.getLocation().getBlock() == null) {
                manager.removeFarm(farm.getLocation());
                continue;
            }
            Block block = farm.getLocation().getBlock();

            if (block.getRelative(BlockFace.DOWN).getType() != Material.HOPPER)
                return;

            Inventory inventory = farm.getInventory();
            Inventory hopperInventory = ((Hopper) block.getRelative(BlockFace.DOWN).getState()).getInventory();

            for (int i = 27; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) continue;

                int amtToMove = 1;

                ItemStack item = inventory.getItem(i);

                ItemStack toMove = item.clone();
                toMove.setAmount(amtToMove);

                int newAmt = item.getAmount() - amtToMove;

                if (canHop(hopperInventory, toMove)) {
                    if (newAmt <= 0)
                        inventory.setItem(i, null);
                    else
                        item.setAmount(newAmt);
                    hopperInventory.addItem(toMove);
                }
                break;
            }
        }
    }

    private boolean canHop(Inventory i, ItemStack item) {
        if (i.firstEmpty() != -1) return true;
        for (ItemStack it : i.getContents()) {
            if (it == null || it.isSimilar(item) && (it.getAmount() + item.getAmount()) <= it.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }
}