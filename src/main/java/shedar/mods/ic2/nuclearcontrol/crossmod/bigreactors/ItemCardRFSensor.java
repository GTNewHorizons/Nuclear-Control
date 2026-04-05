package shedar.mods.ic2.nuclearcontrol.crossmod.bigreactors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IRangeTriggerable;
import shedar.mods.ic2.nuclearcontrol.api.IRemoteSensor;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.inventory.nbt.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardBase;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;


import static shedar.mods.ic2.nuclearcontrol.items.ItemCardEnergySensorLocation.CARD_TYPE;

public class ItemCardRFSensor extends ItemCardBase implements IRemoteSensor, IRangeTriggerable {

    public ItemCardRFSensor() {
        super("nuclearcontrol:cardRFReactor");
    }

    public static final int DISPLAY_ON = 1;
    public static final int DISPLAY_OUTPUT = 2;
    public static final int DISPLAY_ENERGY = 3;
    public static final int DISPLAY_PERCENTAGE = 4;
    public static final int DISPLAY_TEMP = 5;
    public static final UUID CARD_TYPE1 = new UUID(0, 2);

    @Override
    public BRCardData getLayout() {
        return new BRCardData();
    }

    @Override
    public UUID getCardType() {
        return CARD_TYPE1;
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        BRCardData data = (BRCardData) layout;
        ChunkCoordinates target = data.getTarget();
        if (target == null) return CardState.NO_TARGET;
        // int targetType = card.getInt("targetType");
        TileEntity check = world.getTileEntity(target.posX, target.posY, target.posZ);
        if (check instanceof TileEntityBlockFetcher) {
            TileEntityBlockFetcher BF = (TileEntityBlockFetcher) check;

            MultiblockReactor reactorController = BF.getReactorController();

            // TODO gamerforEA code start
            if (reactorController == null) return CardState.NO_TARGET;
            // TODO gamerforEA code end

            data.online.set(BF.isReactorOnline());
            data.storedEnergy.set((double) BF.getEnergyStored());
            data.createdEnergy.set((double) BF.getEnergyGenerated());
            data.temperature.set(BF.getTemp());
            data.fillPercent.set((double) BF.getEnergyOutPercent());
            data.isPassive.set(BF.getReactorController().isPassivelyCooled());
            if (!BF.getReactorController().isPassivelyCooled()) {
                if (BF.getReactorController().getCoolantContainer().getVaporType() != null) {
                    data.vaporType.set(BF.getReactorController().getCoolantContainer().getVaporType().getLocalizedName());
                    data.vaporAmount.set(BF.getReactorController().getCoolantContainer().getVaporAmount());
                } else {
                    data.vaporType.set("Empty");
                    data.vaporAmount.set(0);
                }
                if (BF.getReactorController().getCoolantContainer().getCoolantType() != null) {
                    data.coolantType.set(BF.getReactorController().getCoolantContainer().getCoolantType().getLocalizedName());
                    data.coolantAmount.set(BF.getReactorController().getCoolantContainer().getCoolantAmount());
                } else {
                    data.coolantType.set("Empty");
                    data.coolantAmount.set(0);
                }
            }
            return CardState.OK;
        }
        return CardState.NO_TARGET;
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card, NBTCardLayout layout,
            boolean showLabels) {
        List<PanelString> result = new LinkedList<PanelString>();
        PanelString line;

        BRCardData data = (BRCardData) layout;

        double PerOut = data.fillPercent.get();
        double energyStored = data.storedEnergy.get();
        double outputlvl = data.createdEnergy.get();
        double coreTemp = data.temperature.get();
        int ioutputlvl = (int) outputlvl;
        int ienergyStored = (int) energyStored;
        boolean passive = data.isPassive.get();
        if (passive) {
            // Temperature
            if (displaySettings.getSetting(DISPLAY_TEMP)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelRF.Temp", coreTemp, showLabels);
                result.add(line);
            }

            // Stored Energy
            if (displaySettings.getSetting(DISPLAY_ENERGY)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelRF.EnergyStored", ienergyStored, showLabels);
                result.add(line);
            }

            // Energy Created Frequency
            if (displaySettings.getSetting(DISPLAY_OUTPUT)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelRF.CreatedEnergy", ioutputlvl, showLabels);
                result.add(line);
            }

            // Output Percentage
            if (displaySettings.getSetting(DISPLAY_PERCENTAGE)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelRF.Percentage", PerOut, showLabels);
                result.add(line);
            }

            // On or Off
            int txtColor = 0;
            String text;
            if (displaySettings.getSetting(DISPLAY_ON)) {
                boolean reactorPowered = data.online.get();
                if (reactorPowered) {
                    txtColor = 0x00ff00;
                    text = LangHelper.translate("msg.nc.InfoPanelOn");
                } else {
                    txtColor = 0xff0000;
                    text = LangHelper.translate("msg.nc.InfoPanelOff");
                }
                if (result.size() > 0) {
                    PanelString firstLine = result.get(0);
                    firstLine.textRight = text;
                    firstLine.colorRight = txtColor;
                } else {
                    line = new PanelString();
                    line.textLeft = text;
                    line.colorLeft = txtColor;
                    result.add(line);
                }
            }
        } else {
            // Temperature
            if (displaySettings.getSetting(DISPLAY_TEMP)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelRF.Temp", coreTemp, showLabels);
                result.add(line);
            }
            // Energy Created Frequency
            if (displaySettings.getSetting(DISPLAY_OUTPUT)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelRF.SteamOutput", ioutputlvl, showLabels);
                result.add(line);
            }
            // Stored Energy
            if (displaySettings.getSetting(DISPLAY_ENERGY)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormattedKey(
                        "msg.nc.InfoPanelRF.CoolantTank",
                        data.coolantType.get(),
                        data.coolantAmount.get());
                result.add(line);
            }
            // Vapor Tank
            if (displaySettings.getSetting(DISPLAY_PERCENTAGE)) {
                line = new PanelString();
                line.textLeft = StringUtils.getFormattedKey(
                        "msg.nc.InfoPanelRF.OutputTank",
                        data.vaporType.get(),
                        data.vaporAmount.get());
                result.add(line);
            }
            // On or Off
            int txtColor = 0;
            String text;
            if (displaySettings.getSetting(DISPLAY_ON)) {
                boolean reactorPowered = data.online.get();
                if (reactorPowered) {
                    txtColor = 0x00ff00;
                    text = LangHelper.translate("msg.nc.InfoPanelOn");
                } else {
                    txtColor = 0xff0000;
                    text = LangHelper.translate("msg.nc.InfoPanelOff");
                }
                if (result.size() > 0) {
                    PanelString firstLine = result.get(0);
                    firstLine.textRight = text;
                    firstLine.colorRight = txtColor;
                } else {
                    line = new PanelString();
                    line.textLeft = text;
                    line.colorLeft = txtColor;
                    result.add(line);
                }
            }

        }
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>(5);
        result.add(new NewPanelSetting(LangHelper.translate("1"), DISPLAY_ON, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("2"), DISPLAY_ENERGY, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("3"), DISPLAY_OUTPUT, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("4"), DISPLAY_TEMP, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("5"), DISPLAY_PERCENTAGE, CARD_TYPE));
        return result;
    }

    public static class BRCardData extends NBTCardLayout {
        public DataAccessor<Boolean> online = boolAccessor("Online");
        public DataAccessor<Double> storedEnergy = doubleAccessor("storedEnergy");
        public DataAccessor<Double> createdEnergy = doubleAccessor("createdEnergy");
        public DataAccessor<Integer> temperature = intAccessor("Temp");
        public DataAccessor<Double> fillPercent = doubleAccessor("FillPercent");
        public DataAccessor<Boolean> isPassive = boolAccessor("isPassive");
        public DataAccessor<String> vaporType = stringAccessor("VaporType");
        public DataAccessor<Integer> vaporAmount = intAccessor("VaporAmount");
        public DataAccessor<String> coolantType = stringAccessor("CoolantType");
        public DataAccessor<Integer> coolantAmount = intAccessor("CoolantAmount");
    }
}
