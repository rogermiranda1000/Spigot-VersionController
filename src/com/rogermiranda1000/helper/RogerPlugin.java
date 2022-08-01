package com.rogermiranda1000.helper;

import com.rogermiranda1000.helper.blocks.CustomBlock;
import com.rogermiranda1000.helper.metrics.Metrics;
import com.rogermiranda1000.helper.reflection.SpigotEventOverrider;
import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionChecker;
import com.rogermiranda1000.versioncontroller.VersionController;
import io.sentry.*;
import io.sentry.protocol.SentryId;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class RogerPlugin extends JavaPlugin implements CommandExecutor, Reporter {
    public final String clearPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[" + this.getName() + "] " + ChatColor.GREEN,
            errorPrefix = ChatColor.GOLD.toString() + ChatColor.BOLD + "[" + this.getName() + "] " + ChatColor.RED;

    private final Listener []listeners;
    private CustomCommand []commands;
    private final Metrics.CustomChart []charts;
    private final List<CustomBlock<?>> customBlocks;
    private String noPermissionsMessage, unknownMessage;

    @Nullable
    private Metrics metrics;
    @Nullable
    private IHub hub;
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
     * @param listeners     All the event listeners from the plugin
     */
    public RogerPlugin(CustomCommand []commands, Metrics.CustomChart []charts, final Listener... listeners) {
        this.customBlocks = new ArrayList<>();
        this.isRunning = false;
        this.commands = commands;
        this.charts = charts;
        this.listeners = listeners; // Listener... is the same than Listener[]

        this.noPermissionsMessage = "You don't have the permissions to do that.";
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

    @Nullable
    private static StackTraceElement getMyFault(Throwable ex) {
        // get the first time my package was found
        for (StackTraceElement stack : ex.getStackTrace()) {
            if (stack.getClassName().startsWith("com.rogermiranda1000.")) {
                return stack;
            }
        }
        return null;
    }

    private void setFingerprint(Scope scope, Throwable ex) {
        List<String> r = new ArrayList<>();
        r.add(ex.getClass().getName());
        r.add(ex.getMessage());
        StackTraceElement fail = RogerPlugin.getMyFault(ex);
        if (fail != null) {
            r.add(fail.getClassName() + ": " + fail.getLineNumber());
            scope.setFingerprint(r);
        }
        // else (my package was not found) -> default scope
    }

    @Override
    public void reportException(final Throwable ex) {
        if (this.hub != null) {
            this.hub.captureException(ex, (scope)->this.setFingerprint(scope, ex));

            StackTraceElement fault = getMyFault(ex);
            this.printConsoleErrorMessage("Error captured: " + ex.getMessage() + ((fault == null) ? "" : (" (" + fault.getClassName() + ":" + fault.getLineNumber() + ")")));
        }
        else ex.printStackTrace();
    }

    private int reports = 0;
    @Override
    public void reportRepeatedException(Throwable ex) {
        if (++reports > 30) return; // 30 reports per reload; otherwise ignore
        this.reportException(ex);
    }

    @Override
    public void reportException(String err) {
        if (this.hub != null) this.hub.captureMessage(err);
        System.err.println(err);
    }

    @Override
    public void userReport(@Nullable String contact, @Nullable String name, String message) {
        SentryId sentryId = this.hub.captureMessage("report", SentryLevel.INFO, (scope)->scope.setFingerprint(Arrays.asList(UUID.randomUUID().toString())));

        UserFeedback userFeedback = new UserFeedback(sentryId);
        userFeedback.setComments(message);
        if (name != null) userFeedback.setName(name);
        if (contact != null) userFeedback.setEmail(contact);
        this.hub.captureUserFeedback(userFeedback);
    }

    private IHub initSentry() {
        SentryOptions options = new SentryOptions();
        options.setDsn(this.getSentryDsn());

        // capture 100% of transactions for performance monitoring
        options.setSampleRate(1.0);
        options.setTracesSampleRate(1.0);

        options.setAttachServerName(false); // give the user some privacy

        options.setRelease(this.getDescription().getVersion());

        options.setTag("server-version", VersionController.version.toString());
        options.setTag("spigot", Boolean.toString(!VersionController.isPaper));
        // TODO attach config file
        // TODO add plugins using

        /*options.setDebug(true);
        options.setDiagnosticLevel(SentryLevel.ERROR);*/

        return new Hub(options);
    }

    /**
     * Check for updates, starts the listeners (& commands) and loads CustomBlocks
     */
    @Override
    public void onEnable() {
        try {
            // TODO any way to save the instance here?

            this.reports = 0;
            if (this.getSentryDsn() != null) this.hub = this.initSentry();

            this.preOnEnable();

            // TODO abort

            if (this.getMetricsID() != null) {
                this.metrics = new Metrics(this, this.getMetricsID());
                for (Metrics.CustomChart chart : this.charts) this.metrics.addCustomChart(chart);
            }

            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    String id = this.getPluginID();
                    if (id != null) {
                        String version = VersionChecker.getVersion(id);
                        if (VersionChecker.isLower(this.getDescription().getVersion(), version))
                            this.printConsoleWarningMessage("v" + version + " is now available! You should consider updating the plugin.");
                    }
                } catch (IOException e) {
                    this.printConsoleWarningMessage("Can't check for updates.");
                }
            });

            // get all the events
            List<Listener> listeners = new ArrayList<>(Arrays.asList(this.listeners));
            for (CustomBlock<?> cb : this.customBlocks) listeners.addAll(cb.register());

            // register the events
            for (Listener lis : listeners) {
                SpigotEventOverrider.wrapListeners(this, lis, (run) -> {
                    try {
                        try {
                            run.run();
                        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
                            System.err.println("Error while overriding " + lis.getClass().getName());
                            throw ex;
                        }  catch (InvocationTargetException ex) {
                            throw ex.getCause();
                        }
                    } catch (Throwable ex) {
                        this.reportRepeatedException(ex);
                    }
                });
            }

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

            this.isRunning = true;
        } catch (SoftCriticalException ex) {
            this.printConsoleErrorMessage(ex.getMessage());
            getServer().getPluginManager().disablePlugin(this); // error in the start -> kill
        } catch (SoftException ex) {
            this.printConsoleErrorMessage(ex.getMessage());
        } catch (Throwable ex) {
            this.reportException(ex);
            getServer().getPluginManager().disablePlugin(this); // error in the start -> kill
        }
    }

    @Override
    public void onDisable() {
        try {
            if (!this.isRunning) return; // killed onStart
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
        } catch (Throwable ex) {
            this.reportException(ex);
        }

        this.hub.close();
        this.hub = null;
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
        } catch (Throwable ex) {
            this.reportException(ex);
            return false;
        }
    }
}
