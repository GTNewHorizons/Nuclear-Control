package shedar.mods.ic2.nuclearcontrol.tileentities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import shedar.mods.ic2.nuclearcontrol.inventory.IInventoryListener;
import shedar.mods.ic2.nuclearcontrol.inventory.IndexedItem;

public class TileEntityInventory {
    private final List<IInventoryListener> listeners = new ArrayList<>();
    private final ItemStack[] items;
    private final int[] stackSizeLimits;

    private int count;
    private NBTTagList nbt;

    public final int capacity;

    public TileEntityInventory(int capacity) {
        this.items = new ItemStack[capacity];
        this.stackSizeLimits = new int[capacity];
        this.capacity = capacity;
    }

    public TileEntityInventory(TileEntityInventory other) {
        this.stackSizeLimits = other.stackSizeLimits.clone();
        this.items = new ItemStack[other.items.length];
        this.capacity = other.capacity;
    }

    public void addListener(IInventoryListener listener) {
        listeners.add(listener);
    }

    public void setStackSizeLimit(int slot, int limit) {
        stackSizeLimits[slot] = limit;
    }

    public int getStackSizeLimit(int slot) {
        return stackSizeLimits[slot];
    }

    public void set(int slot, ItemStack item) {
        ItemStack oldItem = items[slot];

        int stackLimit = stackSizeLimits[slot];
        if (stackLimit == 0) stackLimit = item.getMaxStackSize();

        ItemStack storedItem;
        if (item.stackSize > stackLimit) {
            storedItem = item.splitStack(stackLimit);
        } else {
            storedItem = item.copy();
            item.stackSize = 0;
        }

        items[slot] = storedItem;

        nbt = null;
        boolean isNewItem = oldItem == null;
        if (isNewItem) count++;

        for (IInventoryListener listener : listeners) {
            if (!isNewItem) listener.onItemRemoved(slot, oldItem);
            listener.onItemAdded(slot, storedItem);
        }
    }

    public ItemStack get(int slot) {
        return items[slot];
    }

    public IndexedItem<Item> getIndexed(int slot) {
        return new IndexedItem<>(slot, items[slot], items[slot].getItem());
    }
    
    public List<IndexedItem<Item>> getItems() {
        return getItems(Item.class);
    }

    public <T extends Item> List<IndexedItem<T>> getItems(Class<T> type) {
        List<IndexedItem<T>> indexedItems = new ArrayList<>(count);
        for (int i = 0; i < items.length; i++) {
            ItemStack itemStack = items[i];
            if (itemStack != null) {
                Item item = itemStack.getItem();
                if (item == null || !type.isAssignableFrom(item.getClass())) continue;
                indexedItems.add(new IndexedItem<>(i, itemStack, (T) item));
            }
        }
        return indexedItems;
    }

    /** Updates only the NBT tag of an existing stack without firing listeners. Used for card data sync. */
    public void updateNBT(int slot, ItemStack incoming) {
        ItemStack existing = items[slot];
        if (existing == null) {
            set(slot, incoming);
            return;
        }
        existing.stackTagCompound = incoming.stackTagCompound != null
                ? (net.minecraft.nbt.NBTTagCompound) incoming.stackTagCompound.copy()
                : null;
        nbt = null;
    }

    public ItemStack remove(int slot) {
        ItemStack item = items[slot];
        if (item == null) return null;
        items[slot] = null;
        count--;
        nbt = null;
        return item;
    }

    public ItemStack removeFromStack(int slot, int amount) {
        ItemStack item = items[slot];
        if (item == null) return null;

        nbt = null;
        if (item.stackSize > amount) return item.splitStack(amount);
        return remove(slot);
    }

    public void load(NBTTagList tagList) {
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            byte slotNum = compound.getByte("Slot");

            if (slotNum >= 0 && slotNum < capacity) {
                ItemStack nbtItem = ItemStack.loadItemStackFromNBT(compound);
                if (nbtItem != null) set(slotNum, nbtItem);
            }
        }
        nbt = (NBTTagList) tagList.copy();
    }

//    public void updateItemNBT(int slot, INBTUpdater updater) {
//        if (slot < 0 || slot >= capacity) return;
//        ItemStack itemStack = items[slot];
//        if (itemStack == null) return;
//        IndexedItem<Item> item = new IndexedItem<>(slot, itemStack, itemStack.getItem());
//        updater.updateNBT(NBTAccessors.getOrCreateTagCompound(itemStack), item);
//        for (IInventoryListener listener : listeners) listener.onItemNBTUpdated(slot, itemStack);
//    }

    public NBTTagList getNBT() {
        return getNBT(false);
    }

    public NBTTagList getNBT(boolean force) {
        if (nbt != null && !force) return nbt;
        nbt = new NBTTagList();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setByte("Slot", (byte) i);
                item.writeToNBT(compound);
                nbt.appendTag(compound);
            }
        }
        return nbt;
    }
}
