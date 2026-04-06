package shedar.mods.ic2.nuclearcontrol.items;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.reactor.IReactor;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IRemoteSensor;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.utils.CardAccessors;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.NuclearHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class ItemCardReactorSensorLocation extends ItemCardBase implements IRemoteSensor {

    protected static final String HINT_TEMPLATE = "x: %d, y: %d, z: %d";

    public static final int DISPLAY_ONOFF = 1;
    public static final int DISPLAY_HEAT = 2;
    public static final int DISPLAY_MAXHEAT = 3;
    public static final int DISPLAY_OUTPUT = 4;
    public static final int DISPLAY_TIME = 5;
    public static final int DISPLAY_MELTING = 6;

    public static final UUID CARD_TYPE = new UUID(0, 0);

    public ItemCardReactorSensorLocation() {
        super("cardReactor");
    }

    @Override
    public RSLData getLayout() {
        return new RSLData();
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        RSLData data = (RSLData) layout;
        ChunkCoordinates target = data.getTarget();
        if (target == null) return CardState.NO_TARGET;
        IReactor reactor = NuclearHelper.getReactorAt(world, target.posX, target.posY, target.posZ);
        if (reactor != null) {
            data.heat.set(reactor.getHeat());
            data.maxHeat.set(reactor.getMaxHeat());
            data.reactorPowered.set(NuclearHelper.isProducing(reactor));
            data.output.set((int) Math.round(reactor.getReactorEUEnergyOutput()));
            boolean isSteam = NuclearHelper.isSteam(reactor);
            data.isSteam.set(isSteam);

            IInventory inventory = (IInventory) reactor;
            int slotCount = inventory.getSizeInventory();
            int dmgLeft = 0;
            for (int i = 0; i < slotCount; i++) {
                ItemStack rStack = inventory.getStackInSlot(i);
                if (rStack != null) {
                    dmgLeft = Math.max(dmgLeft, NuclearHelper.getNuclearCellTimeLeft(rStack));
                }
            }

            int timeLeft;

            // Classic has a Higher Tick rate for Steam generation but damage tick rate is still the same...
            if (isSteam) {
                timeLeft = dmgLeft;
            } else {
                timeLeft = dmgLeft * reactor.getTickRate() / 10;
            }

            data.timeLeft.set(timeLeft);
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
        List<PanelString> result = new LinkedList<PanelString>();
        String text;
        PanelString line;
        RSLData data = (RSLData) layout;
        if (displaySettings.getSetting(DISPLAY_HEAT)) {
            line = new PanelString();
            line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelHeat", data.heat.get(), showLabels);
            result.add(line);
        }
        if (displaySettings.getSetting(DISPLAY_MAXHEAT)) {
            line = new PanelString();
            line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelMaxHeat", data.maxHeat.get(), showLabels);
            result.add(line);
        }
        if (displaySettings.getSetting(DISPLAY_MELTING)) {
            line = new PanelString();
            line.textLeft = StringUtils
                    .getFormatted("msg.nc.InfoPanelMelting", (double) (data.maxHeat.get() * 85) / 100, showLabels);
            result.add(line);
        }
        if (displaySettings.getSetting(DISPLAY_OUTPUT)) {
            line = new PanelString();
            if (data.isSteam.get()) {
                line.textLeft = StringUtils.getFormatted(
                        "msg.nc.InfoPanelOutputSteam",
                        NuclearHelper.euToSteam(data.output.get()),
                        showLabels);
            } else {
                line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelOutput", data.output.get(), showLabels);
            }
            result.add(line);
        }
        int timeLeft = data.timeLeft.get();
        if (displaySettings.getSetting(DISPLAY_TIME)) {
            int hours = timeLeft / 3600;
            int minutes = (timeLeft % 3600) / 60;
            int seconds = timeLeft % 60;
            line = new PanelString();

            String time = String.format("%d:%02d:%02d", hours, minutes, seconds);
            line.textLeft = StringUtils.getFormatted("msg.nc.InfoPanelTimeRemaining", time, showLabels);
            result.add(line);
        }

        int txtColor = 0;
        if (displaySettings.getSetting(DISPLAY_ONOFF)) {
            boolean reactorPowered = data.reactorPowered.get();
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
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>(6);
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelOnOff"), DISPLAY_ONOFF, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelHeat"), DISPLAY_HEAT, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelMaxHeat"), DISPLAY_MAXHEAT, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelMelting"), DISPLAY_MELTING, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelOutput"), DISPLAY_OUTPUT, CARD_TYPE));
        result.add(
                new NewPanelSetting(LangHelper.translate("msg.nc.cbInfoPanelTimeRemaining"), DISPLAY_TIME, CARD_TYPE));
        return result;
    }

    public static class RSLData extends NBTCardLayout {

        public DataAccessor<Integer> heat = intAccessor("heat");
        public DataAccessor<Integer> maxHeat = intAccessor("maxHeat");
        public DataAccessor<Boolean> reactorPowered = boolAccessor("reactorPoweredB");
        public DataAccessor<Integer> output = intAccessor("output");
        public DataAccessor<Boolean> isSteam = boolAccessor("isSteam");
        public DataAccessor<Integer> timeLeft = intAccessor("timeLeft");
    }
}
