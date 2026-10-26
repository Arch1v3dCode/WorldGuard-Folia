/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.bukkit.util;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * This class is mainly here to replace PaperLib, as it no longer fully functions on MC 26.1+.
 */
public class PaperInterop {
    private PaperInterop() {
    }

    public static @Nullable InventoryHolder getHolder(@Nonnull Inventory inventory, boolean useSnapshot) {
        if (PaperLib.isPaper()) {
            return inventory.getHolder(useSnapshot);
        }

        return inventory.getHolder();
    }

    public static BlockState getBlockState(@Nonnull Block block, boolean useSnapshot) {
        if (PaperLib.isPaper()) {
            return block.getState(useSnapshot);
        }

        return block.getState();
    }

    public static CompletableFuture<Boolean> teleportAsync(@Nonnull Entity entity, @Nonnull Location location) {
        if (PaperLib.isPaper()) {
            return entity.teleportAsync(location);
        }

        return CompletableFuture.completedFuture(entity.teleport(location));
    }

    /**
     * Teleport an entity without calling the synchronous teleport API.
     *
     * <p>Folia and Canvas reject {@code Entity#teleport}. Canvas also rejects teleports made
     * directly inside move events and during login or logout, so region-threaded servers
     * run the teleport on the entity scheduler one tick later.</p>
     */
    public static void teleportLater(@Nonnull Plugin plugin, @Nonnull Entity entity, @Nonnull Location location) {
        Location destination = location.clone();
        if (WorldGuardPlugin.inst().isFolia()) {
            entity.getScheduler().runDelayed(plugin, task -> {
                if (!entity.isValid()) {
                    return;
                }
                teleportAsync(entity, destination);
            }, null, 1L);
            return;
        }

        entity.teleport(destination);
    }
}
