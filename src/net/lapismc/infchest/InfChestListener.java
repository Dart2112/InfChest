package net.lapismc.infchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("deprecation")
public class InfChestListener implements Listener {

    private InfChest plugin;

    InfChestListener(InfChest plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() == Material.CHEST || e.getBlockPlaced().getType() == Material.TRAPPED_CHEST) {
            if (e.getItemInHand().hasItemMeta() &&
                    e.getItemInHand().getItemMeta().getDisplayName().startsWith("Inf ")) {
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
                plugin.chests.put(loc, i);
                e.getPlayer().sendMessage(ChatColor.GOLD + "This Chest Will Now Always Output "
                        + i.getType().name().replace("_", " ").toLowerCase() + "'s");
                Double x = loc.getX();
                Double y = loc.getY();
                Double z = loc.getZ();
                String worldC = loc.getWorld().getName();
                Integer item = i.getTypeId();
                short meta = i.getDurability();
                String string = x + ":" + y + ":" + z + ":" + worldC + ":" + item + ":" + meta;
                List<String> l = plugin.getConfig().getStringList("Chests");
                l.add(string);
                plugin.getConfig().set("Chests", l);
                plugin.saveConfig();
                plugin.loadChests();
            }
        }
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.chests.containsKey(e.getClickedBlock().getLocation())) {
            e.getPlayer().sendMessage(ChatColor.RED + "That Is An Infinite Chest And Cannot Be Accessed!");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        if (plugin.chests.containsKey(e.getBlock().getLocation())) {
            if (!e.getPlayer().hasPermission("InfChest.use")) {
                e.getPlayer().sendMessage("You Don't Have Permission To Do That!");
                e.setCancelled(true);
                return;
            }
            plugin.chests.remove(e.getBlock().getLocation());
            e.getPlayer().sendMessage(ChatColor.GOLD + "Infinite Chest Removed");
            Chest chest = (Chest) e.getBlock().getState();
            chest.getBlockInventory().clear();
            Location loc = e.getBlock().getLocation();
            ItemStack i = plugin.chests.get(loc);
            Double x = loc.getX();
            Double y = loc.getY();
            Double z = loc.getZ();
            String worldC = loc.getWorld().getName();
            Integer item = i.getTypeId();
            short meta = i.getDurability();
            String string = x + ":" + y + ":" + z + ":" + worldC + ":" + item + ":" + meta;
            List<String> l = plugin.getConfig().getStringList("Chests");
            l.remove(string);
            plugin.getConfig().set("Chests", l);
            plugin.saveConfig();
            plugin.loadChests();
        }
    }

}
