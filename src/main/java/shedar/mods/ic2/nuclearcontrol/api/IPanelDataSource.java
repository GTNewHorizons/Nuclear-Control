package shedar.mods.ic2.nuclearcontrol.api;

import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.inventory.nbt.NBTCardLayout;
import shedar.mods.ic2.nuclearcontrol.inventory.nbt.NBTLayout;

/**
 * Custom card for Industrial Information Panel is class, inherited from Item, which implements IPanelDataSource
 * interface. For extended card's behavior it can also implement {@link IRemoteSensor} and {@link IAdvancedCardSettings}
 * interfaces.
 * 
 * @author Shedar, Guid118
 */
public interface IPanelDataSource {
    NBTCardLayout getLayout();

    /**
     * Method to update card's data. Method called on server side.
     * 
     * @param panel    Information Panel, which contains card.
     * @param card     Wrapper object, to update fields and get access to ItemStack
     * @param maxRange max allowed range to the target object, based on Range Upgrades count.
     * @return State of the card after update. Check {@link CardState} for details.
     */
    CardState update(TileEntity panel, IndexedItem<?> card, NBTCardLayout layout, int maxRange);

    /**
     * Method to update card's data in Remote Monitor. Method called on server side.
     *
     * @param world    World, to get a Tile Entity's Location
     * @param card     Wrapper object, to update fields and get access to ItemStack
     * @param maxRange max allowed range to the target object, based on Range Upgrades count.
     * @return State of the card after update. Check {@link CardState} for details.
     */
    CardState update(World world, IndexedItem<?> card, NBTCardLayout layout, int maxRange);

    /**
     * Method returns text representation of card's data. Each line is presented by {@link PanelString} object. Method
     * called on client side. Card's data shouldn't be modified here.
     * 
     * @param displaySettings bit mask of display settings, configure by player for this type of cards.
     * @param card            Wrapper object, to access field values.
     * @param showLabels      Information Panel option. This parameter is true if labels should be shown.
     * @return list of string to display.
     * @see PanelString
     * @deprecated please implement {@link IPanelDataSource#getStringData(DisplaySettingHelper, IndexedItem, boolean)}
     *             instead. Will be removed in 3.0.0
     */
    List<PanelString> getStringData(int displaySettings, IndexedItem<?> card, NBTCardLayout layout, boolean showLabels);

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
    default List<PanelString> getStringData(DisplaySettingHelper displaySettings, IndexedItem<?> card, NBTCardLayout layout, boolean showLabels) {
        return getStringData(displaySettings.getAsInteger(), card, layout, showLabels);
    }

    /**
     * Method should return a list of settings, which displayed in the Information Panel gui as checkboxes. If card
     * doesn't have any settings - method can return either null or empty list. Each setting is represented by
     * {@link PanelSetting} object.
     */
    List<PanelSetting> getSettingsList();

    /**
     * Method should return identifier of the card. It used to save card's display settings. GUID can be generated at
     * http://guidgen.com/ site. You shouldn't use non-random uid, like UUID(0, 4). Non-random uuids used in Nuclear
     * Control cards for backward compatibility.
     */
    UUID getCardType();
}
