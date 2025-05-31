package shedar.mods.ic2.nuclearcontrol.tileentities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraftforge.common.util.ForgeDirection;

import com.recursive_pineapple.nuclear_horizons.reactors.components.IReactorGrid;
import com.recursive_pineapple.nuclear_horizons.reactors.tile.IReactorBlock;
import com.recursive_pineapple.nuclear_horizons.reactors.tile.TileReactorChamber;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.tile.IWrenchable;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.ITextureHelper;
import shedar.mods.ic2.nuclearcontrol.blocks.subblocks.ThermalMonitor;
import shedar.mods.ic2.nuclearcontrol.items.ItemCard55Reactor;
import shedar.mods.ic2.nuclearcontrol.utils.BlockDamages;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearHelper;

public class TileEntityThermo extends TileEntity implements IWrenchable, ITextureHelper {

    protected boolean init;
    private int prevHeatLevel;
    public int heatLevel;
    private int prevOnFire;
    public int onFire;
    private short prevFacing;
    public short facing;
    private boolean prevInvertRedstone;
    private boolean invertRedstone;

    protected int updateTicker;
    protected int tickRate;

    public TileEntityThermo() {
        init = false;
        onFire = 0;
        prevOnFire = 0;
        facing = 0;
        prevFacing = 0;
        prevHeatLevel = 500;
        heatLevel = 500;
        updateTicker = 0;
        tickRate = -1;
        prevInvertRedstone = false;
        invertRedstone = false;
    }

    protected void initData() {
        if (!worldObj.isRemote)
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));

        init = true;
    }

    public boolean isInvertRedstone() {
        return invertRedstone;
    }

    public void setInvertRedstone(boolean value) {
        invertRedstone = value;

        if (prevInvertRedstone != value) {
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
        }

        prevInvertRedstone = value;
    }

    @Override
    public short getFacing() {
        return (short) Facing.oppositeSide[facing];
    }

    @Override
    public void setFacing(short f) {
        setSide((short) Facing.oppositeSide[f]);
    }

    private void setSide(short f) {
        facing = f;

        if (init && prevFacing != f) markForUpdate();

        prevFacing = f;
    }

    public void setOnFire(int f) {
        onFire = f;

        if (prevOnFire != f) markForUpdate();

        prevOnFire = onFire;
    }

    public int getOnFire() {
        return onFire;
    }

    public void setHeatLevel(int h) {
        heatLevel = h;

        if (prevHeatLevel != h) markForUpdate();

        prevHeatLevel = heatLevel;
    }

    public void setHeatLevelWithoutNotify(int h) {
        heatLevel = h;
        prevHeatLevel = heatLevel;
    }

    public Integer getHeatLevel() {
        return heatLevel;
    }

    protected void markForUpdate() {
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setShort("facing", facing);
        tag.setInteger("onFire", onFire);
        tag.setInteger("heatLevel", heatLevel);
        tag.setBoolean("invertRedstone", invertRedstone);

        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, getBlockMetadata(), tag);
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound tag = pkt.func_148857_g();

        this.facing = tag.getShort("facing");
        this.onFire = tag.getInteger("onFire");
        this.heatLevel = tag.getInteger("heatLevel");
        this.invertRedstone = tag.getBoolean("invertRedstone");

        boolean needsUpdate = prevFacing != facing || onFire != prevOnFire || heatLevel != prevHeatLevel || invertRedstone != prevInvertRedstone;

        if (needsUpdate) {
            prevFacing = facing;
            onFire = prevOnFire;
            heatLevel = prevHeatLevel;
            invertRedstone = prevInvertRedstone;

            markForUpdate();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if (tag.hasKey("heatLevel")) {
            int heat = tag.getInteger("heatLevel");
            prevFacing = facing = tag.getShort("facing");
            prevInvertRedstone = invertRedstone = tag.getBoolean("invert");

            setHeatLevelWithoutNotify(heat);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setInteger("heatLevel", getHeatLevel());
        tag.setShort("facing", facing);
        tag.setBoolean("invert", isInvertRedstone());
    }

    protected void checkStatus() {
        byte fire;
        IReactorBlock chamber = NuclearHelper.getReactorChamberAroundCoord(worldObj, xCoord, yCoord, zCoord);
        IReactorGrid reactor = null;

        if (chamber != null) reactor = chamber.getReactor();

        if (reactor == null) reactor = NuclearHelper.getReactorAroundCoord(worldObj, xCoord, yCoord, zCoord);

        if (reactor == null && chamber == null) {
            ForgeDirection facing;

            if (this.getFacing() > 5) {
                facing = ForgeDirection.UNKNOWN;
            } else {
                facing = ForgeDirection.VALID_DIRECTIONS[this.getFacing()].getOpposite();
            }

//            reactor = ItemCard55Reactor.getReactor(worldObj, xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);
        }

        if (reactor != null) {
            if (tickRate == -1) {
                tickRate = reactor.getTickRate() / 2;

                if (tickRate == 0) tickRate = 1;

                updateTicker = tickRate;
            }

            int reactorHeat = reactor.getHullHeat();

            if (reactorHeat >= heatLevel) {
                fire = 1;
            } else {
                fire = 0;
            }
        } else {
            fire = -1;
        }

        if (fire != getOnFire()) {
            setOnFire(fire);
            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
        }
    }

    @Override
    public void updateEntity() {
        if (!init) initData();

        if (!worldObj.isRemote) {
            if (tickRate != -1 && updateTicker-- > 0) return;
            updateTicker = tickRate;
            checkStatus();
        }
    }

    @Override
    public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side) {
        return false;
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public float getWrenchDropRate() {
        return 1;
    }

    @Override
    public int modifyTextureIndex(int texture) {
        if (texture != ThermalMonitor.I_FACE_GREEN) return texture;
        int fireState = getOnFire();
        switch (fireState) {
            case 1:
                texture = ThermalMonitor.I_FACE_RED;
                break;
            case 0:
                texture = ThermalMonitor.I_FACE_GREEN;
                break;
            default:
                texture = ThermalMonitor.I_FACE_GRAY;
                break;
        }
        return texture;
    }

    @Override
    public ItemStack getWrenchDrop(EntityPlayer entityPlayer) {
        return new ItemStack(IC2NuclearControl.blockNuclearControlMain, 1, BlockDamages.DAMAGE_THERMAL_MONITOR);
    }
}
