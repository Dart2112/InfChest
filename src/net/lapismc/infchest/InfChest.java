package net.lapismc.infchest;

import net.lapismc.lapiscore.LapisCorePlugin;
import net.lapismc.lapiscore.utils.CompatibleMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecation")
public class InfChest extends LapisCorePlugin implements Listener {

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
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);
            String worldC = split[3];
            int item = Integer.parseInt(split[4]);
            short meta = Short.parseShort(split[5]);
            World world = Bukkit.getWorld(worldC);
            Location location = new Location(world, x, y, z);
            byte b = 0;
            ItemStack i = new ItemStack(CompatibleMaterial.matchXMaterial(item, b).parseItem());
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
