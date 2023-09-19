package com.rogermiranda1000.versioncontroller.entities;

import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.entities.player.PlayerManager;
import com.rogermiranda1000.versioncontroller.entities.player.PlayerPost9;
import com.rogermiranda1000.versioncontroller.entities.player.PlayerPre9;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VCPlayer extends EntityWrapper {
    private static final PlayerManager playerManager = (VersionController.version.compareTo(Version.MC_1_9) < 0) ? new PlayerPre9() : new PlayerPost9();

    public VCPlayer(Player p) {
        super(p);
    }

    public ItemStack[] getItemInHand() {
        return VCPlayer.playerManager.getItemInHand((Player)this.getEntity());
    }

    public void setItemInHand(ItemStack item, boolean leftHand) {
        VCPlayer.playerManager.setItemInHand(((Player)this.getEntity()).getInventory(), item, leftHand);
    }

    public boolean hasItemInHand(ItemStack i) {
        return VCPlayer.playerManager.hasItemInHand((Player)this.getEntity(), i);
    }
}
