package shedar.mods.ic2.nuclearcontrol.items;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTankInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.ICardWrapper;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.utils.CardAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.LiquidStorageHelper;
import shedar.mods.ic2.nuclearcontrol.utils.NBTAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class ItemCardLiquidArrayLocation extends ItemCardBase {

    public static final int DISPLAY_NAME = 1;
    public static final int DISPLAY_AMOUNT = 2;
    public static final int DISPLAY_FREE = 3;
    public static final int DISPLAY_CAPACITY = 4;
    public static final int DISPLAY_PERCENTAGE = 5;
    public static final int DISPLAY_EACH = 6;
    public static final int DISPLAY_TOTAL = 7;

    private static final int STATUS_NOT_FOUND = Integer.MIN_VALUE;
    private static final int STATUS_OUT_OF_RANGE = Integer.MIN_VALUE + 1;

    public static final UUID CARD_TYPE = new UUID(0, 3);

    public ItemCardLiquidArrayLocation() {
        super("cardLiquidArray");
    }

    @Override
    public NBTCardLayout getLayout() {
        return new LAData();
    }

    private int[] getCoordinates(NBTTagCompound compound) {
        int x = compound.getInteger("x");
        int y = compound.getInteger("y");
        int z = compound.getInteger("z");
        return new int[] { x, y, z };
    }

    public static int getCardCount(ICardWrapper card) {
        return card.getInt("cardCount");
    }

    public static void initArray(ItemStack card, Vector<ItemStack> cards) {
        LAData data = new LAData();
        data.setItem(new IndexedItem<>(0, card, card.getItem()));

        NBTTagList tagList = new NBTTagList();
        data.cards.set(tagList);

        int cardCount = 0;
        for (ItemStack subCard : cards) {
            ChunkCoordinates target = CardAccessors.getCoordinates(subCard);
            if (target == null) continue;
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("x", target.posX);
            compound.setInteger("y", target.posY);
            compound.setInteger("z", target.posZ);
            compound.setInteger("targetType", CardAccessors.getTargetType(subCard));
            tagList.appendTag(compound);
            cardCount++;
        }

        data.count.set(cardCount);
    }

    @Override
    public CardState update(TileEntity panel, IndexedItem<?> card, NBTCardLayout layout, int range) {
        LAData data = (LAData) layout;
        int cardCount = data.count.get();
        double totalAmount = 0.0;

        if (cardCount == 0) {
            return CardState.INVALID_CARD;
        } else {
            boolean foundAny = false;
            boolean outOfRange = false;
            int liquidId = 0;
            NBTTagList tagList = (NBTTagList) data.cards.get();
            for (int i = 0; i < cardCount; i++) {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                int[] coordinates = getCoordinates(tag);
                int dx = coordinates[0] - panel.xCoord;
                int dy = coordinates[1] - panel.yCoord;
                int dz = coordinates[2] - panel.zCoord;
                if (Math.abs(dx) <= range && Math.abs(dy) <= range && Math.abs(dz) <= range) {
                    FluidTankInfo storage = LiquidStorageHelper
                            .getStorageAt(panel.getWorldObj(), coordinates[0], coordinates[1], coordinates[2]);
                    if (storage != null) {
                        if (storage.fluid != null) {
                            totalAmount += storage.fluid.amount;
                            tag.setInteger("amount", (int) storage.fluid.amount);

                            if (storage.fluid.getFluidID() != 0 && storage.fluid.amount > 0) {
                                liquidId = storage.fluid.getFluidID();
                            }
                            if (liquidId == 0) tag.setString("name", LangHelper.translate("msg.nc.None"));
                            else tag.setString("name", FluidRegistry.getFluidName(storage.fluid));
                        }
                        tag.setInteger("capacity", storage.capacity);
                        foundAny = true;
                    } else {
                        tag.setInteger("amount", STATUS_NOT_FOUND);
                    }
                } else {
                    tag.setInteger("amount", STATUS_OUT_OF_RANGE);
                    outOfRange = true;
                }
            }
            data.energyL.set(totalAmount);
            if (!foundAny) {
                if (outOfRange) return CardState.OUT_OF_RANGE;
                else return CardState.NO_TARGET;
            }
            return CardState.OK;
        }
    }

    @Override
    public CardState update(World world, IndexedItem<?> item, NBTCardLayout layout, int range) {
        return CardState.CUSTOM_ERROR;
    }

    @Override
    public UUID getCardType() {
        return CARD_TYPE;
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> item,
            NBTCardLayout layout, boolean showLabels) {
        List<PanelString> result = new LinkedList<PanelString>();
        PanelString line;
        double totalAmount = 0;
        double totalCapacity = 0;
        boolean showEach = displaySettings.getSetting(DISPLAY_EACH);
        boolean showSummary = displaySettings.getSetting(DISPLAY_TOTAL);
        boolean showName = displaySettings.getSetting(DISPLAY_NAME);
        boolean showAmount = true;// displaySettings.getNewSetting(DISPLAY_AMOUNT) > 0;
        boolean showFree = displaySettings.getSetting(DISPLAY_FREE);
        boolean showCapacity = displaySettings.getSetting(DISPLAY_CAPACITY);
        boolean showPercentage = displaySettings.getSetting(DISPLAY_PERCENTAGE);

        LAData card = (LAData) layout;
        int cardCount = card.count.get();
        NBTTagList tagList = (NBTTagList) card.cards.get();

        for (int i = 0; i < cardCount; i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            int amount = tag.getInteger("amount");
            int capacity = tag.getInteger("capacity");
            boolean isOutOfRange = amount == STATUS_OUT_OF_RANGE;
            boolean isNotFound = amount == STATUS_NOT_FOUND;
            if (showSummary && !isOutOfRange && !isNotFound) {
                totalAmount += amount;
                totalCapacity += capacity;
            }

            if (showEach) {
                if (isOutOfRange) {
                    line = new PanelString();
                    line.textLeft = StringUtils.getFormattedKey("msg.nc.InfoPanelOutOfRangeN", i + 1);
                    result.add(line);
                } else if (isNotFound) {
                    line = new PanelString();
                    line.textLeft = StringUtils.getFormattedKey("msg.nc.InfoPanelNotFoundN", i + 1);
                    result.add(line);
                } else {
                    if (showName) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils
                                .getFormattedKey("msg.nc.InfoPanelLiquidNameN", i + 1, tag.getString("name"));
                        else line.textLeft = StringUtils.getFormatted("", amount, false);
                        result.add(line);
                    }
                    if (showAmount) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelLiquidN",
                                i + 1,
                                StringUtils.getFormatted("", amount, false));
                        else line.textLeft = StringUtils.getFormatted("", amount, false);
                        result.add(line);
                    }
                    if (showFree) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelLiquidFreeN",
                                i + 1,
                                StringUtils.getFormatted("", capacity - amount, false));
                        else line.textLeft = StringUtils.getFormatted("", capacity - amount, false);

                        result.add(line);
                    }
                    if (showCapacity) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelLiquidCapacityN",
                                i + 1,
                                StringUtils.getFormatted("", capacity, false));
                        else line.textLeft = StringUtils.getFormatted("", capacity, false);
                        result.add(line);
                    }
                    if (showPercentage) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelLiquidPercentageN",
                                i + 1,
                                StringUtils.getFormatted(
                                        "",
                                        capacity == 0 ? 100 : (((double) amount / capacity) * 100),
                                        false));
                        else line.textLeft = StringUtils
                                .getFormatted("", capacity == 0 ? 100 : (((double) amount / capacity) * 100), false);
                        result.add(line);
                    }
                }
            }
        }
        if (showSummary) {
            if (showAmount) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelLiquidAmount", totalAmount, showLabels);
                result.add(line);
            }
            if (showFree) {
                line = new PanelString();
                line.textLeft = StringUtils
                        .getFormatted("msg.nc.InfoPanelLiquidFree", totalCapacity - totalAmount, showLabels);
                result.add(line);
            }
            if (showName) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelLiquidCapacity", totalCapacity, showLabels);
                result.add(line);
            }
            if (showPercentage) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted(
                        "msg.nc.InfoPanelLiquidPercentage",
                        totalCapacity == 0 ? 100 : ((totalAmount / totalCapacity) * 100),
                        showLabels);
                result.add(line);
            }
        }
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>(7);
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelLiquidName"), DISPLAY_NAME, CARD_TYPE));
        result.add(
                new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelLiquidAmount"), DISPLAY_AMOUNT, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelLiquidFree"), DISPLAY_FREE, CARD_TYPE));
        result.add(
                new NewPanelSetting(
                        LangHelper.translate("msg.nc.cbInfoPanelLiquidCapacity"),
                        DISPLAY_CAPACITY,
                        CARD_TYPE));
        result.add(
                new NewPanelSetting(
                        LangHelper.translate("msg.nc.cbInfoPanelLiquidPercentage"),
                        DISPLAY_PERCENTAGE,
                        CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelLiquidEach"), DISPLAY_EACH, CARD_TYPE));
        result.add(
                new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelLiquidTotal"), DISPLAY_TOTAL, CARD_TYPE));
        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean advanced) {
        int cardCount = getCardCount(itemStack);
        if (cardCount > 0) {
            String title = CardAccessors.getTitle(itemStack);
            if (title != null && !title.isEmpty()) {
                info.add(title);
            }
            String hint = String.format(LangHelper.translate("msg.nc.LiquidCardQuantity"), cardCount);
            info.add(hint);
        }
    }

    public static int getCardCount(ItemStack item) {
        return NBTAccessors.getInt(item, "cardCount");
    }

    private static class LAData extends NBTCardLayout {

        public final DataAccessor<Integer> count = intAccessor("cardCount");
        public final DataAccessor<NBTBase> cards = tagAccessor("cards", new NBTTagList());
        public final DataAccessor<Double> energyL = doubleAccessor("energyL");
    }
}
