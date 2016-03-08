package de.teamlapen.vampirism.util;

import de.teamlapen.vampirism.config.Configs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * TODO integrate with e.g. ForgeEssentials
 */
public class Permissions {
    public static boolean getPermission(String permission) {
        return true;
    }

    public static boolean getPermission(String permission, EntityPlayer player) {
        if (permission.equals("pvp")) {
            if (!player.worldObj.isRemote) {
                return MinecraftServer.getServer().isPVPEnabled();
            }
        }
        return true;
    }

    public static boolean canPlayerTurnPlayer(EntityPlayer player) {
        return Configs.playerCanTurnPlayer;
    }
}