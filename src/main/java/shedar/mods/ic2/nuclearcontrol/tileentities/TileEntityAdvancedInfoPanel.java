package shedar.mods.ic2.nuclearcontrol.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import ic2.core.IC2;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.items.ItemUpgrade;
import shedar.mods.ic2.nuclearcontrol.panel.CardWrapperImpl;
import shedar.mods.ic2.nuclearcontrol.utils.BlockDamages;
import shedar.mods.ic2.nuclearcontrol.utils.DataSorter;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;

public class TileEntityAdvancedInfoPanel extends TileEntityInfoPanel {

    // <editor-fold desc="Constants">
    private static final byte SLOT_CARD1 = 0;
    private static final byte SLOT_CARD2 = 1;
    private static final byte SLOT_CARD3 = 2;
    private static final byte SLOT_UPGRADE_RANGE = 3;
    private static final byte SLOT_UPGRADE_WEB = 3;

    public static final int POWER_REDSTONE = 0;
    public static final int POWER_INVERTED = 1;
    public static final int POWER_ON = 2;
    public static final int POWER_OFF = 3;
    public static final int TRANSPARENCY_CHANGED = 6;
    public static final int ROTATE_LEFT = 7;
    public static final int ROTATE_RIGHT = 8;

    public static final int OFFSET_THICKNESS = 100;
    public static final int OFFSET_ROTATE_HOR = 200;
    public static final int OFFSET_ROTATE_VERT = 300;
    // </editor-fold>

    // <editor-fold desc="State Fields">
    public byte powerMode;
    public byte transparencyMode;
    public byte thickness;
    public byte rotateHor;
    public byte rotateVert;
    public byte textRotation;

    private byte prevPowerMode;
    private byte prevtransparencyMode;
    private byte prevThickness;
    private byte prevRotateHor;
    private byte prevRotateVert;
    private byte prevTextRotation;

    public ItemStack card2;
    public ItemStack card3;

    protected final Map<Byte, Map<UUID, DataSorter>> dataSorters = new HashMap<>();
    // </editor-fold>

    // <editor-fold desc="Constructor">

    /**
     * Default constructor for the Advanced Information Panel.
     */
    public TileEntityAdvancedInfoPanel() {
        super(4); // 3 cards + range/web upgrade
        colored = true;
        thickness = 16;
    }
    // </editor-fold>

    // <editor-fold desc="Inventory Handling">
    @Override
    public int getCardSlotsCount() {
        return 3;
    }

    @Override
    public List<ItemStack> getCards() {
        List<ItemStack> data = new ArrayList<>(3);
        data.add(inventory[SLOT_CARD1]);
        data.add(inventory[SLOT_CARD2]);
        data.add(inventory[SLOT_CARD3]);
        return data;
    }

    @Override
    protected boolean isCardSlot(int slot) {
        return slot == SLOT_CARD1 || slot == SLOT_CARD2 || slot == SLOT_CARD3;
    }

    @Override
    public boolean isItemValid(int slotIndex, ItemStack itemstack) {
        switch (slotIndex) {
            case SLOT_CARD1:
            case SLOT_CARD2:
            case SLOT_CARD3:
                return itemstack.getItem() instanceof IPanelDataSource;
            case SLOT_UPGRADE_RANGE:
                return itemstack.getItem() instanceof ItemUpgrade
                        && (itemstack.getItemDamage() == ItemUpgrade.DAMAGE_RANGE
                                || itemstack.getItemDamage() == ItemUpgrade.DAMAGE_WEB);
            default:
                return false;
        }
    }

    @Override
    protected ItemStack getRangeUpgrade() {
        return inventory[SLOT_UPGRADE_RANGE];
    }

    @Override
    protected boolean isWebEval() {
        ItemStack itemStack = inventory[SLOT_UPGRADE_WEB];
        return itemStack != null && itemStack.getItem() instanceof ItemUpgrade
                && itemStack.getItemDamage() == ItemUpgrade.DAMAGE_WEB;
    }
    // </editor-fold>

    // <editor-fold desc="Power and Display Settings">
    public byte getPowerMode() {
        return powerMode;
    }

    public void setPowerMode(byte p) {
        powerMode = p;
        if (prevPowerMode != p) {
            IC2.network.get().updateTileEntityField(this, "powerMode");
        }
        prevPowerMode = powerMode;
    }

    public byte getTransparencyMode() {
        return transparencyMode;
    }

    public void setTransparencyMode(byte b) {
        if (b == 2) b = 0;
        transparencyMode = b;
        if (prevtransparencyMode != b) {
            IC2.network.get().updateTileEntityField(this, "transparencyMode");
        }
        prevtransparencyMode = transparencyMode;
    }

    public void setThickness(byte p) {
        thickness = p;
        if (prevThickness != p) {
            IC2.network.get().updateTileEntityField(this, "thickness");
        }
        prevThickness = thickness;
    }

    public byte getThickness() {
        return thickness;
    }

    public void setRotateHor(byte p) {
        rotateHor = p;
        if (prevRotateHor != p) {
            IC2.network.get().updateTileEntityField(this, "rotateHor");
        }
        prevRotateHor = rotateHor;
    }

    public byte getRotationHor() {
        return rotateHor;
    }

    public void setRotateVert(byte p) {
        rotateVert = p;
        if (prevRotateVert != p) {
            IC2.network.get().updateTileEntityField(this, "rotateVert");
        }
        prevRotateVert = rotateVert;
    }

    public byte getRotationVert() {
        return rotateVert;
    }

    @Override
    public byte getTextRotation() {
        return textRotation;
    }

    public void setTextRotation(byte r) {
        if (r == -1) r = 3;
        else if (r == 4) r = 0;
        textRotation = r;
        if (prevTextRotation != r) {
            IC2.network.get().updateTileEntityField(this, "textRotation");
        }
        prevTextRotation = textRotation;
    }

    public byte getNextPowerMode() {
        switch (powerMode) {
            case POWER_REDSTONE:
                return POWER_INVERTED;
            case POWER_INVERTED:
                return POWER_ON;
            case POWER_ON:
                return POWER_OFF;
            case POWER_OFF:
                return POWER_REDSTONE;
        }
        return POWER_REDSTONE;
    }

    @Override
    public boolean getPowered() {
        switch (powerMode) {
            case POWER_ON:
                return true;
            case POWER_OFF:
                return false;
            case POWER_REDSTONE:
                return powered;
            case POWER_INVERTED:
                return !powered;
        }
        return false;
    }
    // </editor-fold>

    // <editor-fold desc="Networking">
    @Override
    public List<String> getNetworkedFields() {
        List<String> list = super.getNetworkedFields();
        list.add("card2");
        list.add("card3");
        list.add("powerMode");
        list.add("transparencyMode");
        list.add("thickness");
        list.add("rotateHor");
        list.add("rotateVert");
        list.add("textRotation");
        return list;
    }

    @Override
    public void onNetworkUpdate(String field) {
        super.onNetworkUpdate(field);
        if (field.equals("card2")) {
            inventory[SLOT_CARD2] = card2;
        } else if (field.equals("card3")) {
            inventory[SLOT_CARD3] = card3;
        } else if (field.equals("powerMode") && prevPowerMode != powerMode) {
            if (screen != null) {
                screen.turnPower(getPowered(), worldObj);
            } else {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                worldObj.func_147451_t(xCoord, yCoord, zCoord);
            }
            prevPowerMode = powerMode;
        } else if (field.equals("thickness") || field.equals("rotateHor")
                || field.equals("rotateVert")
                || field.equals("textRotation")
                || field.equals("transparencyMode")) {
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                    if (field.equals("transparencyMode")) {
                        worldObj.func_147451_t(xCoord, yCoord, zCoord);
                    }
                }
    }

    @Override
    protected void initData() {
        super.initData();

        if (worldObj.isRemote) {
            NuclearNetworkHelper.requestDataSorters(this);
        }
    }

    @Override
    public void onNetworkEvent(EntityPlayer entityplayer, int i) {
        super.onNetworkEvent(entityplayer, i);
        if (i >= 0 && i < 100) {
            switch (i) {
                case POWER_ON:
                case POWER_OFF:
                case POWER_REDSTONE:
                case POWER_INVERTED:
                    setPowerMode((byte) i);
                    break;
                case TRANSPARENCY_CHANGED:
                    setTransparencyMode((byte) (getTransparencyMode() + 1));
                    break;
                case ROTATE_LEFT:
                    setTextRotation((byte) (textRotation - 1));
                    break;
                case ROTATE_RIGHT:
                    setTextRotation((byte) (textRotation + 1));
                    break;
            }
        } else if (i >= OFFSET_THICKNESS && i < OFFSET_THICKNESS + 100) {
            setThickness((byte) (i - OFFSET_THICKNESS));
        } else if (i >= OFFSET_ROTATE_HOR && i < OFFSET_ROTATE_HOR + 100) {
            i -= OFFSET_ROTATE_HOR + 8;
            setRotateHor((byte) (-(i * 7)));
        } else if (i >= OFFSET_ROTATE_VERT && i < OFFSET_ROTATE_VERT + 100) {
            i -= OFFSET_ROTATE_VERT + 8;
            setRotateVert((byte) (-(i * 7)));
        }
    }
    // </editor-fold>

    // <editor-fold desc="NBT and Display Settings">
    @Override
    protected void saveDisplaySettings(NBTTagCompound nbt) {
        nbt.setTag("dSettings1", serializeSlotSettings(SLOT_CARD1));
        nbt.setTag("dSettings2", serializeSlotSettings(SLOT_CARD2));
        nbt.setTag("dSettings3", serializeSlotSettings(SLOT_CARD3));
        nbt.setByte("rotateHor", rotateHor);
        nbt.setByte("rotateVert", rotateVert);
        nbt.setByte("thickness", thickness);
        nbt.setByte("powerMode", powerMode);
        nbt.setByte("transparencyMode", transparencyMode);
        nbt.setByte("textRotation", textRotation);
    }

    @Override
    protected void readDisplaySettings(NBTTagCompound nbt) {
        deserializeDisplaySettings(nbt, "dSettings1", SLOT_CARD1);
        deserializeDisplaySettings(nbt, "dSettings2", SLOT_CARD2);
        deserializeDisplaySettings(nbt, "dSettings3", SLOT_CARD3);
        rotateHor = nbt.getByte("rotateHor");
        rotateVert = nbt.getByte("rotateVert");
        thickness = nbt.getByte("thickness");
        powerMode = nbt.getByte("powerMode");
        transparencyMode = nbt.getByte("transparencyMode");
        textRotation = nbt.getByte("textRotation");
    }

    @Override
    public void readDisplaySettingsFromCard(ItemStack item) {
        NBTTagCompound nbt = item.getTagCompound();
        setDeserializedDisplaySettings(nbt, "dSettings1", SLOT_CARD1);
        setDeserializedDisplaySettings(nbt, "dSettings2", SLOT_CARD2);
        setDeserializedDisplaySettings(nbt, "dSettings3", SLOT_CARD3);
        setDeserializedDisplaySettings(nbt, "dSettings", SLOT_CARD1); // Compatibility

        if (nbt.hasKey("rotateHor")) {
            setRotateHor(nbt.getByte("rotateHor"));
            setRotateVert(nbt.getByte("rotateVert"));
            setThickness(nbt.getByte("thickness"));
            setPowerMode(nbt.getByte("powerMode"));
            setTransparencyMode(nbt.getByte("transparencyMode"));
            setTextRotation(nbt.getByte("textRotation"));
            setColorText(nbt.getInteger("colorText"));
            setColorBackground(nbt.getInteger("colorBackground"));
        }
    }

    @Override
    protected void postReadFromNBT() {
        if (inventory[SLOT_CARD1] != null) card = inventory[SLOT_CARD1];
        if (inventory[SLOT_CARD2] != null) card2 = inventory[SLOT_CARD2];
        if (inventory[SLOT_CARD3] != null) card3 = inventory[SLOT_CARD3];
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        writeDataSortersToNBT(nbt);
    }

    private NBTTagList getDataSorterForSlot(byte slot) {
        NBTTagList settingsList = new NBTTagList();
        if (dataSorters.containsKey(slot)) {
            for (Map.Entry<UUID, DataSorter> item : dataSorters.get(slot).entrySet()) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setString("key", item.getKey().toString());
                compound.setIntArray("value", item.getValue().getArray());
                settingsList.appendTag(compound);
            }
        }
        return settingsList;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        readDataSortersFromNBT(nbt);
    }

    public void writeDataSortersToNBT(NBTTagCompound nbt) {
        NBTTagCompound settingsList = new NBTTagCompound();
        settingsList.setTag(String.valueOf(SLOT_CARD1), getDataSorterForSlot(SLOT_CARD1));
        settingsList.setTag(String.valueOf(SLOT_CARD2), getDataSorterForSlot(SLOT_CARD2));
        settingsList.setTag(String.valueOf(SLOT_CARD3), getDataSorterForSlot(SLOT_CARD3));
        nbt.setTag("dataSorters", settingsList);
    }

    public void readDataSortersFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("dataSorters")) {
            NBTTagCompound settingsList = nbt.getCompoundTag("dataSorters");
            dataSorters.put(
                    SLOT_CARD1,
                    deserializeDataSorter(
                            settingsList.getTagList(String.valueOf(SLOT_CARD1), Constants.NBT.TAG_COMPOUND)));
            dataSorters.put(
                    SLOT_CARD2,
                    deserializeDataSorter(
                            settingsList.getTagList(String.valueOf(SLOT_CARD2), Constants.NBT.TAG_COMPOUND)));
            dataSorters.put(
                    SLOT_CARD3,
                    deserializeDataSorter(
                            settingsList.getTagList(String.valueOf(SLOT_CARD3), Constants.NBT.TAG_COMPOUND)));
        }
    }

    private Map<UUID, DataSorter> deserializeDataSorter(NBTTagList dataSorters) {
        Map<UUID, DataSorter> rv = new HashMap<>();
        for (int i = 0; i < dataSorters.tagCount(); i++) {
            NBTTagCompound compound = dataSorters.getCompoundTagAt(i);
            rv.put(UUID.fromString(compound.getString("key")), new DataSorter(compound.getIntArray("value")));
        }
        return rv;
    }

    /**
     * get a sorted list of PanelStrings to display on the screen
     *
     * @param settings  displaySettings of the screen, used as a bitmask
     * @param cardStack ItemStack that contains the card
     * @param helper    Wrapper object, to access field values.
     * @return a list of PanelStrings to display
     */
    public List<PanelString> getSortedCardData(DisplaySettingHelper settings, ItemStack cardStack,
            CardWrapperImpl helper) {
        List<PanelString> data = new ArrayList<>(this.getCardData(settings, cardStack, helper));
        List<PanelString> all_data = new ArrayList<>(
                this.getCardData(new DisplaySettingHelper(true), cardStack, helper));
        if (!Objects.equals(helper.getTitle(), "")) {
            PanelString title = data.remove(0);
            all_data.remove(0);
            getDataSorter(getIndexOfCard(cardStack)).sortListByPrefix(data, all_data);
            data.add(0, title);
        } else {
            getDataSorter(getIndexOfCard(cardStack)).sortListByPrefix(data, all_data);
        }
        return data;
    }

    // </editor-fold>

    // <editor-fold desc="Miscellaneous">
    @Override
    protected boolean isColoredEval() {
        return true;
    }

    @Override
    public ItemStack getWrenchDrop(EntityPlayer entityPlayer) {
        return new ItemStack(IC2NuclearControl.blockNuclearControlMain, 1, BlockDamages.DAMAGE_ADVANCED_PANEL);
    }

    public Map<Byte, Map<UUID, DataSorter>> getDataSorters() {
        if (dataSorters == null) {
            return new HashMap<>();
        }
        return dataSorters;
    }

    public DataSorter getDataSorter(byte slot) {
        UUID uuid = ((IPanelDataSource) getStackInSlot(slot).getItem()).getCardType();
        if (dataSorters.containsKey(slot)) {
            if (!dataSorters.get(slot).containsKey(uuid)) {
                dataSorters.get(slot).put(uuid, new DataSorter());
            }
        } else {
            Map<UUID, DataSorter> newMap = new HashMap<>();
            newMap.put(uuid, new DataSorter());
            dataSorters.put(slot, newMap);
        }
        return dataSorters.get(slot).get(uuid);
    }

    public void setDataSorter(byte slot, DataSorter sorter, boolean sendToServer) {
        UUID uuid = ((IPanelDataSource) getStackInSlot(slot).getItem()).getCardType();
        if (!dataSorters.containsKey(slot)) {
            Map<UUID, DataSorter> newMap = new HashMap<>();
            newMap.put(uuid, sorter);
            dataSorters.put(slot, newMap);
        } else {
            dataSorters.get(slot).put(uuid, sorter);
        }

        if (sendToServer) {
            NuclearNetworkHelper.sendDataSorterSync(this);
        }
    }
    // </editor-fold>
}
