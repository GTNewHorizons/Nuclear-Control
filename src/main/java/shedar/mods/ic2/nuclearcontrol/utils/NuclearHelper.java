package shedar.mods.ic2.nuclearcontrol.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.recursive_pineapple.nuclear_horizons.reactors.components.IReactorGrid;
import com.recursive_pineapple.nuclear_horizons.reactors.tile.IReactorBlock;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;

public class NuclearHelper {

    private static final double STEAM_PER_EU = 3.2D;

    public static IReactor getReactorAt(World world, int x, int y, int z) {
        if (world == null) return null;
        TileEntity entity = world.getTileEntity(x, y, z);
        if (entity instanceof IReactor) return (IReactor) entity;
        return null;
    }

    public static boolean isSteam(IReactor reactor) {
        return IC2NuclearControl.instance.crossIc2.isSteamReactor((TileEntity) reactor);
    }

    public static int euToSteam(int eu) {
        return (int) Math.floor((eu) * STEAM_PER_EU);
    }

    public static IReactorChamber getReactorChamberAt(World world, int x, int y, int z) {
        if (world == null) return null;
        TileEntity entity = world.getTileEntity(x, y, z);
        if (entity instanceof IReactorChamber) {
            return (IReactorChamber) entity;
        }
        return null;
    }

    public static IReactorGrid getReactorAroundCoord(World world, int x, int y, int z) {
        if (world == null) return null;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!(world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) instanceof IReactorGrid reactor)) continue;

            return reactor;
        }

        return null;
    }

    public static IReactorBlock getReactorChamberAroundCoord(World world, int x, int y, int z) {
        if (world == null) return null;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!(world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ) instanceof IReactorBlock chamber)) continue;

            return chamber;
        }

        return null;
    }

    public static boolean isProducing(IReactor reactor) {
        ChunkCoordinates position = reactor.getPosition();
        return reactor.getWorld().isBlockIndirectlyGettingPowered(position.posX, position.posY, position.posZ);
    }

    public static int getNuclearCellTimeLeft(ItemStack rStack) {
        int val;
        if (IC2NuclearControl.instance.crossGT.isApiAvailable()) {
            val = IC2NuclearControl.instance.crossGT.getNuclearCellTimeLeft(rStack);
            if (val == -1) {
                val = IC2NuclearControl.instance.crossIc2.getNuclearCellTimeLeft(rStack);
            }
        } else {
            val = IC2NuclearControl.instance.crossIc2.getNuclearCellTimeLeft(rStack);
        }
        return val;
    }
}
