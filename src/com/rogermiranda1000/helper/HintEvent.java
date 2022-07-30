package com.rogermiranda1000.helper;

import io.sentry.Sentry;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class HintEvent implements TabCompleter {
    private final String commandBase;
    private final CustomCommand []commands;
    private final Reporter reporter;

    public HintEvent(RogerPlugin plugin) {
        this.commandBase = plugin.getName().toLowerCase();
        this.commands = plugin.getCommands();
        this.reporter = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            if (!command.getName().equalsIgnoreCase(this.commandBase)) return null;

            String[] rawCmd = (String[]) ArrayUtils.add(args, 0, this.commandBase);
            Set<String> hints = new HashSet<>(); // a set doesn't allow duplicates
            for (CustomCommand cmd : this.commands) hints.addAll(cmd.candidate(rawCmd));
            return new ArrayList<>(hints);
        } catch (Exception ex) {
            this.reporter.reportException(ex);
            return new ArrayList<>();
        }
    }
}

