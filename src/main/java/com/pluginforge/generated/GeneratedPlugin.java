package com.pluginforge.generated;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeneratedPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    private boolean enabled = true;
    private int speedMultiplier = 2;

    @Override
    public void onEnable() {
        getCommand("fastfurnace").setExecutor(this);
        getCommand("fastfurnace").setTabCompleter(this);

        // Task to accelerate furnace cooking
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled || speedMultiplier <= 1) return;

                // Iterate through all loaded chunks to find active furnaces
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        for (BlockState state : chunk.getTileEntities()) {
                            if (state instanceof Furnace furnace) {
                                // Only accelerate if it's actually cooking (burn time > 0 and has cook progress)
                                if (furnace.getBurnTime() > 0 && furnace.getCookTime() > 0) {
                                    // Add extra progress per tick. 
                                    // Default is 1 progress per tick. 
                                    // To get 'multiplier' speed, we add (multiplier - 1) extra progress.
                                    int currentProgress = furnace.getCookTime();
                                    int newProgress = currentProgress + (speedMultiplier - 1);
                                    
                                    // Ensure we don't exceed the total cook time required
                                    if (newProgress >= furnace.getCookTimeTotal()) {
                                        newProgress = furnace.getCookTimeTotal() - 1;
                                    }
                                    
                                    furnace.setCookTime((short) newProgress);
                                    furnace.update(false, false);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L);

        getLogger().info("FastFurnace enabled with multiplier: " + speedMultiplier);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /fastfurnace <on|off|number>");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("on")) {
            enabled = true;
            sender.sendMessage(ChatColor.GREEN + "FastFurnace is now ON (Speed: " + speedMultiplier + "x)");
        } else if (sub.equals("off")) {
            enabled = false;
            sender.sendMessage(ChatColor.RED + "FastFurnace is now OFF");
        } else {
            try {
                int val = Integer.parseInt(sub);
                if (val < 1 || val > 1000) {
                    sender.sendMessage(ChatColor.RED + "Please provide a number between 1 and 1000.");
                    return true;
                }
                speedMultiplier = val;
                enabled = true;
                sender.sendMessage(ChatColor.GREEN + "FastFurnace speed set to " + val + "x");
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid argument. Use 'on', 'off', or a number.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("on");
            completions.add("off");
            completions.add("2");
            completions.add("5");
            completions.add("10");
            return completions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return completions;
    }
}