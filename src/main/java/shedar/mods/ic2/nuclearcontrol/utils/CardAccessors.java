package shedar.mods.ic2.nuclearcontrol.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import shedar.mods.ic2.nuclearcontrol.api.CardState;
import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;

public class CardAccessors {

    public static CardState getState(IndexedItem<?> item) {
        return getState(item.itemStack);
    }

    public static CardState getState(ItemStack itemStack) {
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return null;
        int state = nbt.getInteger("state");
        return state > 0 ? CardState.fromInteger(state) : null;
    }

    public static ChunkCoordinates getCoordinates(ItemStack item) {
        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null) return null;
        return new ChunkCoordinates(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
    }

    public static ChunkCoordinates getCoordinates(IndexedItem<?> item) {
        return getCoordinates(item.itemStack);
    }

    public static String getTitle(ItemStack item) {
        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null) return "";
        return nbt.getString("title");
    }
}
