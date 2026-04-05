package shedar.mods.ic2.nuclearcontrol.inventory.nbt;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;

@FunctionalInterface
public interface INBTUpdater {
    default void updateNBT(NBTTagCompound compound, IndexedItem<? extends Item> indexedItem) {
        updateNBT(compound);
    }

    void updateNBT(NBTTagCompound compound);
}
