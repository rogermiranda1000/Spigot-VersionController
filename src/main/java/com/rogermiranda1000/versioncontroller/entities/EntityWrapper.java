package com.rogermiranda1000.versioncontroller.entities;

import com.rogermiranda1000.helper.RogerPlugin;
import com.rogermiranda1000.versioncontroller.Version;
import com.rogermiranda1000.versioncontroller.VersionController;
import com.rogermiranda1000.versioncontroller.entities.bounds.BoundingBox;
import com.rogermiranda1000.versioncontroller.entities.bounds.EntityBoundsGetter;
import com.rogermiranda1000.versioncontroller.entities.bounds.EntityBoundsGetterPost14;
import com.rogermiranda1000.versioncontroller.entities.bounds.EntityBoundsGetterPre14;
import com.rogermiranda1000.versioncontroller.entities.gravity.EntityGravityManager;
import com.rogermiranda1000.versioncontroller.entities.gravity.EntityGravityManagerPost10;
import com.rogermiranda1000.versioncontroller.entities.gravity.EntityGravityManagerPre10;
import com.rogermiranda1000.versioncontroller.entities.velocity.EntityVelocityGetter;
import com.rogermiranda1000.versioncontroller.entities.velocity.EntityVelocityGetterPaper;
import com.rogermiranda1000.versioncontroller.entities.velocity.EntityVelocityGetterSpigot;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class EntityWrapper {
    private static final EntityBoundsGetter boundsGetter = (VersionController.version.compareTo(Version.MC_1_14) < 0) ? new EntityBoundsGetterPre14() : new EntityBoundsGetterPost14();
    private static final EntityVelocityGetter velocityGetter = (VersionController.isPaper) ? new EntityVelocityGetterPaper() : new EntityVelocityGetterSpigot();
    private static final EntityGravityManager gravityManager = (VersionController.version.compareTo(Version.MC_1_10) < 0) ? new EntityGravityManagerPre10() : new EntityGravityManagerPost10();

    private final Entity entity;

    public EntityWrapper(Entity e) {
        this.entity = e;
    }

    public Entity getEntity() {
        return this.entity;
    }

    @NotNull
    public Vector getVelocity() {
        return EntityWrapper.velocityGetter.getVelocity(this.entity);
    }

    @NotNull
    public static Vector getVelocity(PlayerMoveEvent e) {
        return EntityWrapper.velocityGetter.getVelocity(e);
    }

    public void setLocation(Location l) {
        EntityWrapper.gravityManager.setLocation(this.entity, l);
    }

    public void disableGravity() {
        EntityWrapper.gravityManager.disableGravity(this.entity);
    }

    public void enableGravity() {
        EntityWrapper.gravityManager.enableGravity(this.entity);
    }

    public BoundingBox getBoundingBox() {
        return EntityWrapper.boundsGetter.getBoundingBox(this.getEntity());
    }

    public static void registerListeners(RogerPlugin plugin) {
        EntityWrapper.gravityManager.registerListeners(plugin);
    }
}
