package com.rogermiranda1000.helper;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface MatchCommandNotifier {
    /**
     * There's a command match
     * @param sender Command's sender
     * @param cmd Command + arguments
     */
    void onCommand(CommandSender sender, @NotNull String[] cmd);
}
