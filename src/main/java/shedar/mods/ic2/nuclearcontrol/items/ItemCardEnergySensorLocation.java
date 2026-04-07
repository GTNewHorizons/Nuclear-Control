package shedar.mods.ic2.nuclearcontrol.items;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IRangeTriggerable;
import shedar.mods.ic2.nuclearcontrol.api.IRemoteSensor;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.crossmod.EnergyStorageData;
import shedar.mods.ic2.nuclearcontrol.utils.CardAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.EnergyStorageHelper;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class ItemCardEnergySensorLocation extends ItemCardBase implements IRemoteSensor, IRangeTriggerable {

    protected static final String HINT_TEMPLATE = "x: %d, y: %d, z: %d";

    public static final int DISPLAY_ENERGY = 1;
    public static final int DISPLAY_FREE = 2;
    public static final int DISPLAY_STORAGE = 3;
    public static final int DISPLAY_PERCENTAGE = 4;

    public static final UUID CARD_TYPE = new UUID(0, 2);

    public ItemCardEnergySensorLocation() {
        super("cardEnergy");
    }

    @Override
    public ESLData getLayout() {
        return new ESLData();
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        ESLData data = (ESLData) layout;
        ChunkCoordinates target = data.getTarget();
        if (target == null) return CardState.NO_TARGET;
        int targetType = data.targetType.get();
        EnergyStorageData storage = EnergyStorageHelper
                .getStorageAt(world, target.posX, target.posY, target.posZ, targetType);
        if (storage != null) {
            data.energy.set(storage.stored);
            data.maxStorage.set(storage.capacity);
            data.rangeTriggerAmount.set(storage.stored);
            return CardState.OK;
        } else {
            return CardState.NO_TARGET;
        }
    }

    @Override
    public UUID getCardType() {
        return CARD_TYPE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack itemStack, EntityPlayer player, List info, boolean advanced) {
        ChunkCoordinates target = CardAccessors.getCoordinates(itemStack);
        if (target != null) {
            String title = CardAccessors.getTitle(itemStack);
            if (title != null && !title.isEmpty()) {
                info.add(title);
            }
            String hint = String.format(HINT_TEMPLATE, target.posX, target.posY, target.posZ);
            info.add(hint);
        }
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card,
            NBTCardLayout layout, boolean showLabels) {
        ESLData data = (ESLData) layout;
        List<PanelString> result = new LinkedList<PanelString>();
        PanelString line;

        double energy = data.energy.get();
        double storage = data.maxStorage.get();

        if (displaySettings.getSetting(DISPLAY_ENERGY)) {
            line = new PanelString();
            line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelEnergy", energy, showLabels);
            result.add(line);
        }
        if (displaySettings.getSetting(DISPLAY_FREE)) {
            line = new PanelString();
            line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelEnergyFree", storage - energy, showLabels);
            result.add(line);
        }
        if (displaySettings.getSetting(DISPLAY_STORAGE)) {
            line = new PanelString();
            line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelEnergyStorage", storage, showLabels);
            result.add(line);
        }
        if (displaySettings.getSetting(DISPLAY_PERCENTAGE)) {
            line = new PanelString();
            line.textLeft = StringUtils.getFormatted(
                    "msg.nc.InfoPanelEnergyPercentage",
                    storage == 0 ? 100 : ((energy / storage) * 100),
                    showLabels);
            result.add(line);
        }
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>(4); // Initial capacity should be 4
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
        return result;
    }

    public static class ESLData extends NBTCardLayout {

        public DataAccessor<Double> energy = doubleAccessor("energyL");
        public DataAccessor<Double> maxStorage = doubleAccessor("maxStorageL");
        public DataAccessor<Double> rangeTriggerAmount = doubleAccessor("range_trigger_amount");
        public DataAccessor<Integer> targetType = intAccessor("targetType");
    }
}
