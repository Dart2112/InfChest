package net.lapismc.infchest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class InfChest extends JavaPlugin implements Listener {

    HashMap<Location, ItemStack> chests = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadChests();
        startRunning();
        new InfChestListener(this);
    }

    void loadChests() {
        this.reloadConfig();
        List<String> list = this.getConfig().getStringList("Chests");
        for (String s : list) {
            String[] split = s.split(":");
            Double x = Double.parseDouble(split[0]);
            Double y = Double.parseDouble(split[1]);
            Double z = Double.parseDouble(split[2]);
            String worldC = split[3];
            Integer item = Integer.parseInt(split[4]);
            short meta = Short.parseShort(split[5]);
            World world = Bukkit.getWorld(worldC);
            Location location = new Location(world, x, y, z);
            ItemStack i = new ItemStack(Material.getMaterial(item));
            i.setDurability(meta);
            chests.put(location, i);
        }
    }

    private void startRunning() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public synchronized void run() {
                for (Location loc : chests.keySet()) {
                    ItemStack i = chests.get(loc);
                    if (loc.getChunk().isLoaded()) {
                        Chest chest = (Chest) loc.getBlock().getState();
                        if (!chest.getBlockInventory().contains(i, 2)) {
                            i.setAmount(i.getMaxStackSize());
                            chest.getBlockInventory().addItem(i);
                        }
                    }
                }
            }
        }, 20, 20);
    }


}
