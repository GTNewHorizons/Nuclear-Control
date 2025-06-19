package shedar.mods.ic2.nuclearcontrol.api;

import java.util.List;

/**
 * This class is mostly a placeholder to give mods that depend on this API the time to adjust, without breaking
 * instantly. Functionality will be pulled down to {@link IPanelAdvDataSource} at some point.
 */
public interface IPanelAdvDataSource extends IPanelDataSource {

    /**
     * Method returns text representation of card's data. Each line is presented by {@link PanelString} object. Method
     * called on client side. Card's data shouldn't be modified here.
     *
     * @param displaySettings display settings, configure by player for this type of cards.
     * @param card            Wrapper object, to access field values.
     * @param showLabels      Information Panel option. This parameter is true if labels should be shown.
     * @return list of string to display.
     * @see PanelString
     */
    List<PanelString> getStringData(DisplaySettingHelper displaySettings, ICardWrapper card, boolean showLabels);
}
