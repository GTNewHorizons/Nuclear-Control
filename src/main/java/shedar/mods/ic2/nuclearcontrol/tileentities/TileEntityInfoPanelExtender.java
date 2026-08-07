package shedar.mods.ic2.nuclearcontrol.tileentities;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;

import cpw.mods.fml.common.FMLCommonHandler;
import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.IRotation;
import shedar.mods.ic2.nuclearcontrol.IScreenPart;
import shedar.mods.ic2.nuclearcontrol.ITextureHelper;
import shedar.mods.ic2.nuclearcontrol.blocks.subblocks.InfoPanel;
import shedar.mods.ic2.nuclearcontrol.panel.Screen;
import shedar.mods.ic2.nuclearcontrol.utils.BlockDamages;

public class TileEntityInfoPanelExtender extends TileEntity
        implements INetworkDataProvider, INetworkUpdateListener, IWrenchable, ITextureHelper, IScreenPart, IRotation {

    protected boolean init;
    private int screenRetryTicks = 40;

    private Screen screen;
    private short prevFacing;
    public short facing;
    private boolean partOfScreen;
    private boolean NBTLoaded;
    private int coreX;
    private int coreY;
    private int coreZ;

    @Override
    public short getFacing() {
        return (short) Facing.oppositeSide[facing];
    }

    public boolean getPartOfScreen() {
        return partOfScreen;
    }

    public boolean getNBTLoaded() {
        return NBTLoaded;
    }

    @Override
    public void setFacing(short f) {
        setSide((short) Facing.oppositeSide[f]);

    }

    private void setSide(short f) {
        facing = f;
        if (init && prevFacing != f) {
            if (FMLCommonHandler.instance().getEffectiveSide().isServer() && init) {
                IC2NuclearControl.instance.screenManager.unregisterScreenPart(this);
                IC2NuclearControl.instance.screenManager.registerInfoPanelExtender(this);
            }

            ((NetworkManager) IC2.network.get()).updateTileEntityField(this, "facing");
        }

        prevFacing = f;
    }

    @Override
    public void onNetworkUpdate(String field) {
        if (field.equals("facing") && prevFacing != facing) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            prevFacing = facing;
        } else if (field.equals("partOfScreen") || field.equals("coreX")
                || field.equals("coreY")
                || field.equals("coreZ")) {
                    tryResolveScreen();
                }
    }

    public TileEntityInfoPanelExtender() {
        super();
        init = false;
        facing = 0;
        prevFacing = 0;
        screen = null;
        partOfScreen = false;
        NBTLoaded = false;
    }

    @Override
    public List<String> getNetworkedFields() {
        List<String> list = new ArrayList<String>(5);
        list.add("facing");
        list.add("partOfScreen");
        list.add("coreX");
        list.add("coreY");
        list.add("coreZ");
        return list;
    }

    protected void initData() {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer() && !partOfScreen) {
            IC2NuclearControl.instance.screenManager.registerInfoPanelExtender(this);
        }
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            IC2.network.get().updateTileEntityField(this, "facing");
        }
        tryResolveScreen();
        init = true;
    }

    /**
     * Resolve and attach the screen of this extender. Runs at init, when the networked fields of the extender arrive,
     * and as a periodic retry while the core panel chunk has not been loaded yet. Chunk load order is arbitrary and the
     * client only receives the screen identity (partOfScreen/coreX/coreY/coreZ) through the IC2 field sync, so without
     * the retry an extender whose chunk loads late would stay unattached forever.
     */
    private void tryResolveScreen() {
        if (!partOfScreen || screen != null) return;
        TileEntity core = worldObj.getTileEntity(coreX, coreY, coreZ);
        if (core != null && core instanceof TileEntityInfoPanel) {
            screen = ((TileEntityInfoPanel) core).getScreen();
            if (screen != null) screen.init(true, worldObj);
        }
    }

    public void setCoreCoordinates(int x, int y, int z) {
        coreX = x;
        coreY = y;
        coreZ = z;
    }

    @Override
    public void updateEntity() {
        if (!init) {
            initData();
        }
        if (partOfScreen && screen == null && --screenRetryTicks <= 0) {
            screenRetryTicks = 40;
            tryResolveScreen();
        }
        super.updateEntity();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        prevFacing = facing = nbttagcompound.getShort("facing");
        partOfScreen = nbttagcompound.getBoolean("partOfScreen");
        if (nbttagcompound.hasKey("coreX")) {
            coreX = nbttagcompound.getInteger("coreX");
            coreY = nbttagcompound.getInteger("coreY");
            coreZ = nbttagcompound.getInteger("coreZ");
        }
        NBTLoaded = true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            IC2NuclearControl.instance.screenManager.unregisterScreenPart(this);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setShort("facing", facing);
        nbttagcompound.setBoolean("partOfScreen", partOfScreen);
        if (screen != null) {
            nbttagcompound.setInteger("coreX", coreX);
            nbttagcompound.setInteger("coreY", coreY);
            nbttagcompound.setInteger("coreZ", coreZ);
        }
    }

    @Override
    public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int face) {
        return !entityPlayer.isSneaking() && getFacing() != face;
    };

    @Override
    public float getWrenchDropRate() {
        return 1;
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
        return !entityPlayer.isSneaking();
    }

    @Override
    public int modifyTextureIndex(int texture) {
        if (texture != InfoPanel.I_COLOR_DEFAULT) return texture;
        if (screen != null) {
            TileEntityInfoPanel core = screen.getCore(worldObj);
            if (core != null) {
                return core.modifyTextureIndex(texture, xCoord, yCoord, zCoord);
            }
        }
        return texture;
    }

    @Override
    public void setScreen(Screen screen) {
        this.screen = screen;
        partOfScreen = screen != null;
        if (partOfScreen) {
            setCoreCoordinates(
                    screen.getCore(worldObj).xCoord,
                    screen.getCore(worldObj).yCoord,
                    screen.getCore(worldObj).zCoord);
        }
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            IC2.network.get().updateTileEntityField(this, "partOfScreen");
            IC2.network.get().updateTileEntityField(this, "coreX");
            IC2.network.get().updateTileEntityField(this, "coreY");
            IC2.network.get().updateTileEntityField(this, "coreZ");
        }
    }

    @Override
    public Screen getScreen() {
        return screen;
    }

    @Override
    public void rotate() {
        if (screen != null) {
            TileEntityInfoPanel core = screen.getCore(worldObj);
            if (core != null) core.rotate();
        }
    }

    @Override
    public int getRotation() {
        if (screen != null) {
            TileEntityInfoPanel core = screen.getCore(worldObj);
            if (core != null) return core.rotation;
        }
        return 0;
    }

    @Override
    public void setRotation(int rotation) {
        if (screen != null) {
            TileEntityInfoPanel core = screen.getCore(worldObj);
            if (core != null) core.setRotation(rotation);
        }
    }

    @Override
    public ItemStack getWrenchDrop(EntityPlayer entityPlayer) {
        return new ItemStack(IC2NuclearControl.blockNuclearControlMain, 1, BlockDamages.DAMAGE_INFO_PANEL_EXTENDER);
    }

    @Override
    public void updateData() {}
}
