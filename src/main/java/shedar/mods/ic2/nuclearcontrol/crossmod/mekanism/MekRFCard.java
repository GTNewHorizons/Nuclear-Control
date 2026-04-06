package shedar.mods.ic2.nuclearcontrol.crossmod.mekanism;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import shedar.mods.ic2.nuclearcontrol.api.NewPanelSetting;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardEnergySensorLocation;
import shedar.mods.ic2.nuclearcontrol.utils.LangHelper;

public class MekRFCard extends ItemCardEnergySensorLocation {

    public static final UUID CARD_TYPE = new UUID(0, 3);

    public MekRFCard() {
        this.setUnlocalizedName("MekRFenergyCard");
    }

    @Override
    public UUID getCardType() {
        return CARD_TYPE;
    }

    @Override
    public List<PanelSetting> getSettingsList() {
        List<PanelSetting> result = new ArrayList<PanelSetting>(4);
        result.add(new NewPanelSetting(LangHelper.translate("1"), DISPLAY_ENERGY, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("2"), DISPLAY_STORAGE, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("3"), DISPLAY_FREE, CARD_TYPE));
        result.add(new NewPanelSetting(LangHelper.translate("4"), DISPLAY_PERCENTAGE, CARD_TYPE));
        return result;
    }
}
