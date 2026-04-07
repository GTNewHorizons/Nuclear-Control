package shedar.mods.ic2.nuclearcontrol.api;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import shedar.mods.ic2.nuclearcontrol.utils.NBTAccessors;

public class IndexedItem<T> {

    public final int slot;
    public final ItemStack itemStack;
    public final T item;

    public IndexedItem(int slot, ItemStack itemStack, T item) {
        this.slot = slot;
        this.itemStack = itemStack;
        this.item = item;
    }

    @Nullable
    public NBTTagCompound getNBT() {
        return itemStack.getTagCompound();
    }

    public NBTTagCompound getOrCreateNBT() {
        return NBTAccessors.getOrCreateTagCompound(itemStack);
    }
}
