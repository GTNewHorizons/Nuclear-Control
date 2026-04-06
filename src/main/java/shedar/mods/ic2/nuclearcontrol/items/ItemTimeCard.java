package shedar.mods.ic2.nuclearcontrol.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.api.DisplaySettingHelper;
import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.api.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;
import shedar.mods.ic2.nuclearcontrol.utils.StringUtils;

public class ItemTimeCard extends ItemCardBase {

    public static final UUID CARD_TYPE = new UUID(0, 1);
    public static final int MODE_24H = 1;

    public ItemTimeCard() {
        super("cardTime");
    }

    @Override
    public NBTCardLayout getLayout() {
        return new NBTCardLayout();
    }

    @Override
    public CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int range) {
        return CardState.OK;
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card,
            NBTCardLayout layout, boolean showLabels) {
        List<PanelString> result = new ArrayList<PanelString>(1);
        PanelString item = new PanelString();
        result.add(item);
        World world = FMLClientHandler.instance().getClient().theWorld;
        if (world == null) return Collections.emptyList();
        int time = (int) ((world.getWorldTime() + 6000) % 24000);
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

}
