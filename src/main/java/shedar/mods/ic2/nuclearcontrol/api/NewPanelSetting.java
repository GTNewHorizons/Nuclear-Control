package shedar.mods.ic2.nuclearcontrol.api;

import java.util.UUID;

public class NewPanelSetting extends PanelSetting {

    /**
     * @param title               Name of the option, will only be shown in the advanced info panel
     * @param displaySettingIndex An index location of the setting
     * @param cardType            Identifier of the card. Should be same as {@link IPanelDataSource#getCardType()}.
     */
    public NewPanelSetting(String title, int displaySettingIndex, UUID cardType) {
        super(title, DisplaySettingHelper.indexToBitMask(displaySettingIndex), cardType);
    }

}
