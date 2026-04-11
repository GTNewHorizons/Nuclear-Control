package shedar.mods.ic2.nuclearcontrol.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import shedar.mods.ic2.nuclearcontrol.api.IndexedItem;

public class NBTAccessors {

    public static int getInt(ItemStack itemStack, String key) {
        NBTTagCompound compound = itemStack.getTagCompound();
        if (compound == null) return 0;
        return compound.getInteger(key);
    }

    public static double getDouble(ItemStack itemStack, String key) {
        NBTTagCompound compound = itemStack.getTagCompound();
        if (compound == null) return 0;
        return compound.getDouble(key);
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
