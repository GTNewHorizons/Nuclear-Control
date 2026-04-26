package shedar.mods.ic2.nuclearcontrol.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import shedar.mods.ic2.nuclearcontrol.SlotFilter;
import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;

public class ContainerInfoPanel extends Container {

    public TileEntityInfoPanel panel;
    public EntityPlayer player;

    public ContainerInfoPanel() {
        super();
    }

    public ContainerInfoPanel(EntityPlayer player, TileEntityInfoPanel panel) {
        super();

        this.panel = panel;
        this.player = player;

        // card
        addSlotToContainer(new SlotFilter(panel, 0, 8, 24 + 18));

        // range upgrade
        addSlotToContainer(new SlotFilter(panel, 1, 8, 24 + 18 * 2));

        // color upgrade
        addSlotToContainer(new SlotFilter(panel, 2, 8, 24 + 18 * 3));

        // inventory
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                addSlotToContainer(new Slot(player.inventory, k + i * 9 + 9, 8 + k * 18, 24 + 84 + i * 18));
            }
        }

        for (int j = 0; j < 9; j++) {
            addSlotToContainer(new Slot(player.inventory, j, 8 + j * 18, 24 + 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1) {
        return panel.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer p, int slotId) {
        Slot slot = (Slot) this.inventorySlots.get(slotId);
        if (slot == null || !slot.getHasStack()) return null;

        ItemStack items = slot.getStack();
        ItemStack originalCopy = items.copy();

        if (slotId < panel.getSizeInventory()) {
            // panel → player
            if (!mergeItemStack(items, panel.getSizeInventory(), inventorySlots.size(), false)) {
                return null;
            }
        } else {
            // player → panel
            for (int i = 0; i < panel.getSizeInventory(); i++) {
                if (!panel.isItemValid(i, items)) continue;

                Slot targetSlot = (Slot) this.inventorySlots.get(i);
                ItemStack target = targetSlot.getStack();

                int limit = Math.min(targetSlot.getSlotStackLimit(), items.getMaxStackSize());

                // Case 1: empty slot
                if (target == null) {
                    int move = Math.min(items.stackSize, limit);

                    ItemStack copy = items.copy();
                    copy.stackSize = move;

                    targetSlot.putStack(copy);
                    items.stackSize -= move;

                    // Case 2: merge into existing stack
                } else if (target.isItemEqual(items) && ItemStack.areItemStackTagsEqual(target, items)) {
                    int space = limit - target.stackSize;
                    if (space <= 0) continue;

                    int move = Math.min(items.stackSize, space);

                    target.stackSize += move;
                    items.stackSize -= move;

                    targetSlot.onSlotChanged();
                }

                if (items.stackSize <= 0) break;
            }
        }

        if (items.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }

        slot.onPickupFromSlot(p, items);

        return originalCopy.stackSize != items.stackSize ? originalCopy : null;
    }

}
