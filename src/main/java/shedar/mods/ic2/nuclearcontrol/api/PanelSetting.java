package shedar.mods.ic2.nuclearcontrol.api;

import java.util.UUID;

/**
 * Object of PanelSetting class defines one checkbox in the card's settings.
 * 
 * @author Shedar
 * @see IPanelDataSource#getSettingsList()
 */
public class PanelSetting {

    /**
     * Name of the option
     */
    public String title;

    /**
     * A bit mask of the display setting
     */
    public int displayBit;

    /**
     * Identifier of the card. Should be same as {@link IPanelDataSource#getCardType()}.
     */
    public UUID cardType;

    /**
     * @param title      Name of the option
     * @param displayBit A bit mask of the setting.
     * @param cardType   Identifier of the card. Should be same as {@link IPanelDataSource#getCardType()}.
     * @deprecated Will update to match {@link NewPanelSetting}, this is only still here to support other mods using the
     *             API. depending on the amount of settings for a card, might crash the game with an out of memory error
     *             if used with a bitmask after the update
     */
    public PanelSetting(String title, int displayBit, UUID cardType) {
        this.title = title;
        this.displayBit = DisplaySettingHelper.bitMaskToIndex(displayBit);
        this.cardType = cardType;
    }
}
