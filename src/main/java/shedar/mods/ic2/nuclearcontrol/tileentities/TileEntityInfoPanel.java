package shedar.mods.ic2.nuclearcontrol.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
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
import shedar.mods.ic2.nuclearcontrol.api.ICardWrapper;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.IPanelMultiCard;
import shedar.mods.ic2.nuclearcontrol.api.IRemoteSensor;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.blocks.subblocks.InfoPanel;
import shedar.mods.ic2.nuclearcontrol.items.ItemUpgrade;
import shedar.mods.ic2.nuclearcontrol.panel.CardWrapperImpl;
import shedar.mods.ic2.nuclearcontrol.panel.Screen;
import shedar.mods.ic2.nuclearcontrol.utils.BlockDamages;
import shedar.mods.ic2.nuclearcontrol.utils.ColorUtil;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearNetworkHelper;
import shedar.mods.ic2.nuclearcontrol.utils.RedstoneHelper;

public class TileEntityInfoPanel extends TileEntity
        implements ISlotItemFilter, INetworkDataProvider, INetworkUpdateListener, INetworkClientTileEntityEventListener,
        IWrenchable, IRedstoneConsumer, ITextureHelper, IScreenPart, /* ISidedInventory, */IRotation, IInventory {

    private static final int[] COLORS_HEX = { 0x000000, 0xe93535, 0x82e306, 0x702b14, 0x1f3ce7, 0x8f1fea, 0x1fd7e9,
            0xcbcbcb, 0x222222, 0xe60675, 0x1fe723, 0xe9cc1f, 0x06aee4, 0xb006e3, 0xe7761f };

    public static final int I_PANEL_BACKGROUND = 6;

    public static final int DISPLAY_DEFAULT = Integer.MAX_VALUE;

    private static final byte SLOT_CARD = 0;
    private static final byte SLOT_UPGRADE_RANGE = 1;
    private static final byte SLOT_UPGRADE_COLOR = 2;
    private static final byte SLOT_UPGRADE_WEB = 2;
    private static final byte LOCATION_RANGE = 8;

    protected int updateTicker;
    protected int dataTicker;
    protected int tickRate;
    protected int dt;
    protected int updatedataTicker;
    public boolean init;
    public ItemStack inventory[];
    public NBTTagCompound screenData;
    protected Screen screen;
    protected ItemStack card;

    private boolean prevPowered;
    protected boolean powered;

    private boolean prevIsWeb = false;
    protected boolean isWeb = false;

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

    private boolean prevColored;
    public boolean colored;

    private final Map<Integer, List<PanelString>> cardData;

    @Override
    public short getFacing() {
        return (short) Facing.oppositeSide[facing];
    }

    @Override
    public void setFacing(short f) {
        setSide((short) Facing.oppositeSide[f]);
    }

    private void setCard(ItemStack value) {
        card = value;
        // IC2.network.get().updateTileEntityField(this, "card");
        // ((NetworkManager)IC2.network.get()).updateTileEntityField(this, "card");
    }

    private void setSide(short f) {
        facing = f;
        if (init && prevFacing != f) {
            if (FMLCommonHandler.instance().getEffectiveSide().isServer() && !init) {
                IC2NuclearControl.instance.screenManager.unregisterScreenPart(this);
                IC2NuclearControl.instance.screenManager.registerInfoPanel(this);
            }

            ((NetworkManager) IC2.network.get()).updateTileEntityField(this, "facing");
        }
        prevFacing = f;
    }

    @Override
    public void setPowered(boolean p) {
        powered = p;
        if (prevPowered != p) {
            IC2.network.get().updateTileEntityField(this, "powered");
        }
        prevPowered = powered;
    }

    @Override
    public boolean getPowered() {
        return powered;
    }

    public void setColored(boolean c) {
        colored = c;
        if (prevColored != c) {
            IC2.network.get().updateTileEntityField(this, "colored");
        }
        prevColored = colored;
    }

    public boolean getColored() {
        return colored;
    }

    public byte getTextRotation() {
        return 0;
    }

    public void setIsWeb(boolean c) {
        isWeb = c;
        if (prevIsWeb != c) {
            IC2.network.get().updateTileEntityField(this, "isWeb");
        }
        prevIsWeb = isWeb;
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
        ItemStack stack = inventory[slot];
        if (stack != null) {
            if (stack.getItem() instanceof IPanelMultiCard) {
                cardType = ((IPanelMultiCard) stack.getItem()).getCardType(new CardWrapperImpl(stack, slot));
            } else if (stack.getItem() instanceof IPanelDataSource) {
                cardType = ((IPanelDataSource) inventory[slot].getItem()).getCardType();
            }
        }
        if (cardType != null) {
            if (!displaySettings.containsKey(slot)) displaySettings.put(slot, new HashMap<>());
            displaySettings.get(slot).put(cardType, settings);
            if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
                NuclearNetworkHelper.sendDisplaySettingsUpdate(this, slot, cardType, settings);
            }
        }
    }

    @Override
    public void onNetworkUpdate(String field) {
        if (field.equals("screenData")) {
            if (screen != null && FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
            prevColored = colored;
            prevColorBackground = colorBackground;
        }
        if (field.equals("card")) {
            inventory[SLOT_CARD] = card;
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
        inventory = new ItemStack[inventorySize];
        screen = null;
        card = null;
        init = false;
        cardData = new HashMap<Integer, List<PanelString>>();
        tickRate = IC2NuclearControl.instance.screenRefreshPeriod;
        updateTicker = tickRate;
        dt = IC2NuclearControl.instance.dataRefreshPeriod;
        dataTicker = (dt > tickRate) ? tickRate : dt;
        updatedataTicker = dataTicker;
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
        List<String> list = new ArrayList<String>(9);
        list.add("powered");
        list.add("facing");
        list.add("rotation");
        list.add("card");
        list.add("showLabels");
        list.add("colorBackground");
        list.add("colorText");
        list.add("colored");
        list.add("screenData");
        list.add("isWeb");
        return list;
    }

    protected void initData() {
        if (worldObj.isRemote) {
            NuclearNetworkHelper.requestDisplaySettings(this);
        } else {
            RedstoneHelper.checkPowered(worldObj, this);
        }
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
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
        init = true;
    }

    public void resetCardData() {
        cardData.clear();
    }

    /**
     * get a list of PanelStrings to display on the screen
     *
     * @param settings  displaySettings of the screen, used as a bitmask
     * @param cardStack ItemStack that contains the card
     * @param helper    Wrapper object, to access field values.
     * @return a list of PanelStrings to display
     */
    public List<PanelString> getCardData(DisplaySettingHelper settings, ItemStack cardStack, ICardWrapper helper) {
        IPanelDataSource card = (IPanelDataSource) cardStack.getItem();
        int slot = getIndexOfCard(cardStack);
        resetCardData();
        List<PanelString> data = cardData.get(slot);
        if (data == null) {
            if (card != null) data = card.getStringData(settings, helper, getShowLabels());
            String title = helper.getTitle();
            if (data != null && title != null && !title.isEmpty()) {
                PanelString titleString = new PanelString();
                titleString.textCenter = title;
                data.add(0, titleString);
            }
            cardData.put(slot, data);
        }
        return data;
    }

    @Override
    public void updateEntity() {
        if (!init) {
            initData();
        }
        updatedataTicker--;
        if (updatedataTicker <= 0) {
            resetCardData();
            updatedataTicker = dataTicker;
        }
        if (!worldObj.isRemote) {
            if (updateTicker-- > 0) return;
            updateTicker = tickRate;
            markDirty();
        }
        super.updateEntity();
    }

    protected void postReadFromNBT() {
        if (inventory[SLOT_CARD] != null) {
            card = inventory[SLOT_CARD];
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
            prevRotation = rotation = nbttagcompound.getInteger("rotation");
        }
        if (nbttagcompound.hasKey("showLabels")) {
            prevShowLabels = showLabels = nbttagcompound.getBoolean("showLabels");
        } else {
            // v.1.1.11 compatibility
            prevShowLabels = showLabels = true;
        }
        prevFacing = facing = nbttagcompound.getShort("facing");

        if (nbttagcompound.hasKey("colorBackground")) {
            colorText = nbttagcompound.getInteger("colorText");
            colorBackground = nbttagcompound.getInteger("colorBackground");
        } else {
            // 1.4.1 compatibility
            colorText = 0;
            colorBackground = ColorUtil.COLOR_GREEN;
        }

        if (nbttagcompound.hasKey("screenData")) {
            screenData = (NBTTagCompound) nbttagcompound.getTag("screenData");
        }
        readDisplaySettings(nbttagcompound);

        NBTTagList nbttaglist = nbttagcompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound compound = nbttaglist.getCompoundTagAt(i);
            byte slotNum = compound.getByte("Slot");

            if (slotNum >= 0 && slotNum < inventory.length) {
                inventory[slotNum] = ItemStack.loadItemStackFromNBT(compound);
            }
        }
        postReadFromNBT();
        markDirty();
    }

    @Override
    public void invalidate() {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
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

        saveDisplaySettings(nbttagcompound);

        if (screen != null) {
            screenData = screen.toTag();
            nbttagcompound.setTag("screenData", screenData);
        }

        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(compound);
                nbttaglist.appendTag(compound);
            }
        }
        nbttagcompound.setTag("Items", nbttaglist);
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotNum) {
        return inventory[slotNum];
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int amount) {
        if (inventory[slotNum] != null) {
            if (inventory[slotNum].stackSize <= amount) {
                ItemStack itemStack = inventory[slotNum];
                inventory[slotNum] = null;
                if (slotNum == SLOT_CARD) {
                    setCard(null);
                }
                return itemStack;
            }

            ItemStack taken = inventory[slotNum].splitStack(amount);
            if (inventory[slotNum].stackSize == 0) {
                inventory[slotNum] = null;
                if (slotNum == SLOT_CARD) setCard(null);
            }
            return taken;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack itemStack) {
        inventory[slotNum] = itemStack;
        if (slotNum == SLOT_CARD) {
            setCard(itemStack);
        }

        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
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

    protected ItemStack getRangeUpgrade() {
        return inventory[SLOT_UPGRADE_RANGE];
    }

    protected boolean isColoredEval() {
        ItemStack itemStack = inventory[SLOT_UPGRADE_COLOR];
        return itemStack != null && itemStack.getItem() instanceof ItemUpgrade
                && itemStack.getItemDamage() == ItemUpgrade.DAMAGE_COLOR;
    }

    protected boolean isWebEval() {
        ItemStack itemStack = inventory[SLOT_UPGRADE_WEB];
        return itemStack != null && itemStack.getItem() instanceof ItemUpgrade
                && itemStack.getItemDamage() == ItemUpgrade.DAMAGE_WEB;
    }

    public int getCardSlotsCount() {
        return 1;
    }

    public List<ItemStack> getCards() {
        List<ItemStack> data = new ArrayList<>(1);
        data.add(inventory[SLOT_CARD]);
        return data;
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

    protected long getIdForCard(CardWrapperImpl cardHelper) {
        long id = cardHelper.getLong("_webSensorId");
        if (id <= 0) {
            /*
             * if (id <= -10) id += 10; if (id == 0) HttpCardSender.instance.requestId(); Long newId =
             * HttpCardSender.instance.availableIds.poll(); if (newId == null) id--; else id = newId;
             * cardHelper.setLong("_webSensorId", id);
             */
        }
        return id;
    }

    public void processCard(ItemStack card, int upgradeCountRange, int slot) {
        if (card == null) {
            return;
        }
        Item item = card.getItem();
        if (item instanceof IPanelDataSource) {
            boolean needUpdate = true;
            if (upgradeCountRange > 7) {
                upgradeCountRange = 7;
            }
            int range = LOCATION_RANGE * (int) Math.pow(2, upgradeCountRange);
            CardWrapperImpl cardHelper = new CardWrapperImpl(card, slot);

            if (isWeb) {
                /*
                 * long id = getIdForCard(cardHelper); if (id > 0) { UUID cardType = card.getItem() instanceof
                 * IPanelMultiCard ? ((IPanelMultiCard) card.getItem()).getCardType(cardHelper) : ((IPanelDataSource)
                 * card.getItem()).getCardType(); HttpCardSender.instance.add(ItemStackUtils.getTagCompound(card),
                 * cardType, id); }
                 */
            }

            if (item instanceof IRemoteSensor) {
                ChunkCoordinates target = cardHelper.getTarget();
                if (target == null) {
                    needUpdate = false;
                    cardHelper.setState(CardState.INVALID_CARD);
                } else {
                    int dx = target.posX - xCoord;
                    int dy = 0;// target.posY - yCoord;
                    int dz = target.posZ - zCoord;
                    if (Math.abs(dx) > range || Math.abs(dy) > range || Math.abs(dz) > range) {
                        needUpdate = false;
                        cardHelper.setState(CardState.OUT_OF_RANGE);
                    }
                }
            }
            if (needUpdate) {
                CardState state = ((IPanelDataSource) item).update(this, cardHelper, range);
                cardHelper.setInt("state", state.getIndex());
            }
            cardHelper.commit(this);
        }

    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (worldObj != null && FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            int upgradeCountRange = 0;
            setColored(isColoredEval());
            setIsWeb(isWebEval());
            ItemStack itemStack = getRangeUpgrade();
            if (itemStack != null && itemStack.getItem() instanceof ItemUpgrade
                    && itemStack.getItemDamage() == ItemUpgrade.DAMAGE_RANGE) {
                upgradeCountRange = itemStack.stackSize;
            }
            List<ItemStack> cards = getCards();
            for (ItemStack card : cards) {
                byte slot = getIndexOfCard(card);
                processCard(card, upgradeCountRange, slot);
            }
        }
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

    public int getDisplaySettingsForCardInSlot(int slot) {
        return getNewDisplaySettingsForCardInSlot(slot).getAsInteger();
    }

    public DisplaySettingHelper getNewDisplaySettingsForCardInSlot(int slot) {
        ItemStack card = inventory[slot];
        if (card == null) {
            return new DisplaySettingHelper();
        }
        return getNewDisplaySettingsByCard(card);
    }

    public DisplaySettingHelper getNewDisplaySettingsByCard(ItemStack card) {
        byte slot = getIndexOfCard(card);
        if (card == null) {
            return new DisplaySettingHelper();
        }
        if (!displaySettings.containsKey(slot)) {
            return new DisplaySettingHelper();
        }
        UUID cardType = null;
        if (card.getItem() instanceof IPanelMultiCard) {
            cardType = ((IPanelMultiCard) card.getItem()).getCardType(new CardWrapperImpl(card, 0));
        } else if (card.getItem() instanceof IPanelDataSource) {
            cardType = ((IPanelDataSource) card.getItem()).getCardType();
        }
        if (displaySettings.get(slot).containsKey(cardType)) {
            return displaySettings.get(slot).get(cardType);
        }
        return new DisplaySettingHelper();
    }

    public int getDisplaySettingsByCard(ItemStack card) {
        return getNewDisplaySettingsByCard(card).getAsInteger();
    }

    @Override
    public ItemStack getWrenchDrop(EntityPlayer entityPlayer) {
        return new ItemStack(IC2NuclearControl.blockNuclearControlMain, 1, BlockDamages.DAMAGE_INFO_PANEL);
    }

    @Override
    public void updateData() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
