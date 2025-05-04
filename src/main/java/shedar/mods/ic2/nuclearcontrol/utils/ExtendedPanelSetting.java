package shedar.mods.ic2.nuclearcontrol.utils;

import java.util.UUID;

import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.PanelSetting;

public class ExtendedPanelSetting extends PanelSetting {

    /**
     * @deprecated Use the new {@link ExtendedPanelSetting#ExtendedPanelSetting(int, String, UUID)} constructor instead.
     * @param ID       ID of this setting, should be unique
     * @param title    Name of the option
     * @param cardType Identifier of the card. Should be same as {@link IPanelDataSource#getCardType()}.
     */
    public ExtendedPanelSetting(int ID, String title, UUID cardType) {
        super(title, ID, cardType);
        this.title = title;
        this.cardType = cardType;
    }

}
