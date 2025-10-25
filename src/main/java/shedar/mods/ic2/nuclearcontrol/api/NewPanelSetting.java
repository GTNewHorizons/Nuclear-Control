package shedar.mods.ic2.nuclearcontrol.api;

import java.util.UUID;

public class NewPanelSetting extends PanelSetting {

    /**
     * Construct a PanelSetting that has an index instead of a bitmask. Unfortunately the constructor is the same as the
     * deprecated one, since they both use an integer. This problem should be resolved with the removal of the
     * deprecated constructor, in 3.0.0
     *
     * @param title               Name of the option, will only be shown in the advanced info panel's settings GUI
     * @param displaySettingIndex An index location of the setting
     * @param cardType            Identifier of the card. Should be same as {@link IPanelDataSource#getCardType()}.
     */
    public NewPanelSetting(String title, int displaySettingIndex, UUID cardType) {
        super(title, DisplaySettingHelper.indexToBitMask(displaySettingIndex), cardType);
    }

}
