package shedar.mods.ic2.nuclearcontrol.inventory;

import net.minecraft.item.ItemStack;

public interface IInventoryListener {

    void onItemAdded(int slot, ItemStack item);

    void onItemRemoved(int slot, ItemStack item);
    // void onItemNBTUpdated(int slot, ItemStack item);
}
