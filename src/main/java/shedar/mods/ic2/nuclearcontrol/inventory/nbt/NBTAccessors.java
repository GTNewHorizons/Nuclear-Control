package shedar.mods.ic2.nuclearcontrol.inventory.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;

public class NBTAccessors {

    public static int getInt(ItemStack itemStack, String key) {
        NBTTagCompound compound = itemStack.getTagCompound();
        if (compound == null) return 0;
        return compound.getInteger(key);
    }

    public static int getInt(IndexedItem<?> item, String key) {
        return getInt(item.itemStack, key);
    }

    public static void setInt(String key, int value, ItemStack itemStack) {
        NBTTagCompound compound = getOrCreateTagCompound(itemStack);
        compound.setInteger(key, value);
        itemStack.setTagCompound(compound);
    }

    public static NBTTagCompound getOrCreateTagCompound(ItemStack itemStack) {
        NBTTagCompound compound = itemStack.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
            itemStack.setTagCompound(compound);
        }
        return compound;
    }

    public static NBTTagCompound getOrCreateTagCompound(IndexedItem<?> item) {
        return getOrCreateTagCompound(item.itemStack);
    }
}
