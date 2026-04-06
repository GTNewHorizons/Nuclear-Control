package shedar.mods.ic2.nuclearcontrol.inventory;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import shedar.mods.ic2.nuclearcontrol.inventory.nbt.NBTAccessors;

public class IndexedItem<T extends Item> {

    public final int slot;
    public final ItemStack itemStack;
    public final T item;

    public IndexedItem(int slot, ItemStack itemStack, T item) {
        this.slot = slot;
        this.itemStack = itemStack;
        this.item = item;
    }
    // public void updateNBT(INBTUpdater updater) {
    // if (inventory == null) {
    // NBTTagCompound nbt = NBTAccessors.getOrCreateTagCompound(itemStack);
    // updater.updateNBT(nbt, this);
    // } else {
    // inventory.updateItemNBT(slot, updater);
    // }
    // }
    //
    // public void updateNBT() {
    // if (inventory != null) inventory.updateItemNBT(slot, _ -> {});
    // }

    @Nullable
    public NBTTagCompound getNBT() {
        return itemStack.getTagCompound();
    }

    public NBTTagCompound getOrCreateNBT() {
        return NBTAccessors.getOrCreateTagCompound(itemStack);
    }
}
