package shedar.mods.ic2.nuclearcontrol.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.world.World;

import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

// Special card that is only rendered by the client and does not send packets from the server
public class ItemTimeCard extends ItemCardBase {

    public static final UUID CARD_TYPE = new UUID(0, 1);
    public static final int MODE_24H = 1;

    public ItemTimeCard() {
        super("cardTime");
    }

    @Override
    public NBTCardLayout getLayout() {
        return new TimeCardData();
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        // don't update on server side
        if (!world.isRemote) return CardState.OK;
        TimeCardData data = (TimeCardData) layout;
        data.time.set((world.getWorldTime() + 6000) % 24000);
        return CardState.OK;
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card,
            NBTCardLayout layout, boolean showLabels) {
        TimeCardData data = (TimeCardData) layout;
        List<PanelString> result = new ArrayList<>(1);
        PanelString item = new PanelString();
        result.add(item);
        Long timeLong = data.time.get();
        if (timeLong == null) return Collections.emptyList();

        int time = timeLong.intValue();
        int hours = time / 1000;
        int minutes = (time % 1000) * 6 / 100;
        String suffix = "";

        if (displaySettings.getSetting(MODE_24H)) {
            suffix = hours < 12 ? "AM" : "PM";
            hours %= 12;
            if (hours == 0) hours += 12;
        }

        item.textLeft = StringUtils
                .getFormatted("msg.nc.InfoPanelTime", String.format("%02d:%02d%s", hours, minutes, suffix), showLabels);
        return result;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>(1);
        result.add(new NewPanelSetting(LangHelper.translate("msg.nc.cb24h"), MODE_24H, CARD_TYPE));
        return result;
    }

    @Override
    public UUID getCardType() {
        return CARD_TYPE;
    }

    @Override
    public boolean isClientOnly() {
        return true;
    }

    private static class TimeCardData extends NBTCardLayout {

        private final DataAccessor<Long> time = longAccessor("time");
    }
}
