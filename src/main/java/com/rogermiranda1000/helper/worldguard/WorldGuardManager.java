package com.rogermiranda1000.helper.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.BinaryOperator;

/**
 * IMPORTANT: this class may not work as intended in plugins that overrides protections
 */
public class WorldGuardManager implements RegionDelimiter {
    /*private HashMap<String,Boolean> defaultValues = new HashMap<>();
    private HashMap<String, Flag<StateFlag.State>> flags = new HashMap<>();*/

    @Override
    public boolean isInsideRegion(Location target, Collection<String> regionNames) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        ApplicableRegionSet regions = container.createQuery().getApplicableRegions(BukkitAdapter.adapt(target));
        for (ProtectedRegion pr : regions) {
            if (regionNames.contains(pr.getId())) return true;
        }
        return false;
    }

    /*@Override
    public void setupFlag(String flagName, boolean defaultValue) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(flagName, defaultValue);
            registry.register(flag);
            this.flags.put(flagName, flag);
        } catch (FlagConflictException ex) {
            // already existing
            Flag<?> flag = registry.get(flagName);
            if (flag instanceof StateFlag) this.flags.put(flagName, (StateFlag)flag);
        }

        this.defaultValues.put(flagName,defaultValue);
    }

    @Override
    public Boolean getFlagValue(Location target, String flag, BinaryOperator<Boolean> reduce) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        ApplicableRegionSet regions = container.createQuery().getApplicableRegions(BukkitAdapter.adapt(target));
        if (regions.size() == 0) return null;

        boolean first = true;
        Boolean ret = null;
        for (ProtectedRegion pr : regions) {
            StateFlag.State state = pr.getFlag(this.flags.get(flag));
            Boolean stateValue = (state == null) ? null : state.equals(StateFlag.State.ALLOW);
            if (first) {
                ret = stateValue;
                first = false;
            }
            else ret = reduce.apply(ret, stateValue);
        }
        return ret;
    }*/
}
