package net.lapismc.inf;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;

public class chest extends JavaPlugin implements Listener {
    public HashMap<Location, ItemStack> chests = new HashMap<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadChests();
        Bukkit.getPluginManager().registerEvents(this, this);
        this.startRunning();
    }

    public void loadChests() {
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
            this.chests.put(location, i);
        }
    }

    public void startRunning() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public synchronized void run() {
                for (Location loc : chest.this.chests.keySet()) {
                    ItemStack i = chest.this.chests.get(loc);
                    if (loc.getChunk().isLoaded()) {
                        Chest chest = (Chest) loc.getBlock().getState();
                        if (!chest.getBlockInventory().contains(i, 2)) {
                            i.setAmount(64);
                            chest.getBlockInventory().addItem(i);
                        }
                    }
                }
            }
        }, 20, 20);
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() == Material.CHEST || e.getBlockPlaced().getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) e.getBlockPlaced().getState();
            if (e.getItemInHand().getItemMeta().getDisplayName().startsWith("Inf ")) {
                if (!e.getPlayer().hasPermission("InfChest.use")) {
                    return;
                }
                String s1 = e.getItemInHand().getItemMeta().getDisplayName();
                String s = s1.replace("Inf ", "");
                ItemStack i;
                if (s.contains(":")) {
                    String[] split = s.split(":");
                    i = new ItemStack(Material.getMaterial(Integer.parseInt(split[0])));
                    i.setDurability(Short.parseShort(split[1]));
                } else {
                    i = new ItemStack(Material.getMaterial(Integer.parseInt(s)));
                }
                Location loc = e.getBlockPlaced().getLocation();
                this.chests.put(loc, i);
                e.getPlayer().sendMessage(ChatColor.GOLD + "This Chest Will Now Always Output "
                        + i.getType().name().replace("_", " ").toLowerCase() + "'s");
                Double x = loc.getX();
                Double y = loc.getY();
                Double z = loc.getZ();
                String worldC = loc.getWorld().getName();
                Integer item = i.getTypeId();
                short meta = i.getDurability();
                String string = x + ":" + y + ":" + z + ":" + worldC + ":" + item + ":" + meta;
                List<String> l = this.getConfig().getStringList("Chests");
                l.add(string);
                this.getConfig().set("Chests", l);
                this.saveConfig();
                this.loadChests();
            }
        }
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && this.chests.containsKey(e.getClickedBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.RED + "That Is An Infinite Chest And Cannot Be Accessed!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        if (this.chests.containsKey(e.getBlock().getLocation())) {
            if (!e.getPlayer().hasPermission("InfChest.use")) {
                e.getPlayer().sendMessage("You Don't Have Permission To Do That!");
                e.setCancelled(true);
                return;
            }
            this.chests.remove(e.getBlock().getLocation());
            e.getPlayer().sendMessage(ChatColor.GOLD + "Infinite Chest Removed");
            Chest chest = (Chest) e.getBlock().getState();
            chest.getBlockInventory().clear();
            Location loc = e.getBlock().getLocation();
            ItemStack i = this.chests.get(loc);
            Double x = loc.getX();
            Double y = loc.getY();
            Double z = loc.getZ();
            String worldC = loc.getWorld().getName();
            Integer item = i.getTypeId();
            short meta = i.getDurability();
            String string = x + ":" + y + ":" + z + ":" + worldC + ":" + item + ":" + meta;
            List<String> l = this.getConfig().getStringList("Chests");
            l.remove(string);
            this.getConfig().set("Chests", l);
            this.saveConfig();
            this.loadChests();
        }
    }

}
