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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.crossmod.EnergyStorageData;
import shedar.mods.ic2.nuclearcontrol.utils.CardAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.EnergyStorageHelper;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.NBTAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class ItemCardEnergyArrayLocation extends ItemCardBase {

    public static final int DISPLAY_ENERGY = 1;
    public static final int DISPLAY_FREE = 2;
    public static final int DISPLAY_STORAGE = 3;
    public static final int DISPLAY_EACH = 4;
    public static final int DISPLAY_TOTAL = 5;
    public static final int DISPLAY_PERCENTAGE = 6;

    private static final int STATUS_NOT_FOUND = Integer.MIN_VALUE;
    private static final int STATUS_OUT_OF_RANGE = Integer.MIN_VALUE + 1;

    public static final UUID CARD_TYPE = new UUID(0, 3);

    public ItemCardEnergyArrayLocation() {
        super("cardEnergyArray");
    }

    @Override
    public NBTCardLayout getLayout() {
        return new EAData();
    }

    private int[] getCoordinates(NBTTagCompound compound) {
        int x = compound.getInteger("x");
        int y = compound.getInteger("y");
        int z = compound.getInteger("z");
        return new int[] { x, y, z };
    }

    public static void initArray(ItemStack card, Vector<ItemStack> cards) {
        EAData data = new EAData();
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
        EAData data = (EAData) layout;
        int cardCount = data.count.get();
        double totalEnergy = 0.0;
        if (cardCount == 0) {
            return CardState.INVALID_CARD;
        } else {
            boolean foundAny = false;
            boolean outOfRange = false;
            NBTTagList tagList = (NBTTagList) data.cards.get();

            for (int i = 0; i < cardCount; i++) {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                int[] coordinates = getCoordinates(tag);
                int dx = coordinates[0] - panel.xCoord;
                int dy = coordinates[1] - panel.yCoord;
                int dz = coordinates[2] - panel.zCoord;
                if (Math.abs(dx) <= range && Math.abs(dy) <= range && Math.abs(dz) <= range) {
                    EnergyStorageData storage = EnergyStorageHelper.getStorageAt(
                            panel.getWorldObj(),
                            coordinates[0],
                            coordinates[1],
                            coordinates[2],
                            tag.getInteger("targetType"));
                    if (storage != null) {
                        totalEnergy += storage.stored;
                        tag.setInteger("energy", (int) storage.stored);
                        tag.setInteger("maxStorage", (int) storage.capacity);
                        foundAny = true;
                    } else {
                        tag.setInteger("energy", STATUS_NOT_FOUND);
                    }
                } else {
                    tag.setInteger("energy", STATUS_OUT_OF_RANGE);
                    outOfRange = true;
                }
            }
            data.energyL.set(totalEnergy);
            if (!foundAny) {
                if (outOfRange) return CardState.OUT_OF_RANGE;
                else return CardState.NO_TARGET;
            }
            return CardState.OK;
        }
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        return CardState.CUSTOM_ERROR;
    }

    @Override
    public UUID getCardType() {
        return CARD_TYPE;
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card,
            NBTCardLayout layout, boolean showLabels) {
        EAData data = (EAData) layout;

        List<PanelString> result = new LinkedList<>();
        PanelString line;
        double totalEnergy = 0;
        double totalStorage = 0;
        boolean showEach = displaySettings.getSetting(DISPLAY_EACH);
        boolean showSummary = displaySettings.getSetting(DISPLAY_TOTAL);
        boolean showEnergy = displaySettings.getSetting(DISPLAY_ENERGY);
        boolean showFree = displaySettings.getSetting(DISPLAY_FREE);
        boolean showStorage = displaySettings.getSetting(DISPLAY_STORAGE);
        boolean showPercentage = displaySettings.getSetting(DISPLAY_PERCENTAGE);

        int cardCount = data.count.get();
        NBTTagList tagList = (NBTTagList) data.cards.get();

        for (int i = 0; i < cardCount; i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            int energy = tag.getInteger("energy");
            int storage = tag.getInteger("maxStorage");
            boolean isOutOfRange = energy == STATUS_OUT_OF_RANGE;
            boolean isNotFound = energy == STATUS_NOT_FOUND;
            if (showSummary && !isOutOfRange && !isNotFound) {
                totalEnergy += energy;
                totalStorage += storage;
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
                    if (showEnergy) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelEnergyN",
                                i + 1,
                                StringUtils.getFormatted("", energy, false));
                        else line.textLeft = StringUtils.getFormatted("", energy, false);
                        result.add(line);
                    }
                    if (showFree) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelEnergyFreeN",
                                i + 1,
                                StringUtils.getFormatted("", storage - energy, false));
                        else line.textLeft = StringUtils.getFormatted("", storage - energy, false);

                        result.add(line);
                    }
                    if (showStorage) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelEnergyStorageN",
                                i + 1,
                                StringUtils.getFormatted("", storage, false));
                        else line.textLeft = StringUtils.getFormatted("", storage, false);
                        result.add(line);
                    }
                    if (showPercentage) {
                        line = new PanelString();
                        if (showLabels) line.textLeft = StringUtils.getFormattedKey(
                                "msg.nc.InfoPanelEnergyPercentageN",
                                i + 1,
                                StringUtils.getFormatted(
                                        "",
                                        storage == 0 ? 100 : (int) (((double) energy / storage) * 100D),
                                        false));
                        else line.textLeft = StringUtils.getFormatted(
                                "",
                                storage == 0 ? 100 : (int) (((double) energy / storage) * 100D),
                                false);
                        result.add(line);
                    }
                }
            }
        }
        if (showSummary) {
            if (showEnergy) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelEnergy", totalEnergy, showLabels);
                result.add(line);
            }
            if (showFree) {
                line = new PanelString();
                line.textLeft = StringUtils
                        .getFormatted("msg.nc.InfoPanelEnergyFree", totalStorage - totalEnergy, showLabels);
                result.add(line);
            }
            if (showStorage) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelEnergyStorage", totalStorage, showLabels);
                result.add(line);
            }
            if (showPercentage) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted(
                        "msg.nc.InfoPanelEnergyPercentage",
                        totalStorage == 0 ? 100 : ((totalEnergy / totalStorage) * 100),
                        showLabels);
                result.add(line);
            }
        }
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<>(6);
        result.add(
                new NewPanelSetting(
                        LangHelper.translate("msg.nc.cbInfoPanelEnergyCurrent"),
                        DISPLAY_ENERGY,
                        CARD_TYPE));
        result.add(
                new NewPanelSetting(
                        LangHelper.translate("msg.nc.cbInfoPanelEnergyStorage"),
                        DISPLAY_STORAGE,
                        CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelEnergyFree"), DISPLAY_FREE, CARD_TYPE));
        result.add(
                new NewPanelSetting(
                        LangHelper.translate("msg.nc.cbInfoPanelEnergyPercentage"),
                        DISPLAY_PERCENTAGE,
                        CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelEnergyEach"), DISPLAY_EACH, CARD_TYPE));
        result.add(
                new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelEnergyTotal"), DISPLAY_TOTAL, CARD_TYPE));
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
            String hint = String.format(LangHelper.translate("msg.nc.EnergyCardQuantity"), cardCount);
            info.add(hint);
        }
    }

    public static int getCardCount(ItemStack item) {
        return NBTAccessors.getInt(item, "cardCount");
    }

    private static class EAData extends NBTCardLayout {

        public final DataAccessor<Integer> count = intAccessor("cardCount");
        public final DataAccessor<NBTBase> cards = tagAccessor("cards", new NBTTagList());
        public final DataAccessor<Double> energyL = doubleAccessor("energyL");
    }
}
