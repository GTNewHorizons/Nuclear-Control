package shedar.mods.ic2.nuclearcontrol;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import shedar.mods.ic2.nuclearcontrol.tileentities.TileEntityInfoPanel;

public class SlotFilter extends Slot {

    private final int slotIndex;

    public SlotFilter(IInventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
        this.slotIndex = slotIndex;
    }

    @Override
    public boolean isItemValid(ItemStack itemStack) {
        if (inventory instanceof ISlotItemFilter)
            return ((ISlotItemFilter) inventory).isItemValid(slotIndex, itemStack);
        return super.isItemValid(itemStack);
    }

    @Override
    public int getSlotStackLimit() {
        if (inventory instanceof TileEntityInfoPanel panel) {
            int limit = panel.inventory.getStackSizeLimit(slotIndex);
            if (limit > 0) return limit;
        }
        return super.getSlotStackLimit();
    }
}
