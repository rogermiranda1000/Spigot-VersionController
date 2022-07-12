package com.rogermiranda1000.helper;

import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionChecker;
import com.rogermiranda1000.versioncontroller.VersionController;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class RogerPlugin extends JavaPlugin implements CommandExecutor {
    public final String clearPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[" + this.getName() + "] " + ChatColor.GREEN,
            errorPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[" + this.getName() + "] " + ChatColor.RED;

    private final Listener []listeners;
    private final CustomCommand []commands;
    private final CustomBlock []customBlocks;

    /**
     * JavaPlugin with some basic functionalities.
     * /!\\ While overriding onEnable remember to call the super() function /!\\
     * @param commands      All the commands from the plugin. It's important that in the first position you set the 'help' command
     * @param customBlocks  Plugin's custom blocks
     * @param listeners     All the event listeners from the plugin
     */
    public RogerPlugin(CustomCommand []commands, CustomBlock []customBlocks, Listener... listeners) {
        this.commands = commands;
        this.customBlocks = customBlocks;
        this.listeners = listeners; // Listener... is the same than Listener[]
    }

    public RogerPlugin(CustomCommand []commands, Listener... listeners) {
        this(commands, new CustomBlock[]{}, listeners);
    }

    public void printConsoleErrorMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] " + msg);
    }

    public void printConsoleWarningMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[" + this.getName() + "] " + msg);
    }

    public CustomCommand []getCommands() {
        return this.commands;
    }

    /**
     * Get the spigot ID to check for updates
     * @return Spigot ID, or NULL if don't want to check updates
     */
    @Nullable
    abstract public String getPluginID();

    @Override
    public void onEnable() {
        // TODO any way to save the instance here?

        Bukkit.getScheduler().runTaskAsynchronously(this,()->{
            try {
                String id = this.getPluginID();
                if (id != null) {
                    String version = VersionChecker.getVersion(id);
                    if (VersionChecker.isLower(this.getDescription().getVersion(), version)) this.printConsoleWarningMessage("v" + version + " is now available! You should consider updating the plugin.");
                }
            } catch (IOException e) {
                this.printConsoleWarningMessage("Can't check for updates.");
            }
        });

        // register the events
        PluginManager pm = getServer().getPluginManager();
        for (Listener lis : this.listeners) pm.registerEvents(lis, this); // TODO ignore some events depending on the version
        for (CustomBlock cb : this.customBlocks) pm.registerEvents(cb, this);

        if (this.commands.length > 0 && VersionController.version.compareTo(Version.MC_1_10) >= 0) {
            // if MC > 10 we can send hints onTab
            String commandBase = this.getName().toLowerCase();
            getCommand(commandBase).setTabCompleter(new HintEvent(this));
        }

        // call enable functions
        for (CustomBlock cb : this.customBlocks) cb.load();
    }

    @Override
    public void onDisable() {
        // call disable functions
        for (CustomBlock cb : this.customBlocks) cb.save();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        for (CustomCommand command : this.commands) {
            switch (command.search((sender instanceof Player) ? (Player) sender : null, cmd.getName(), args)) {
                case NO_MATCH:
                    continue;

                case NO_PERMISSIONS:
                    sender.sendMessage(this.errorPrefix + "You don't have the permissions to do that.");
                    break;
                case MATCH:
                    command.notifier.onCommand(sender, args);
                    break;
                case NO_PLAYER:
                    sender.sendMessage("Don't use this command in console.");
                    break;
                case INVALID_LENGTH:
                    sender.sendMessage(this.errorPrefix +"Unknown command. Use " + ChatColor.GOLD + "/mineit ?");
                    break;
                default:
                    this.printConsoleErrorMessage("Unknown response to command");
                    return false;
            }
            return true;
        }

        sender.sendMessage(this.errorPrefix +"Unknown command");
        this.commands[0].notifier.onCommand(sender, new String[]{}); // '?' command
        return true;
    }
}
