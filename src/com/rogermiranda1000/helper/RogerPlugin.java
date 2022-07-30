package com.rogermiranda1000.helper;

import com.rogermiranda1000.helper.blocks.CustomBlock;
import com.rogermiranda1000.helper.metrics.Metrics;
import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionChecker;
import com.rogermiranda1000.versioncontroller.VersionController;
import io.sentry.Sentry;
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
import java.util.ArrayList;

public abstract class RogerPlugin extends JavaPlugin implements CommandExecutor {
    public final String clearPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[" + this.getName() + "] " + ChatColor.GREEN,
            errorPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[" + this.getName() + "] " + ChatColor.RED;

    private final Listener []listeners;
    private CustomCommand []commands;
    private final Metrics.CustomChart []charts;
    private final ArrayList<CustomBlock<?>> customBlocks;
    private String noPermissionsMessage, unknownMessage;

    @Nullable
    private Metrics metrics;
    private boolean isRunning;

    /**
     * JavaPlugin with some basic functionalities.
     * @param commands      All the commands from the plugin. It's important that in the first position you set the 'help' command
     * @param listeners     All the event listeners from the plugin
     */
    public RogerPlugin(CustomCommand []commands, Listener... listeners) {
        this(commands, new Metrics.CustomChart[]{}, listeners);
    }

    /**
     * JavaPlugin with some basic functionalities. Also enables report data
     * @param commands      All the commands from the plugin. It's important that in the first position you set the 'help' command
     * @param charts        All the reported data
     * @param listeners     All the event listeners from the plugin TODO protect the listeners here
     */
    public RogerPlugin(CustomCommand []commands, Metrics.CustomChart []charts, Listener... listeners) {
        this.customBlocks = new ArrayList<>();
        this.isRunning = false;
        this.commands = commands;
        this.charts = charts;
        this.listeners = listeners; // Listener... is the same than Listener[]

        this.noPermissionsMessage = "You don't have the permissions to do that.";

        if (this.getSentryDsn() != null) {
            Sentry.init(options -> {
                options.setDsn(this.getSentryDsn());
                // capture 100% of transactions for performance monitoring
                options.setTracesSampleRate(1.0);
            });
        }
    }

    public RogerPlugin(Listener... listeners) {
        this(new CustomCommand[]{}, listeners);
    }

    /**
     * Called to add a new custom block
     * @return Method concatenation
     */
    // TODO on stop remove all?
    public RogerPlugin addCustomBlock(CustomBlock<?> cb) {
        if (cb.willSave()) {
            try {
                Class.forName("com.google.gson.JsonSyntaxException");
            } catch (ClassNotFoundException ex) {
                this.printConsoleErrorMessage( this.getName() + " needs Gson in order to work.");
                return this;
            }
        }

        this.customBlocks.add(cb);

        if (this.isRunning) {
            try {
                cb.load();
            } catch (IOException ex) {
                this.printConsoleErrorMessage("Invalid file format. The block '" + cb.getId() + "' can't be loaded.");
            }
            cb.register();
        }

        return this;
    }

    public void setCommandMessages(String noPermissionsMessage, String unknownMessage) {
        this.noPermissionsMessage = noPermissionsMessage;
        this.unknownMessage = unknownMessage;
    }

    /**
     * Called while the server is running if the block must be removed (DON'T call this onStop)
     * @return Method concatenation
     */
    public RogerPlugin removeCustomBlock(CustomBlock<?> cb) {
        try {
            cb.save();
        } catch (IOException e) {
            this.printConsoleErrorMessage("Error while disabling custom block"); // TODO get more info
            e.printStackTrace();
        }
        cb.unregister();

        this.customBlocks.remove(cb);

        return this;
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
     * @pre Before onEnable
     */
    public void setCommands(CustomCommand []commands) {
        this.commands = commands;
    }

    /**
     * Get the spigot ID to check for updates
     * @return Spigot ID, or NULL if don't want to check updates
     */
    @Nullable
    public String getPluginID() { return null; }

    /**
     * Get the bStats ID
     * @return bStats ID, or NULL if don't want to report
     */
    @Nullable
    public Integer getMetricsID() { return null; }

    @Nullable
    public String getSentryDsn() { return null; }

    public void reportException(Exception ex) {
        Sentry.captureException(ex);
    }

    public void reportException(String err) {
        Sentry.captureMessage(err);
    }

    /**
     * Check for updates, starts the listeners (& commands) and loads CustomBlocks
     */
    @Override
    public void onEnable() {
        try {
            this.isRunning = true;
            // TODO any way to save the instance here?

            this.preOnEnable();

            if (this.getMetricsID() != null) {
                this.metrics = new Metrics(this, this.getMetricsID());
                for (Metrics.CustomChart chart : this.charts) this.metrics.addCustomChart(chart);
            }

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
            for (CustomBlock<?> cb : this.customBlocks) cb.register();

            if (this.commands.length > 0 && VersionController.version.compareTo(Version.MC_1_10) >= 0) {
                // if MC > 10 we can send hints onTab
                String commandBase = this.getName().toLowerCase();
                getCommand(commandBase).setTabCompleter(new HintEvent(this));
            }

            // call enable functions
            for (CustomBlock<?> cb : this.customBlocks) {
                try {
                    cb.load();
                } catch (IOException ex) {
                    this.printConsoleErrorMessage("Invalid file format. The block '" + cb.getId() + "' can't be loaded.");
                }
            }

            this.postOnEnable();
        } catch (Exception ex) {
            this.reportException(ex);
        }
    }

    @Override
    public void onDisable() {
        try {
            this.isRunning = false;

            this.preOnDisable();

            // call disable functions
            for (CustomBlock<?> cb : this.customBlocks) {
                try {
                    cb.save();
                } catch (IOException e) {
                    this.printConsoleErrorMessage("Error while disabling custom block"); // TODO get more info
                    e.printStackTrace();
                }
            }

            this.postOnDisable();
        } catch (Exception ex) {
            this.reportException(ex);
        }
    }

    public void preOnEnable() {}
    public void postOnEnable() {}
    public void preOnDisable() {}
    public void postOnDisable() {}

    public void clearCustomBlocks() {
        for (CustomBlock<?> cb : this.customBlocks) {
            cb.removeAllBlocksArtificially();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        try {
            for (CustomCommand command : this.commands) {
                switch (command.search((sender instanceof Player) ? (Player) sender : null, cmd.getName(), args)) {
                    case NO_MATCH:
                        continue;

                    case NO_PERMISSIONS:
                        sender.sendMessage(this.errorPrefix + this.noPermissionsMessage);
                        break;
                    case MATCH:
                        command.notifier.onCommand(sender, args);
                        break;
                    case NO_PLAYER:
                        sender.sendMessage("Don't use this command in console.");
                        break;
                    case INVALID_LENGTH:
                        sender.sendMessage(this.errorPrefix +"Unknown command. Use " + ChatColor.GOLD + "/" + this.getName().toLowerCase() + " ?");
                        break;
                    default:
                        this.printConsoleErrorMessage("Unknown response to command");
                        return false;
                }
                return true;
            }

            sender.sendMessage(this.errorPrefix + this.unknownMessage);
            this.commands[0].notifier.onCommand(sender, new String[]{}); // '?' command
            return true;
        } catch (Exception ex) {
            this.reportException(ex);
            return false;
        }
    }
}
