package shedar.mods.ic2.nuclearcontrol.api;

import java.util.List;
import java.util.UUID;
import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;
import shedar.mods.ic2.nuclearcontrol.items.ItemCardBase;

public interface IPanelMultiCard {

    List<PanelSetting> getSettingsList(IndexedItem<ItemCardBase> card);

    UUID getCardType(IndexedItem<?> card);
}
