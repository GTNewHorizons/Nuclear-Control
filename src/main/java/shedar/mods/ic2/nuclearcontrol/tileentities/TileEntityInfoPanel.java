package shedar.mods.ic2.nuclearcontrol.tileentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Facing;
import net.minecraftforge.common.util.Constants;

import cpw.mods.fml.common.FMLCommonHandler;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import shedar.mods.ic2.nuclearcontrol.IC2NuclearControl;
import shedar.mods.ic2.nuclearcontrol.IRedstoneConsumer;
import shedar.mods.ic2.nuclearcontrol.IRotation;
import shedar.mods.ic2.nuclearcontrol.IScreenPart;
import shedar.mods.ic2.nuclearcontrol.ISlotItemFilter;
import shedar.mods.ic2.nuclearcontrol.ITextureHelper;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IInventoryListener;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.IPanelMultiCard;
import shedar.mods.ic2.nuclearcontrol.api.IRemoteSensor;
import shedar.mods.ic2.nuclearcontrol.api.ITEInventoryHolder;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.blocks.subblocks.InfoPanel;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardBase;
import shedar.mods.ic2.nuclearcontrol.items.ItemUpgrade;
import shedar.mods.ic2.nuclearcontrol.panel.Screen;
import shedar.mods.ic2.nuclearcontrol.utils.BlockDamages;
import shedar.mods.ic2.nuclearcontrol.utils.CardAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.CardCache;
import shedar.mods.ic2.nuclearcontrol.utils.ColorUtil;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;
import shedar.mods.ic2.nuclearcontrol.utils.RedstoneHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class TileEntityInfoPanel extends TileEntity implements ISlotItemFilter, INetworkDataProvider,
        INetworkUpdateListener, INetworkClientTileEntityEventListener, IWrenchable, IRedstoneConsumer, ITextureHelper,
        IScreenPart, ISidedInventory, IRotation, IInventory, IInventoryListener, ITEInventoryHolder {

    private static final int[] COLORS_HEX = { 0x000000, 0xe93535, 0x82e306, 0x702b14, 0x1f3ce7, 0x8f1fea, 0x1fd7e9,
            0xcbcbcb, 0x222222, 0xe60675, 0x1fe723, 0xe9cc1f, 0x06aee4, 0xb006e3, 0xe7761f };

    public static final int I_PANEL_BACKGROUND = 6;

    public static final int DISPLAY_DEFAULT = Integer.MAX_VALUE;

    private static final byte SLOT_CARD = 0;
    private static final byte SLOT_UPGRADE_RANGE = 1;
    private static final byte SLOT_UPGRADE_COLOR = 2;
    private static final byte SLOT_UPGRADE_WEB = 2;
    private static final byte LOCATION_RANGE = 8;

    private static final int[] HOPPER_SLOTS = new int[] { 0 };

    private static final int MAX_RANGE_UPGRADE = 7;

    private final boolean isServerSide = FMLCommonHandler.instance().getEffectiveSide().isServer();

    public boolean init;
    public TileEntityInventory inventory;
    public NBTTagCompound screenData;
    protected Screen screen;
    protected ItemStack card;

    private boolean prevPowered;
    protected boolean powered;

    protected boolean isWeb = false;
    protected boolean colored;
    protected int rangeModifiers;

    protected final Map<Byte, Map<UUID, DisplaySettingHelper>> displaySettings;

    private int prevRotation;
    public int rotation;

    private boolean prevShowLabels;
    public boolean showLabels;

    private short prevFacing;
    public short facing;

    private int prevColorBackground;
    public int colorBackground;

    private int prevColorText;
    public int colorText;

    public final CardCache cardCache = new CardCache();
    private final Set<String> nbtFields = new HashSet<>();

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        Screen screen = getScreen();
        return screen == null ? super.getRenderBoundingBox() : screen.getBoundingBox();
    }

    public List<PanelString> getDisplayedData() {
        return cardCache.getCachedStrings();
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
        if (init && prevFacing != f) {
            if (isServerSide && !init) {
                IC2NuclearControl.instance.screenManager.unregisterScreenPart(this);
                IC2NuclearControl.instance.screenManager.registerInfoPanel(this);
            }

            ((NetworkManager) IC2.network.get()).updateTileEntityField(this, "facing");
        }
        prevFacing = f;
    }

    @Override
    public void setPowered(boolean p) {
        if (powered == p) return;
        powered = p;
        if (worldObj == null) return;
        IC2.network.get().updateTileEntityField(this, "powered");
    }

    @Override
    public boolean getPowered() {
        return powered;
    }

    public void setColored(boolean c) {
        if (colored == c) return;
        colored = c;
        if (worldObj == null) return;
        IC2.network.get().updateTileEntityField(this, "colored");
    }

    public boolean getColored() {
        return colored;
    }

    public byte getTextRotation() {
        return 0;
    }

    public void setIsWeb(boolean c) {
        if (isWeb == c) return;
        isWeb = c;
        if (worldObj == null) return;
        IC2.network.get().updateTileEntityField(this, "isWeb");
    }

    public boolean getIsWeb() {
        return isWeb;
    }

    public void setColorBackground(int c) {
        c &= 0xf;
        colorBackground = c;
        if (prevColorBackground != c) {
            IC2.network.get().updateTileEntityField(this, "colorBackground");
        }
        prevColorBackground = colorBackground;
    }

    public int getColorBackground() {
        return colorBackground;
    }

    public void setColorText(int c) {
        c &= 0xf;
        colorText = c;
        if (prevColorText != c) {
            IC2.network.get().updateTileEntityField(this, "colorText");
        }
        prevColorText = colorText;
    }

    public int getColorText() {
        return colorText;
    }

    public int getColorTextHex() {
        return COLORS_HEX[colorText];
    }

    public void setShowLabels(boolean p) {
        showLabels = p;
        if (prevShowLabels != p) {
            IC2.network.get().updateTileEntityField(this, "showLabels");
        }
        prevShowLabels = showLabels;
    }

    public boolean getShowLabels() {
        return showLabels;
    }

    protected boolean isCardSlot(int slot) {
        return slot == SLOT_CARD;
    }

    public void setDisplaySettings(byte slot, DisplaySettingHelper settings) {
        if (!isCardSlot(slot)) return;
        UUID cardType = null;
        IndexedItem<?> indexedItem = inventory.getIndexed(slot);
        if (indexedItem != null) {
            if (indexedItem.item instanceof IPanelMultiCard panelMultiCard) {
                cardType = panelMultiCard.getCardType(indexedItem);
            } else if (indexedItem.item instanceof IPanelDataSource panelDataSource) {
                cardType = panelDataSource.getCardType();
            }
        }
        if (cardType != null) {
            if (!displaySettings.containsKey(slot)) displaySettings.put(slot, new HashMap<>());
            displaySettings.get(slot).put(cardType, settings);
            if (isServerSide) {
                NuclearNetworkHelper.sendDisplaySettingsUpdate(this, slot, cardType, settings);
            }
        }
    }

    @Override
    public void onNetworkUpdate(String field) {
        if (field.equals("screenData")) {
            if (screen != null) {
                screen.destroy(true, worldObj);
            }
            if (screenData != null) {
                screen = IC2NuclearControl.instance.screenManager.loadScreen(this);
                if (screen != null) {
                    screen.init(true, worldObj);
                }
            }
        }

        if (field.equals("facing") && prevFacing != facing) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            prevFacing = facing;
        }

        if (field.equals("colorBackground") || field.equals("colored")) {
            if (screen != null) {
                screen.markUpdate(worldObj);
            } else {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
        if (field.equals("showLabels")) {
            prevShowLabels = showLabels;
        }
        if (field.equals("powered") && prevPowered != powered) {
            if (screen != null) {
                screen.turnPower(getPowered(), worldObj);
            } else {
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                worldObj.func_147451_t(xCoord, yCoord, zCoord);
            }
            prevPowered = powered;
        }
        if (field.equals("rotation") && prevRotation != rotation) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            prevRotation = rotation;
        }
    }

    @Override
    public void onNetworkEvent(EntityPlayer entityplayer, int i) {
        if (i == -1) {
            setShowLabels(true);
        } else if (i == -2) {
            setShowLabels(false);
        }
    }

    public TileEntityInfoPanel(int inventorySize) {
        super();
        inventory = new TileEntityInventory(inventorySize);
        inventory.addListener(this);
        inventory.setStackSizeLimit(SLOT_CARD, 1);
        inventory.setStackSizeLimit(SLOT_UPGRADE_COLOR, 1);
        inventory.setStackSizeLimit(SLOT_UPGRADE_WEB, 1);
        inventory.setStackSizeLimit(SLOT_UPGRADE_RANGE, MAX_RANGE_UPGRADE);

        screen = null;
        card = null;
        init = false;
        displaySettings = new HashMap<>(1);
        displaySettings.put((byte) 0, new HashMap<>());
        powered = false;
        prevPowered = false;
        facing = 0;
        prevFacing = 0;
        prevRotation = 0;
        rotation = 0;
        showLabels = true;
        colored = false;
        colorBackground = ColorUtil.COLOR_GREEN;
    }

    public TileEntityInfoPanel() {
        this(3);// card + range upgrade + color/web upgrade
    }

    @Override
    public List<String> getNetworkedFields() {
        return new ArrayList<>(
                Arrays.asList(
                        "powered",
                        "facing",
                        "rotation",
                        "card",
                        "showLabels",
                        "colorBackground",
                        "colorText",
                        "colored",
                        "screenData",
                        "isWeb"));
    }

    protected void initData() {
        init = true;
        if (!isServerSide) return;

        NuclearNetworkHelper.requestDisplaySettings(this);
        if (!nbtFields.contains("powered")) RedstoneHelper.checkPowered(worldObj, this);

        IC2.network.get().updateTileEntityField(this, "facing");
        if (screenData == null) {
            IC2NuclearControl.instance.screenManager.registerInfoPanel(this);
        } else {
            screen = IC2NuclearControl.instance.screenManager.loadScreen(this);
            if (screen != null) {
                screen.init(true, worldObj);
            }
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!init) {
            initData();
        }
        if (!getPowered()) return;
        if (worldObj.getTotalWorldTime() % IC2NuclearControl.instance.screenRefreshPeriod != 0) return;

        boolean isServer = !worldObj.isRemote;
        if (isServer) rangeModifiers = computeRangeModifiers();
        int range = getMaxRange();

        List<IndexedItem<IPanelDataSource>> cards = getCards();
        for (IndexedItem<IPanelDataSource> card : cards) {
            NBTCardLayout layout = cardCache.getLayout(card);
            if (isServer) {
                boolean isValid = checkCardValidity(card, layout);
                if (isValid) layout.setState(card.item.update(this, card, layout, range));
            }
            if (cardCache.update(card, this::getNewStringData)) {
                if (isServer) {
                    NuclearNetworkHelper.sendItemSyncPacket(this, (byte) card.slot, card.itemStack);
                    markDirty();
                } else {
                    NuclearNetworkHelper.sendItemUpdatedPacket(this, (byte) card.slot, card.itemStack);
                }
            }
        }
    }

    protected void deserializeDisplaySettings(NBTTagCompound nbttagcompound, String tagName, byte slot) {
        if (nbttagcompound.hasKey(tagName)) {
            NBTTagList settingsList = nbttagcompound.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < settingsList.tagCount(); i++) {
                NBTTagCompound compound = settingsList.getCompoundTagAt(i);
                try {
                    UUID key = UUID.fromString(compound.getString("key"));
                    String value = compound.getString("value");
                    getDisplaySettingsForSlot(slot).put(key, new DisplaySettingHelper(value));
                } catch (IllegalArgumentException e) {
                    IC2NuclearControl.logger.warn("Invalid display settings for Information Panel");
                }
            }
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public TileEntityInventory getInventory() {
        return inventory;
    }

    // Same as deserializeDisplaySettings, but uses setDisplaySettings
    // to ensure values are properly updated
    protected void setDeserializedDisplaySettings(NBTTagCompound nbttagcompound, String tagName, byte slot) {
        if (nbttagcompound.hasKey(tagName)) {
            NBTTagList settingsList = nbttagcompound.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < settingsList.tagCount(); i++) {
                NBTTagCompound compound = settingsList.getCompoundTagAt(i);
                try {
                    String value = compound.getString("value");
                    setDisplaySettings(slot, new DisplaySettingHelper(value));
                } catch (IllegalArgumentException e) {
                    IC2NuclearControl.logger.warn("Invalid display settings for Information Panel");
                }
            }
        }
    }

    protected void readDisplaySettings(NBTTagCompound nbttagcompound) {
        deserializeDisplaySettings(nbttagcompound, "dSettings", SLOT_CARD);
    }

    public void readDisplaySettingsFromCard(ItemStack item) {
        NBTTagCompound nbt = item.getTagCompound();
        setDeserializedDisplaySettings(nbt, "dSettings", SLOT_CARD);

        // Compat for settings for one card from advanced panel
        setDeserializedDisplaySettings(nbt, "dSettings1", SLOT_CARD);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);

        if (nbttagcompound.hasKey("rotation")) {
            nbtFields.add("rotation");
            prevRotation = rotation = nbttagcompound.getInteger("rotation");
        }
        if (nbttagcompound.hasKey("showLabels")) {
            nbtFields.add("showLabels");
            prevShowLabels = showLabels = nbttagcompound.getBoolean("showLabels");
        } else {
            // v.1.1.11 compatibility
            prevShowLabels = showLabels = true;
        }
        prevFacing = facing = nbttagcompound.getShort("facing");
        powered = nbttagcompound.getBoolean("powered");

        if (nbttagcompound.hasKey("colorBackground")) {
            nbtFields.add("colorBackground");
            colorText = nbttagcompound.getInteger("colorText");
            colorBackground = nbttagcompound.getInteger("colorBackground");
        } else {
            // 1.4.1 compatibility
            colorText = 0;
            colorBackground = ColorUtil.COLOR_GREEN;
        }

        if (nbttagcompound.hasKey("powered")) {
            nbtFields.add("powered");
            powered = nbttagcompound.getBoolean("powered");
        }

        if (nbttagcompound.hasKey("screenData")) {
            nbtFields.add("screenData");
            screenData = (NBTTagCompound) nbttagcompound.getTag("screenData");
        }
        readDisplaySettings(nbttagcompound);

        NBTTagList nbttaglist = nbttagcompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        inventory.load(nbttaglist);
    }

    @Override
    public void invalidate() {
        if (isServerSide) {
            IC2NuclearControl.instance.screenManager.unregisterScreenPart(this);
        }
        super.invalidate();
    }

    protected NBTTagList serializeSlotSettings(byte slot) {
        NBTTagList settingsList = new NBTTagList();
        for (Map.Entry<UUID, DisplaySettingHelper> item : getDisplaySettingsForSlot(slot).entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("key", item.getKey().toString());
            compound.setString("value", item.getValue().toString());
            settingsList.appendTag(compound);
        }
        return settingsList;
    }

    protected void saveDisplaySettings(NBTTagCompound nbttagcompound) {
        nbttagcompound.setTag("dSettings", serializeSlotSettings(SLOT_CARD));
    }

    public void saveDisplaySettingsToCard(ItemStack item) {
        NBTTagCompound nbt = new NBTTagCompound();
        this.saveDisplaySettings(nbt);
        nbt.setInteger("colorText", colorText);
        nbt.setInteger("colorBackground", colorBackground);
        item.setTagCompound(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setShort("facing", facing);

        nbttagcompound.setInteger("rotation", rotation);
        nbttagcompound.setBoolean("showLabels", getShowLabels());

        nbttagcompound.setInteger("colorBackground", colorBackground);
        nbttagcompound.setInteger("colorText", colorText);
        nbttagcompound.setBoolean("powered", powered);

        saveDisplaySettings(nbttagcompound);

        if (screen != null) {
            screenData = screen.toTag();
            nbttagcompound.setTag("screenData", screenData);
        }

        nbttagcompound.setTag("Items", inventory.getNBT());
    }

    @Override
    public int getSizeInventory() {
        return inventory.capacity;
    }

    @Override
    public ItemStack getStackInSlot(int slotNum) {
        return inventory.get(slotNum);
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int amount) {
        ItemStack removed = inventory.removeFromStack(slotNum, amount);
        if (removed != null) {
            onItemInventoryUpdate(slotNum, removed, inventory.get(slotNum) == null);
            if (this.isServerSide) NuclearNetworkHelper.sendItemSyncPacket(this, (byte) slotNum, null);
        }
        return removed;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack itemStack) {
        if (itemStack == null) {
            ItemStack old = inventory.remove(slotNum);
            if (old != null) {
                onItemInventoryUpdate(slotNum, old, true);
            }
        } else {
            inventory.set(slotNum, itemStack);
        }

        markDirty();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return HOPPER_SLOTS;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack p_102008_2_, int side) {
        return slot == SLOT_CARD;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack item, int side) {
        return slot == SLOT_CARD && item.getItem() instanceof ItemCardBase;
    }

    @Override
    public String getInventoryName() {
        return "block.StatusDisplay";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public void onItemAdded(int slot, ItemStack item) {
        onItemInventoryUpdate(slot, item, false);
    }

    @Override
    public void onItemRemoved(int slot, ItemStack item) {
        onItemInventoryUpdate(slot, item, true);
    }

    protected void onItemInventoryUpdate(int slot, ItemStack item, boolean removed) {
        cardCache.clear(slot, removed);
        if (!isServerSide) return;
        checkColorUpgrade(item, removed);
        checkWebUpgrade(item, removed);
        checkRangeUpgrade(item, removed);
    }

    protected void checkColorUpgrade(ItemStack item, boolean removed) {
        if (isUpgrade(item, ItemUpgrade.DAMAGE_COLOR)) {
            setColored(!removed);
        }
    }

    protected void checkWebUpgrade(ItemStack item, boolean removed) {
        if (isUpgrade(item, ItemUpgrade.DAMAGE_WEB)) {
            setIsWeb(!removed);
        }
    }

    protected void checkRangeUpgrade(ItemStack item, boolean removed) {
        if (isUpgrade(item, ItemUpgrade.DAMAGE_RANGE)) {
            rangeModifiers = removed ? 0 : Math.min(item.stackSize, MAX_RANGE_UPGRADE);
        }
    }

    protected int computeRangeModifiers() {
        for (int i = 0; i < inventory.capacity; i++) {
            ItemStack stack = inventory.get(i);
            if (stack != null && isUpgrade(stack, ItemUpgrade.DAMAGE_RANGE)) {
                return Math.min(stack.stackSize, MAX_RANGE_UPGRADE);
            }
        }
        return 0;
    }

    protected boolean isUpgrade(ItemStack item, int damage) {
        return item != null && item.getItem() instanceof ItemUpgrade && item.getItemDamage() == damage;
    }

    public List<IndexedItem<IPanelDataSource>> getCards() {
        return inventory.getItems(IPanelDataSource.class);
    }

    public byte getIndexOfCard(Object card) {
        if (card == null) {
            return 0;
        }
        byte slot = 0;
        for (byte i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack != null && stack.equals(card)) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private int getMaxRange() {
        return LOCATION_RANGE * (int) Math.pow(2, MAX_RANGE_UPGRADE);
    }

    private boolean checkCardValidity(IndexedItem<IPanelDataSource> card, NBTCardLayout layout) {
        IPanelDataSource cardItem = card.item;
        if (!(cardItem instanceof IRemoteSensor)) return true;
        int range = getMaxRange();

        ChunkCoordinates target = layout.getTarget();
        if (target == null) {
            layout.setState(CardState.INVALID_CARD);
            return false;
        } else {
            int dx = target.posX - xCoord;
            int dz = target.posZ - zCoord;
            if (Math.abs(dx) > range || Math.abs(dz) > range) {
                layout.setState(CardState.OUT_OF_RANGE);
                return false;
            }
        }

        return true;
    }

    protected List<PanelString> getNewStringData(IndexedItem<IPanelDataSource> card) {
        CardState state = CardAccessors.getState(card);
        if (state != null && state != CardState.OK) {
            return StringUtils.getStateMessage(state);
        }

        NBTCardLayout layout = cardCache.getLayout(card);
        List<PanelString> data = new ArrayList<>(
                card.item.getStringData(getNewDisplaySettingsByCard(card), card, layout, getShowLabels()));
        String title = layout.title.get();
        if (!title.equals("")) data.add(0, new PanelString(title));
        return data;
    }

    @Override
    public boolean isItemValid(int slotIndex, ItemStack itemstack) {
        switch (slotIndex) {
            case SLOT_CARD:
                return itemstack.getItem() instanceof IPanelDataSource;
            case SLOT_UPGRADE_RANGE:
                return itemstack.getItem() instanceof ItemUpgrade
                        && itemstack.getItemDamage() == ItemUpgrade.DAMAGE_RANGE;
            case SLOT_UPGRADE_COLOR:
                return itemstack.getItem() instanceof ItemUpgrade
                        && (itemstack.getItemDamage() == ItemUpgrade.DAMAGE_COLOR
                                || itemstack.getItemDamage() == ItemUpgrade.DAMAGE_WEB);
            default:
                return false;
        }

    }

    @Override
    public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int face) {
        return !entityPlayer.isSneaking() && getFacing() != face;
    }

    @Override
    public float getWrenchDropRate() {
        return 1;
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer entityPlayer) {
        return !entityPlayer.isSneaking();
    }

    public int modifyTextureIndex(int texture, int x, int y, int z) {
        if (texture != InfoPanel.I_COLOR_DEFAULT) {
            return texture;
        }
        texture = I_PANEL_BACKGROUND;

        texture = texture + colorBackground * 16;

        if (getPowered()) {
            texture += 240;
        }

        return texture;
    }

    @Override
    public int modifyTextureIndex(int texture) {
        return modifyTextureIndex(texture, xCoord, yCoord, zCoord);
    }

    @Override
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    public Screen getScreen() {
        return screen;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + xCoord;
        result = prime * result + yCoord;
        result = prime * result + zCoord;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TileEntityInfoPanel other = (TileEntityInfoPanel) obj;
        if (xCoord != other.xCoord) return false;
        if (yCoord != other.yCoord) return false;
        if (zCoord != other.zCoord) return false;
        if (worldObj != other.worldObj) return false;
        return true;
    }

    @Override
    public void rotate() {
        int r;
        switch (rotation) {
            case 0:
                r = 1;
                break;
            case 1:
                r = 3;
                break;
            case 3:
                r = 2;
                break;
            case 2:
                r = 0;
                break;
            default:
                r = 0;
                break;
        }
        setRotation(r);
    }

    @Override
    public int getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(int value) {
        rotation = value;
        if (rotation != prevRotation) {
            // NetworkHelper.updateTileEntityField(this, "rotation");
            IC2.network.get().updateTileEntityField(this, "rotation");
        }
        prevRotation = rotation;
    }

    public Map<Byte, Map<UUID, DisplaySettingHelper>> getDisplaySettings() {
        return displaySettings;
    }

    public Map<UUID, DisplaySettingHelper> getDisplaySettingsForSlot(byte slot) {
        if (!displaySettings.containsKey(slot)) {
            displaySettings.put(slot, new HashMap<>());
        }
        return displaySettings.get(slot);
    }

    public DisplaySettingHelper getNewDisplaySettingsForCardInSlot(int slot) {
        return getNewDisplaySettingsByCard(inventory.getIndexed(slot));
    }

    public DisplaySettingHelper getNewDisplaySettingsByCard(IndexedItem<?> card) {
        byte slot = (byte) card.slot;
        if (!displaySettings.containsKey(slot)) {
            return new DisplaySettingHelper();
        }
        UUID cardType = null;
        if (card.item instanceof IPanelMultiCard panelMultiCard) {
            cardType = panelMultiCard.getCardType(card);
        } else if (card.item instanceof IPanelDataSource panelDataSource) {
            cardType = panelDataSource.getCardType();
        }
        if (displaySettings.get(slot).containsKey(cardType)) {
            return displaySettings.get(slot).get(cardType);
        }
        return new DisplaySettingHelper();
    }

    @Override
    public ItemStack getWrenchDrop(EntityPlayer entityPlayer) {
        return new ItemStack(IC2NuclearControl.blockNuclearControlMain, 1, BlockDamages.DAMAGE_INFO_PANEL);
    }

    @Override
    public void updateData() {
        if (!isServerSide) {
            return;
        }
        if (screen == null) {
            screenData = null;
        } else {
            screenData = screen.toTag();
        }
        IC2.network.get().updateTileEntityField(this, "screenData");
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
        return isItemValid(slot, itemstack);
    }
}
